package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.controller.PDFController;
import cn.nzxxx.predict.webrequest.controller.TranslateController;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardBody;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("pdfService")
@Transactional
public class PdfServiceImpl implements PdfServiceI {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
    @Autowired
    TranslateController translateController ;
    @Autowired
    TranslateServiceImpl translateServiceImpl ;
    @Autowired
    TrInStorageServiceImpl trInStorageServiceImpl ;
	/**
	 * 获取数据并翻译下载
	 * @author 子火 
	 * 2021年2月19日13:59:01
	 */
	@Override
	public String translateTaskCard(Map analyPdfM, HttpServletRequest request, HttpServletResponse response)throws Exception{
	    String re="";
        FormPdf fpdf=new FormPdf();
        String fileType=(String) analyPdfM.get("fileType");
        fpdf.setFileType(fileType);
        Map translateTemp = getTranslateTemp(fileType);
        List vallList=(List)translateTemp.get("vall");
        List sectionsList=(List)translateTemp.get("sections");
        //对数值做处理进行翻译
        Map vallMap=(Map)analyPdfM.get("vall");
        Map sectionsMap=(Map)analyPdfM.get("sections");
        String sentenceL="";
        //获取所有句柄
        List<Map<String, Object>> allSentence = translateServiceImpl.getAllSentence(fileType);
        //句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
        Map<Integer, List<Map<String, Object>>> splitSentenceL=translateServiceImpl.splitSentenceL(allSentence);
        sentenceL=Helper.mapToStringJSON(splitSentenceL);
        translateVall(vallMap,vallList,fileType,sentenceL);
        translateSections(sectionsMap,sectionsList,fileType,sentenceL);
        ReturnClass returnClass = fpdf.cWordT(analyPdfM, response);
        if(!"200".equals(returnClass.getStatusCode())){
            String strE = Helper.pojoToStringJSON(returnClass);
            logger.error(strE);
		}else{
            re=Helper.pojoToStringJSON(returnClass);
        }
		return re;

    }

    /**
     * CRJ BOEING 数据翻译并入 job_card_body 库
     * @param analyPdfM
     * @return
     * @throws Exception
     */
    @Override
    public String transTCInStorage(Map analyPdfM,Integer jobCardId)throws Exception{
        String re="";
        FormPdf fpdf=new FormPdf();
        String fileType=(String) analyPdfM.get("fileType");
        fpdf.setFileType(fileType);
        LinkedHashMap<String,Map<String,String>> translateTemp = getInStorageTemp(fileType);
        String sentenceL="";
        //获取所有句柄
        List<Map<String, Object>> allSentence = translateServiceImpl.getAllSentence(fileType);
        //句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
        Map<Integer, List<Map<String, Object>>> splitSentenceL=translateServiceImpl.splitSentenceL(allSentence);
        sentenceL=Helper.mapToStringJSON(splitSentenceL);
        ReturnClass returnClass = trInStorageServiceImpl.transInStorage(analyPdfM,translateTemp,sentenceL,jobCardId);
        if(!"200".equals(returnClass.getStatusCode())){
            String strE = Helper.pojoToStringJSON(returnClass);
            logger.error(strE);
        }else{
            re=Helper.pojoToStringJSON(returnClass);
        }
        return re;

    }

    /**
     * 获取需要翻译的模板数据
     * @return
     */
    public Map getTranslateTemp(String fileType){
        Map map=new HashMap();
        List vallList=new ArrayList();
        map.put("vall",vallList);
        List sectionsList=new ArrayList();
        map.put("sections",sectionsList);
        if("crj".equals(fileType)){
            /*vallList.add("Skill");*/
            vallList.add("Description");
            sectionsList.add("titV");
            sectionsList.add("startV");
            sectionsList.add("endV");
        }else if("boeing".equals(fileType)){
            /*vallList.add("TITLE");
            vallList.add("TASK");
            vallList.add("WORKAREA");
            vallList.add("SKILL");*/
            vallList.add("CONTENT");
            sectionsList.add("STARTV");
            sectionsList.add("STAT");
        }
        return map;
    }

