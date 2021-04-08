package cn.nzxxx.predict.webinterface.service.impl;


import cn.nzxxx.predict.business.amms.service.AmComFileImplLG;
import cn.nzxxx.predict.business.amms.service.TaskParserFileService;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webinterface.service.InterfaceServiceI;
import cn.nzxxx.predict.webrequest.controller.PDFController;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.*;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.*;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import cn.nzxxx.predict.webrequest.service.impl.PdfServiceImpl;
import cn.nzxxx.predict.webrequest.service.impl.TranslateServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
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
    @Autowired
    private JobCardBodyMapper jobCardBodyMapper;
    @Override
    public String syncJobCard(String IDD,String CARDSOURCE,String CREATEDBY)throws Exception {
        String resstr;
        //转大写
        CARDSOURCE=CARDSOURCE.toUpperCase();
        String sql;
        if("CRJ".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardCrj(IDD);
        }else if("BOEING".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardBoeing(IDD);
        }else if("AIRBUS".equalsIgnoreCase(CARDSOURCE)){
            sql =jobCardAirbus(IDD);
        }else{
            resstr =Help.returnClass(300,"来源不匹配","非CRJ/BOEING/AIRBUS");
            return resstr;
        }
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        if(re.size()==0){
            resstr =Help.returnClass(200,"无查询结果","CARDSOURCE:"+CARDSOURCE+";IDD"+IDD);
            return resstr;
        }
        Map<String, Object> minM=re.get(0);
        //元素数据主键
        Integer idInit=(Integer)minM.get("IDD");
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        minM.put("uuid",uuid);
        minM.put("CARDSOURCE",CARDSOURCE);
        minM.put("CREATEDBY",CREATEDBY);
        //插入 JobCard 表
        int insert = insertJobCard(minM);
        if(insert==0){
            resstr=Help.returnClass(300,"insertJobCard失败","返回条数0");
            return resstr;
        }
        //返回新增的 JobCard 表主键
        Integer jobCardId = selInitdataid(uuid, String.valueOf(idInit));
        //根据主键翻译并保存进 job_card_body
        String translateTaskCard="" ;
        if("AIRBUS".equals(CARDSOURCE)){
            translateTaskCard= transAirbusTCInStorage(idInit,jobCardId);
        }else{ //适应 CRJ BOEING
            //根据主键和类型 查询 AnalyPdfData 数据
            translateTaskCard=pdfController.transTCInStorage(String.valueOf(idInit), CARDSOURCE,jobCardId);
        }
        if(StringUtils.isBlank(translateTaskCard)){
            resstr=Help.returnClass(300,"同步数据到job_card_body异常","主键:"+idInit+";类型:"+CARDSOURCE);
            return resstr;
        }

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
        resstr=Help.returnClass(200,"数据同步成功","IDD:"+IDD+";CARDSOURCE:"+CARDSOURCE+";CREATEDBY:"+CREATEDBY);
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
        textt=pdfServiceImpl.translateEToC(textt,null,sentenceL,null);
        operatemap.put("textt",textt);
        String pathh=taskParserFileService.setTemp(operatemap,"AIRBUSTrans");
        return pathh;
    }
    //Airbus 翻译后数据入库-job_card_body
    public String transAirbusTCInStorage(Integer idInit,Integer jobCardId)throws Exception{
        String re="";
        try {
            //amms_job_cardbody 数据
            List<Map<String, Object>> getBody=sqlService.getBody(idInit);
            Map<String,Object> operatemap=sqlService.operateList(getBody);
            operatemap.put("id",idInit);
            operatemap.put("jobCardId",jobCardId);
            String textt =(String) operatemap.get("textt");
            //翻译
            //获取所有句柄
            List<Map<String, Object>> allSentence = translateServiceImpl.getAllSentence(null);
            //句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
            Map<Integer, List<Map<String, Object>>> splitSentenceL=translateServiceImpl.splitSentenceL(allSentence);
            String sentenceL=Helper.mapToStringJSON(splitSentenceL);
            //需要入库的数据
            List<JobCardBody> list=new ArrayList<JobCardBody>();
            pdfServiceImpl.translateEToC(textt,null,sentenceL,list);
            //图片入数据集
            List<byte[]> imageL =(List) operatemap.get("image");
            if(imageL.size()>0){
                List<String> imageTit =(List) operatemap.get("imageTit");
                for(int i=0;i<imageL.size();i++){
                    byte[] bytes = imageL.get(i);
                    String base64 = Helper.byteToBase64(bytes);
                    String imageT = imageTit.get(i);
                    //图片数据的二次处理
                    base64=base64.replaceFirst("^imageSingle_IMW[^;]+;|^","data:image/png;base64,");
                    JobCardBody jcb=new JobCardBody();
                    jcb.setBodytype("IMAGE");
                    jcb.setBodyval("<div><img src='"+base64+"'/></div><div>"+imageT+"</div>");
                    list.add(jcb);
                }
            }
            //入库job_card_body
            for(int i=0;i<list.size();i++){
                JobCardBody jcb = list.get(i);
                jcb.setJobcardid(jobCardId);
                double di=i;
                jcb.setOrderby(di);
                jobCardBodyMapper.insert(jcb);
            }
            re=Help.returnClass(200,"Airbus 翻译后数据入库-job_card_body成功","");
        } catch (Exception e) {
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
        }
        return re;
    }
    //000-25-900-101 (Config A43)  CRJ900
    public String jobCardCrj(String IDD)throws Exception{
        //工卡机型 TASK_CARD_AC,值如 CRJ700/900/1000
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
                "where t.CRJ_CARD_ID='"+IDD+"' ";
        return sql;
    }
    public String jobCardBoeing(String IDD)throws Exception{
        //工卡机型 TASK_CARD_AC,值如 737-600/700/800/900
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
                "where t.BOEING_CARD_ID='"+IDD+"'" ;
        return sql;
    }
    public String jobCardAirbus(String IDD)throws Exception{
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
                "where t.CARDID='"+IDD+"'" ;
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
            titlech = translate.sentenceTranslate(TITLEEN,null,null,null,null);
            //翻译标题
            jc.setTitlech(titlech);
        }
        jc.setTitleen(TITLEEN);
        jc.setReference((String) minM.get("REFERENCE"));
        jc.setRevdate((String) minM.get("REVDATE"));
        jc.setAppl((String) minM.get("APPL"));
        jc.setCardsource((String) minM.get("CARDSOURCE"));
        jc.setRevison((String)minM.get("REVISON"));
        jc.setCreatedby((String)minM.get("CREATEDBY"));
        /*String pathh=(String) minM.get("pathh");*/
        /*if(StringUtils.isBlank(pathh)){
            jc.setWordpath(null);//翻译后word路径
        }else{
            jc.setWordpath(pathh);//翻译后word路径
        }*/
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