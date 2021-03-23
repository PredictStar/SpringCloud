package cn.nzxxx.predict.webinterface.service.impl;


import cn.nzxxx.predict.business.amms.service.AmComFileImplLG;
import cn.nzxxx.predict.business.amms.service.TaskParserFileService;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webinterface.service.InterfaceServiceI;
import cn.nzxxx.predict.webrequest.controller.PDFController;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCard;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardMaterials;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardReference;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardTool;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.JobCardMapper;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.JobCardMaterialsMapper;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.JobCardReferenceMapper;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.JobCardToolMapper;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import cn.nzxxx.predict.webrequest.service.impl.PdfServiceImpl;
import cn.nzxxx.predict.webrequest.service.impl.TranslateServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service("interfaceServiceImpl")
@Transactional
public class InterfaceServiceImpl implements InterfaceServiceI {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
	private JdbcTemplate jdbcTemplate;
    @Autowired
    PDFController pdfController ;
    @Autowired
    TaskParserFileService taskParserFileService;
    @Autowired
    private JobCardMapper jobCardMapper;
    @Autowired
    private JobCardMaterialsMapper jobCardMaterialsMapper;
    @Autowired
    private JobCardReferenceMapper jobCardReferenceMapper;
    @Autowired
    private JobCardToolMapper jobCardToolMapper;
    @Autowired
    TranslateServiceImpl translateServiceImpl ;
    @Autowired
    PdfServiceImpl pdfServiceImpl;
    @Resource(name = "AmComFileImplLG")
    AmComFileImplLG sqlService;
    @Autowired
    private TranslateServiceI translate;
    @Override
    public String syncJobCard(String ACTYPE, String CARDSOURCE, String JOBCARDNO)throws Exception {
        String resstr;
        //转大写
        CARDSOURCE=CARDSOURCE.toUpperCase();
        String sql;
        if("CRJ".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardCrj(ACTYPE, JOBCARDNO);
        }else if("BOEING".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardBoeing(ACTYPE, JOBCARDNO);
        }else if("AIRBUS".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardAirbus(ACTYPE, JOBCARDNO);
        }else{
            resstr =Help.returnClass(300,"来源不匹配","非CRJ/BOEING/AIRBUS");
            return resstr;
        }
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        if(re.size()==0){
            resstr =Help.returnClass(200,"无查询结果","ACTYPE:"+ACTYPE);
            return resstr;
        }
        Map<String, Object> minM=re.get(0);
        //元素数据主键
        Integer idInit=(Integer)minM.get("IDD");
        //根据主键生成翻译后word
        String translateTaskCard="" ;
        if("AIRBUS".equals(CARDSOURCE)){
            translateTaskCard= translateAirbusRC(idInit);

        }else{ //适应 CRJ BOEING
            translateTaskCard= pdfController.translateTaskCard(String.valueOf(idInit), CARDSOURCE, null, null);
        }

        if(StringUtils.isBlank(translateTaskCard)){
            resstr=Help.returnClass(300,"根据主键生成翻译后word方法返回为空","主键:"+idInit+";类型:crj");
            return resstr;
        }
        String pathh;
        if("AIRBUS".equals(CARDSOURCE)){
            //翻译后word生成地址所在
            pathh = translateTaskCard;
        }else{
            ReturnClass ReturnClass = Helper.stringJSONToPojo(translateTaskCard,ReturnClass.class);
            //翻译后word生成地址所在
            pathh = (String)ReturnClass.getValueDescribe();
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        minM.put("pathh",pathh);
        minM.put("uuid",uuid);
        minM.put("CARDSOURCE",CARDSOURCE);
        //插入 JobCard 表
        int insert = insertJobCard(minM);
        if(insert==0){
            resstr=Help.returnClass(300,"insertJobCard失败","返回条数0");
            return resstr;
        }
        //返回新增的 JobCard 表主键
        Integer jobCardId = selInitdataid(uuid, String.valueOf(idInit));
        //同步三个从表数据
        List<Map<String, Object>> getSyncTool = getSyncTool(idInit, CARDSOURCE );
        if(getSyncTool.size()>0){
            //赋值tool数据
            setSyncTool(jobCardId, getSyncTool);
        }
        List<Map<String, Object>> getSyncMaterials = getSyncMaterials(idInit, CARDSOURCE );
        if(getSyncMaterials.size()>0){
            //赋值 Materials 数据
            setSyncMaterials(jobCardId, getSyncMaterials);
        }
        List<Map<String, Object>> getSyncReference = getSyncReference(idInit, CARDSOURCE );
        if(getSyncReference.size()>0){
            //赋值 Reference 数据
            setSyncReference(jobCardId, getSyncReference);
        }
        resstr=Help.returnClass(200,"数据同步成功","ACTYPE:"+ACTYPE+";CARDSOURCE:"+CARDSOURCE+";JOBCARDNO:"+JOBCARDNO);
        return resstr;
    }
    //生成 Airbus 翻译后 word 并返回生成地址
    @Override
    public String translateAirbusRC(Integer idInit)throws Exception{
        List<Map<String, Object>> getBody=sqlService.getBody(idInit);
        Map<String,Object> operatemap=sqlService.operateList(getBody);
        operatemap.put("id",idInit);
        String textt =(String) operatemap.get("textt");
        //翻译
        //获取所有句柄
        List<Map<String, Object>> allSentence = translateServiceImpl.getAllSentence(null);
        //句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
        Map<Integer, List<Map<String, Object>>> splitSentenceL=translateServiceImpl.splitSentenceL(allSentence);
        String sentenceL=Helper.mapToStringJSON(splitSentenceL);
        textt=pdfServiceImpl.translateEToC(textt,null,sentenceL);
        operatemap.put("textt",textt);
        String pathh=taskParserFileService.setTemp(operatemap,"AIRBUSTrans");
        return pathh;
    }
    //000-25-900-101 (Config A43)  CRJ900
    public String jobCardCrj(String ACTYPE, String JOBCARDNO)throws Exception{
        //工卡机型 TASK_CARD_AC,值如 CRJ700/900/1000
        //工卡号 TASK_CARD_NUMBER
        //适用性 AIRCRAFT_EFFECTIVITY
        /*String type="CRJ";
        if(StringUtils.isNotBlank(ACTYPE)){
            ACTYPE=ACTYPE.replaceAll(type,"");
        }*/
        String sql="SELECT\n" +
                "t.CRJ_CARD_ID as IDD,\n" +
                "t.TASK_CARD_AC as ACTYPE,\n" +
                "t.TASK_CARD_NUMBER as JOBCARDNO,\n" +
                "t.TASK_TYPE as TASKTYPE,\n" +
                "t.SKILL as SKILL,\n" +
                "t.LABOR_HOURS as MHS,\n" +
                "t.ZONES as ZONE,\n" +
                "t.TASK_DESCRIPTION as TITLEEN ,\n" +
                "t.REFERENCE as REFERENCE,\n" +
                "t.VERSION_DATE as REVDATE,\n" +
                "t.AIRCRAFT_EFFECTIVITY as APPL,\n" +
                "'' as REVISON\n" +
                "FROM\n" +
                "crj_card AS t\n" +
                "where t.TASK_CARD_NUMBER='"+JOBCARDNO+"' ";

        if(StringUtils.isNotBlank(ACTYPE)){
            // sql+="and FIND_IN_SET('"+ACTYPE+"',REPLACE(REPLACE(t.TASK_CARD_AC,'CRJ',''),'/',','))>0 ";
            sql+="and t.TASK_CARD_AC='"+ACTYPE+"' ";
        }
        return sql;
    }
    public String jobCardBoeing(String ACTYPE, String JOBCARDNO)throws Exception{
        //工卡机型 TASK_CARD_AC,值如 737-600/700/800/900
        //工卡号 CARDNUM
        //适用性 AIRPLANE(即飞机的适应性)  ENGINE(即引擎的适应性-暂时没用上)
        /*if(StringUtils.isNotBlank(ACTYPE)){
            ACTYPE=ACTYPE.replaceAll("737-","");
        }*/
        String sql="SELECT \n" +
                "t.BOEING_CARD_ID as IDD,\n" +
                "t.TASK_CARD_AC as ACTYPE,\n" +
                "t.CARDNUM as JOBCARDNO,\n" +
                "t.TASK  as TASKTYPE,\n" +
                "t.SKILL as SKILL,\n" +
                "'' as MHS,\n" +
                "t.ZONE_T as ZONE,\n" +
                "t.TITLE as TITLEEN,\n" +
                "'' as REFERENCE,\n" +
                "t.VERSION_DATE as REVDATE,\n" +
                "t.AIRPLANE as APPL,\n" +
                "t.VERSION as REVISON\n" +
                "FROM boeing_card t\n" +
                "where t.CARDNUM='"+JOBCARDNO+"'" ;
        if(StringUtils.isNotBlank(ACTYPE)){
            sql+="and t.TASK_CARD_AC='"+ACTYPE+"' ";
        }
        return sql;
    }
    public String jobCardAirbus(String ACTYPE, String JOBCARDNO)throws Exception{
        String sql="SELECT \n" +
                "t.CARDID as IDD,\n" +
                "t.ACTYPE as ACTYPE,\n" +
                "t.JOBCARDNO as JOBCARDNO,\n" +
                "t.CARDTYPE  as TASKTYPE,\n" +
                "'' as SKILL,\n" +
                "'' as MHS,\n" +
                "'' as ZONE,\n" +
                "t.TITLEEN as TITLEEN,\n" +
                "t.REFERENCE as REFERENCE,\n" +
                "t.REVDATE as REVDATE,\n" +
                "t.EFFRG as APPL,\n" +
                "t.REVISON as REVISON\n" +
                "FROM amms_job_card t\n" +
                "where t.JOBCARDNO='"+JOBCARDNO+"'" ;
        if(StringUtils.isNotBlank(ACTYPE)){
            sql+="and t.ACTYPE='"+ACTYPE+"' ";
        }
        return sql;
    }
    /**
     * 插入 JobCard 表
     */
    public int insertJobCard(Map<String, Object> minM){
        JobCard jc=new JobCard();
        //插入
        jc.setActype((String) minM.get("ACTYPE"));
        jc.setJobcardno((String) minM.get("JOBCARDNO"));
        jc.setTasktype((String) minM.get("TASKTYPE"));
        jc.setSkill((String) minM.get("SKILL"));
        jc.setMhs((String) minM.get("MHS"));
        jc.setZone((String) minM.get("ZONE"));
        String TITLEEN=(String) minM.get("TITLEEN");
        if(StringUtils.isNotBlank(TITLEEN)){
            String titlech="";
            titlech = translate.sentenceTranslate(TITLEEN,null,null);
            //翻译标题
            jc.setTitlech(titlech);
        }
        jc.setTitleen(TITLEEN);
        jc.setReference((String) minM.get("REFERENCE"));
        jc.setRevdate((String) minM.get("REVDATE"));
        jc.setAppl((String) minM.get("APPL"));
        jc.setCardsource((String) minM.get("CARDSOURCE"));
        jc.setRevison((String)minM.get("REVISON"));
        String pathh=(String) minM.get("pathh");
        if(StringUtils.isBlank(pathh)){
            jc.setWordpath(null);//翻译后word路径
        }else{
            jc.setWordpath(pathh);//翻译后word路径
        }
        jc.setInitdataid((String) minM.get("uuid"));//	 原始数据id即crj_card,boeing_card,amms_job_card的主键;当前做中间列作用
        int insert = jobCardMapper.insert(jc);
        return insert;
    }
    /**
     * 已知uuid 获取 JobCard 表主键,并更新 Initdataid 为 原始数据id
     * uuid是临时存在 Initdataid 里
     * 返回新增的 JobCard 表主键
     */
    public Integer selInitdataid(String uuid,String initdataid){
        Integer cardid=null;
        List<JobCard> selectByMap = jobCardMapper.selectList(new QueryWrapper<JobCard>().eq("INITDATAID", uuid).select("cardid"));
        if(selectByMap.size()==0){
            return cardid;
        }
        JobCard jobCard = selectByMap.get(0);
        cardid = jobCard.getCardid();
        //更新 Initdataid 为 原始数据id
        JobCard ajc=new JobCard();
        ajc.setCardid(cardid);
        ajc.setInitdataid(initdataid);
        jobCardMapper.updateById(ajc);
        return cardid;
    }

    /**
     * 赋值tool数据
     */
    public void setSyncTool(Integer cardIDD,List<Map<String, Object>> list){
        for(Map<String, Object> map: list){
            String PN=(String) map.get("PN");
            String DESCR=(String) map.get("DESCR");
            JobCardTool jct=new JobCardTool();
            jct.setPn(PN);
            jct.setDescr(DESCR);
            jct.setJobCardId(cardIDD);
            jobCardToolMapper.insert(jct);
        }
    }
    /**
     * 获取同步tool数据
     */
    public List<Map<String, Object>> getSyncTool(Integer initIDD,String type){
        String sql="";
        if("CRJ".equals(type)){
            sql="SELECT\n" +
                "t.CRJ_CARD_ID,\n" +
                "t.REFERENCE as PN,\n" +
                "t.DESIGNATION as DESCR\n" +
                "FROM\n" +
                "crj_card_tool AS t\n" +
                "where t.CRJ_CARD_ID="+initIDD;
        }else if("BOEING".equals(type)){
            sql="SELECT\n" +
                "t.BOEING_CARD_ID,\n" +
                "t.REFERENCE as PN,\n" +
                "t.DESCRIPTION as DESCR\n" +
                "FROM\n" +
                "boeing_card_tool AS t\n" +
                "where t.BOEING_CARD_ID="+initIDD;
        }else if("AIRBUS".equals(type)){
            sql="SELECT\n" +
                "t.CARDID,\n" +
                "t.TOOLPN as PN,\n" +
                "t.DESCRIPTIONS as DESCR\n" +
                "FROM\n" +
                "amms_job_card_tools AS t\n" +
                "where t.CARDID="+initIDD;
        }
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        return re;
    }
    /**
     * 获取同步 materials 数据
     */
    public List<Map<String, Object>> getSyncMaterials(Integer initIDD,String type){
        String sql="";
        if("CRJ".equals(type)){
            sql="SELECT\n" +
                "t.CRJ_CARD_ID,\n" +
                "t.REFERENCE as PN,\n" +
                "t.DESIGNATION as DESCR\n" +
                "FROM\n" +
                "crj_card_materials AS t\n" +
                "where t.CRJ_CARD_ID="+initIDD;
        }else if("BOEING".equals(type)){
            sql="SELECT\n" +
                "t.BOEING_CARD_ID,\n" +
                "t.REFERENCE as PN,\n" +
                "t.DESCRIPTION as DESCR,\n" +
                "t.SPECIFICATION as NOTE\n" +
                "FROM\n" +
                "boeing_card_materials AS t\n" +
                "where t.BOEING_CARD_ID="+initIDD;
        }else if("AIRBUS".equals(type)){
            sql="SELECT\n" +
                "t.CARDID,\n" +
                "t.PN as PN,\n" +
                "t.DESCRIPTIONS as DESCR\n" +
                "FROM\n" +
                "amms_job_card_material AS t\n" +
                "WHERE  t.CARDID="+initIDD;
        }
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        return re;
    }
    /**
     * 赋值 materials 数据
     */
    public void setSyncMaterials(Integer cardIDD,List<Map<String, Object>> list){
        for(Map<String, Object> map: list){
            String PN=(String) map.get("PN");
            String DESCR=(String) map.get("DESCR");
            String NOTE=(String) map.get("NOTE");
            JobCardMaterials jcm=new JobCardMaterials();
            jcm.setPn(PN);
            jcm.setDescr(DESCR);
            jcm.setJobCardId(cardIDD);
            jcm.setNote(NOTE);
            jobCardMaterialsMapper.insert(jcm);
        }
    }
    /**
     * 获取同步 reference 数据
     */
    public List<Map<String, Object>> getSyncReference(Integer initIDD,String type){
        String sql="";
        if("CRJ".equals(type)){
            sql="SELECT\n" +
                "t.CRJ_CARD_ID,\n" +
                "t.MANUAL_NO as MANUAL_NO,\n" +
                "t.REFERENCE as REFERENCE,\n" +
                "t.DESIGNATION as TITLE\n" +
                "FROM\n" +
                "crj_card_reference AS t\n" +
                "where t.CRJ_CARD_ID="+initIDD;
        }else if("BOEING".equals(type)){
            sql="SELECT\n" +
                "t.BOEING_CARD_ID,\n" +
                "t.REFERENCE as REFERENCE,\n" +
                "t.TITLE as TITLE\n" +
                "FROM\n" +
                "boeing_card_reference AS t\n" +
                "where t.BOEING_CARD_ID="+initIDD;
        }else if("AIRBUS".equals(type)){
            sql="SELECT\n" +
                "t.CARDID,\n" +
                "t.REFERENCE as REFERENCE,\n" +
                "t.DESIGNATION as TITLE\n" +
                "FROM\n" +
                "amms_job_cardreferenced AS t\n" +
                "where t.CARDID="+initIDD;
        }
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        return re;
    }
    /**
     * 赋值 reference 数据
     */
    public void setSyncReference(Integer cardIDD,List<Map<String, Object>> list){
        for(Map<String, Object> map: list){
            String MANUAL_NO=(String) map.get("MANUAL_NO");
            String TITLE=(String) map.get("TITLE");
            String REFERENCE=(String) map.get("REFERENCE");
            JobCardReference jcr=new JobCardReference();
            jcr.setReference(REFERENCE);
            jcr.setTitle(TITLE);
            jcr.setManualNo(MANUAL_NO);
            jcr.setJobCardId(cardIDD);
            jobCardReferenceMapper.insert(jcr);
        }
    }
}