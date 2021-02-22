package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.controller.PDFController;
import cn.nzxxx.predict.webrequest.controller.TranslateController;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("pdfService")
@Transactional
public class PdfServiceImpl implements PdfServiceI {
    private final Logger logger=Logger.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
    @Autowired
    TranslateController translateController ;
	/**
	 * 获取数据并翻译下载
	 * @author 子火 
	 * 2021年2月19日13:59:01
	 */
	@Override
	public void translateTaskCard(String analyPdfData, HttpServletRequest request, HttpServletResponse response)throws Exception{
	    //解决 stringJSONToMap 会报错的问题(analyPdfData的colMatch(列匹配规则中有\导致))
        String analyPdfDataN=analyPdfData.replaceAll("\\\\","反斜杠暂时去掉");
        //System.out.println(analyPdfDataN);
        Map analyPdfM = Helper.stringJSONToMap(analyPdfDataN);
        FormPdf fpdf=new FormPdf();
        String fileType=(String) analyPdfM.get("fileType");
        fpdf.setFileType(fileType);
        Map translateTemp = getTranslateTemp(fileType);
        List vallList=(List)translateTemp.get("vall");
        List sectionsList=(List)translateTemp.get("sections");
        //对数值做处理进行翻译
        Map vallMap=(Map)analyPdfM.get("vall");
        Map sectionsMap=(Map)analyPdfM.get("sections");
        translateVall(vallMap,vallList);
        translateSections(sectionsMap,sectionsList);
        ReturnClass returnClass = fpdf.cWordT(analyPdfM, response);
        if(!"200".equals(returnClass.getStatusCode())){
            String strE = Helper.pojoToStringJSON(returnClass);
            logger.error(strE);
		}
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
            vallList.add("Skill");
            vallList.add("Description");
            sectionsList.add("titV");
            sectionsList.add("startV");
            sectionsList.add("endV");
        }else if("boeing".equals(fileType)){
            vallList.add("TITLE");
            vallList.add("TASK");
            vallList.add("WORKAREA");
            vallList.add("SKILL");
            vallList.add("CONTENT");
            sectionsList.add("STARTV");
            sectionsList.add("STAT");
        }
        return map;
    }
    //对 analyPdfM 里的 sections(模板里的循环部分,即区块对) 进行获取并翻译
    public void translateSections(Object obj,List sectionsList){
        if(sectionsList==null||obj==null){
            return;
        }
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object key:map.keySet()){
                String keyy=(String)key;
                if(sectionsList.contains(keyy)){
                    String value=(String)map.get(keyy);
                    value=translateEToC(value);
                    map.put(keyy,value);
                }else{
                    Object value=map.get(keyy);
                    if(value instanceof Map||(value instanceof List)){
                        translateSections(value,sectionsList);
                    }
                }
            }
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    translateSections(value,sectionsList);
                }
            }
        }
    }
    //对 analyPdfM 里的 vall 进行获取并翻译
    public void translateVall(Map vallMap,List vallList){
        if(vallMap==null||vallList==null){
            return;
        }
        for(Object key:vallMap.keySet()){
            String keyy=(String)key;
            if(vallList.contains(keyy)){
                String value=(String)vallMap.get(keyy);
                value=tEToC(value,false);
                vallMap.put(keyy,value);
            }
        }
    }
    //英文转中文
    public String translateEToC(String English){
        StringBuilder sbS=new StringBuilder();
        //大块划分为小块
        List<String> splitList = splitEnglish(English);
        for(String str: splitList){
            //再翻译
            String s = tEToC(str,true);
            if (sbS.length()==0){
                sbS.append(s);
            }else{
                sbS.append("\n"+s);
            }
        }
        return sbS.toString();
    }
    //英文转中文,中英内容累加
    public String tEToC(String English,boolean retract){
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
        String translate = translate(English);
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
    public String translate(String English){
        if(StringUtils.isBlank(English)){
            return "";
        }
        English=twiceAnalysis(English);
        String reStr=tranInterface(English);
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
    public String tranInterface(String English){
        return  "这是中文;这是原文截取:"+English.substring(0,10>English.length()?English.length():10);
    }
    /**
     * 把要翻译的部分拆成几部分进行翻译
     * @return
     */
    public List<String> splitEnglish(String English){
        List<String> splitList=new ArrayList();
        splitList.add("");
        String[] splitEnglish = English.split("\n");
        for(String str:splitEnglish){
            boolean bol=true;
            String pp="^ *[A-Z]\\. |^ *\\([a-z]\\) |^ *\\(\\d+\\) |^ *\\d\\. ";
            Pattern pattern = Pattern.compile(pp);
            Matcher matcher = pattern.matcher(str);
            if(matcher.find()){
                bol=false;
            }
            if(bol){
                int size=splitList.size() - 1;
                String s = splitList.get(size);
                s=s+"\n"+str;
                splitList.set(size,s);
            }else {
                splitList.add(str);
            }
        }
        return splitList;
    }


	/*
	 * 根据id 获取 ANALY_PDF_DATA 数据
	 * 2021年2月19日13:58:47
	 */
    @Override
	public String getAnalyPdfData(String idd){
        String reStr="";
        if(StringUtils.isBlank(idd)){
            return reStr;
        }
		String sql="SELECT\n" +
				"crj_card.ANALY_PDF_DATA AS pdfdata\n" +
				"FROM\n" +
				"crj_card\n" +
				"WHERE CRJ_CARD_ID="+idd;
		List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
		if(re.size()>0){
            Map<String, Object> stringObjectMap = re.get(0);
            reStr=(String) stringObjectMap.get("pdfdata");
        }
		return reStr;
	}

 
	
}