    /**
     * 获取入 job_card_body 库的模板名称
     * @param fileType
     * @return
     */
    public LinkedHashMap<String,Map<String,String>> getInStorageTemp(String fileType){
        LinkedHashMap<String,Map<String,String>> map=new LinkedHashMap();
        if("crj".equals(fileType)){
            Map titV=new HashMap();
            titV.put("type","txt");
            map.put("titV",titV);

            Map startV=new HashMap();
            startV.put("type","txt");
            map.put("startV",startV);

            Map tablee=new HashMap();
            tablee.put("type","table");
            map.put("tablee",tablee);

            Map endV=new HashMap();
            endV.put("type","txt");
            map.put("endV",endV);
        }else if("boeing".equals(fileType)){
            Map STAT=new HashMap();
            STAT.put("type","txt");
            map.put("STAT",STAT);

            Map TABLET=new HashMap();
            TABLET.put("type","table");
            map.put("TABLET",TABLET);

            Map IMAGET=new HashMap();
            IMAGET.put("type","image");
            map.put("IMAGET",IMAGET);
        }
        return map;
    }
    //对 analyPdfM 里的 sections(模板里的循环部分,即区块对) 进行获取并翻译
    public void translateSections(Object obj,List sectionsList,String fileType,String sentenceL){
        if(sectionsList==null||obj==null){
            return;
        }
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object key:map.keySet()){
                String keyy=(String)key;
                if(sectionsList.contains(keyy)){
                    String value=(String)map.get(keyy);
                    value=translateEToC(value,fileType,sentenceL,null);
                    map.put(keyy,value);
                }else{
                    Object value=map.get(keyy);
                    if(value instanceof Map||(value instanceof List)){
                        translateSections(value,sectionsList,fileType,sentenceL);
                    }
                }
            }
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    translateSections(value,sectionsList,fileType,sentenceL);
                }
            }
        }
    }
    //对 analyPdfM 里的 vall 进行获取并翻译
    public void translateVall(Map vallMap,List vallList,String fileType,String sentenceL){
        if(vallMap==null||vallList==null){
            return;
        }
        for(Object key:vallMap.keySet()){
            String keyy=(String)key;
            if(vallList.contains(keyy)){
                String value=(String)vallMap.get(keyy);
                value=tEToC(value,false,fileType,sentenceL,null);
                vallMap.put(keyy,value);
            }
        }
    }
    //英文转中文
    public String translateEToC(String English,String fileType,String sentenceL,List<JobCardBody> list){
        StringBuilder sbS=new StringBuilder();
        //大块划分为小块
        List<String> splitList = splitEnglish(English);
        for(String str: splitList){
            //再翻译
            String s = tEToC(str,true,fileType,sentenceL,list);
            if (sbS.length()==0){
                sbS.append(s);
            }else{
                sbS.append("\n"+s);
            }
        }
        return sbS.toString();
    }
    //英文转中文,中英内容累加
    public String tEToC(String English,boolean retract,String fileType,String sentenceL,List<JobCardBody> list){
        String Chinese="";
        //被翻译内容前的空格获取
        String bla="";
        String pp="^ +";
        Pattern pattern = Pattern.compile(pp);
        Matcher matcher = pattern.matcher(English);
        if(retract){ //是否有缩进
            bla="   ";//设置三个空格,为了有缩进效果
        }
        if(matcher.find()){
            bla=bla+matcher.group(0);
        }
        String translate = translate(English,fileType,sentenceL,list);
        if(StringUtils.isNotBlank(translate)){
            translate=bla+translate;
            Chinese="\n"+translate;
        }
        String returnStr=English+Chinese;
        return returnStr;
    }
    /**
     * 请求翻译接口
     */
    public String translate(String English,String fileType,String sentenceL,List<JobCardBody> list){
        if(StringUtils.isBlank(English)){
            return "";
        }
        //初始数据
        String initEnglish=English;
        English=twiceAnalysis(English);
        String reStr=tranInterface(English,fileType,sentenceL,list,initEnglish);
        return  reStr;
    }
    //对需要翻译内容的二次处理,如去除两边空格,去掉如 (2)
    public String twiceAnalysis(String English){
        String reStr="";
        String pp="^ *([A-Z]\\.|\\([a-z]\\)|\\(\\d+\\)|\\d\\.)?([\\s\\S]+)";
        Pattern pattern = Pattern.compile(pp);
        Matcher matcher = pattern.matcher(English);
        if(matcher.find()){
            reStr = matcher.group(2);
        }
        reStr=Helper.nvlString(reStr);
        return reStr;
    }
    //翻译
    public String tranInterface(String English,String fileType,String sentenceL,List<JobCardBody> list,String initEnglish){
        String etoc="";
        try {
            etoc = translateController.etocList(fileType, English, sentenceL,list,initEnglish);
        } catch (Exception e) {
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
        }
        return etoc;
        //return  "这是中文;这是原文截取:"+English.substring(0,10>English.length()?English.length():10);
    }
    /**
     * 把要翻译的部分拆成几部分进行翻译
     * @return
     */
    public List<String> splitEnglish(String English){
        List<String> splitList=new ArrayList();
        splitList.add("");
        String[] splitEnglish = English.split("\n");
        boolean nextBol=false;//匹配到后,下一行是否是句子的开头
        for(String str:splitEnglish){
            if(nextBol){
                nextBol=false;
                splitList.add(str);
                continue;
            }
            boolean bol=false;//当前匹配行是否是句子的开头
            //String pp="^ *[A-Z]\\. |^ *\\([a-z]\\) |^ *\\(\\d+\\) |^ *Refer to Figure";
            //airbus 开头值如 A.Get Access 没有空格
            String pp="^ *[A-Z]\\.|^ *\\([a-z]\\) |^ *\\(\\d+\\) |^ *Refer to Figure";
            Pattern pattern = Pattern.compile(pp);
            Matcher matcher = pattern.matcher(str);
            if(matcher.find()){
                bol=true;//本行是句子的开头
            }else{
                pp="^ *\\d\\. |^ *Ref\\.Refer";
                pattern = Pattern.compile(pp);
                matcher = pattern.matcher(str);
                if(matcher.find()){
                    bol=true;//本行是句子的开头
                    nextBol=true;//下一行也是句子的开头
                }
            }
            if(bol){
                splitList.add(str);
            }else {
                int size=splitList.size() - 1;
                String s = splitList.get(size);
                s=s+"\n"+str;
                splitList.set(size,s);
            }

        }
        return splitList;
    }


	/*
	 * 根据id 获取 ANALY_PDF_DATA 数据
	 * 2021年2月19日13:58:47
	 */
    @Override
	public String getAnalyPdfData(String idd,String type){
        String reStr="";
        if(StringUtils.isBlank(idd)||StringUtils.isBlank(type)){
            return reStr;
        }
        //转小写
        type=type.toLowerCase();
        String sql="";
        if("crj".equals(type)){
            sql="SELECT\n" +
            "crj_card.ANALY_PDF_DATA AS pdfdata\n" +
            "FROM\n" +
            "crj_card\n" +
            "WHERE CRJ_CARD_ID="+idd;
        }else if("boeing".equals(type)){
            sql="SELECT\n" +
            " boeing_card.ANALY_PDF_DATA AS pdfdata\n" +
            " FROM\n" +
            " boeing_card\n" +
            " WHERE BOEING_CARD_ID="+idd;
        }
        if (StringUtils.isBlank(sql)){
            return reStr;
        }
		List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
		if(re.size()>0){
            Map<String, Object> stringObjectMap = re.get(0);
            reStr=(String) stringObjectMap.get("pdfdata");
        }
		return reStr;
	}

 
	
}