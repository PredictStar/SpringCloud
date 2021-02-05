package cn.nzxxx.predict.config.pdftable;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import com.deepoove.poi.data.style.TableStyle;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.jbig2.SegmentData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.ResourceUtils;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


//解析pdf,form 数据 生成word
public class FormPdf {
    private Map<String,Map<String,Object>> mapp=new HashMap<String,Map<String,Object>>();
    private String fileType="";
    private List<TextElement> pageTextT=new ArrayList<TextElement>();
    public void setPageTextT(List<TextElement> pageTextT) {
        this.pageTextT = pageTextT;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    //初始化
    public FormPdf() {
        //------------CRJ 开始--------------------
        Map<String,Object> crjMap=new HashMap<String,Object>();
        //表规则
        Map<String,Map> tableCRJ=new HashMap<String,Map>();
        //列个数即key以空格分隔的数组个数
        // 匹配时_要换为空格,且获取值清除两边空格 !!!
        Map<String,Object> table2=new HashMap<String,Object>();
        List<String> table2List=new ArrayList<String>();
        table2List.add("^(.+ )?(\\S+) (AIPC \\S+)$");
        table2.put("colMatch",table2List);//列值获取方式
        table2.put("valNVL","up");// //up 列无值时,取上行值
        tableCRJ.put("NAME MANUAL AIPC_REFERENCE",table2);                                                     //1. part
        Map<String,Object> table1=new HashMap<String,Object>();
        List<String> table1List=new ArrayList<String>();
        table1List.add("^([A-Z0-9\\- \\(\\)/:]+)( .+)?$");
        table1List.add("()(.+)");
        table1.put("colMatch",table1List);//列值获取方式
        table1.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableCRJ.put("REFERENCE DESIGNATION",table1);                      //1. Consumable Materials; Tools and Equipment;
        Map<String,Object> table3=new HashMap<String,Object>();
        List<String> table3List=new ArrayList<String>();
        table3List.add("^([A-Z0-9\\-]+ )(AMM \\S+ )(.+)$");
        table3List.add("^([A-Z0-9\\-]+ )(CMM \\S+ )(.+)$");
        table3List.add("^()()(.+)$");
        table3.put("colMatch",table3List);//列值获取方式
        table3.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableCRJ.put("MANUAL_NO REFERENCE DESIGNATION",table3);//1. Reference Information ; Standard Practices Information
        Map<String,Object> table4=new HashMap<String,Object>();
        List<Integer> setMat = Arrays.asList(1, 3, 4);
        //列与具名组匹配对应规则;
        table4.put("setMat",setMat);
        List<String> table4List=new ArrayList<String>();
        table4List.add("^(\\S+( LOWER)? )(\\S+ )(.+)$");
        table4.put("colMatch",table4List);//列值获取方式
        tableCRJ.put("CB-PANEL CB-NO NAME",table4);                                                       //2 Job Set−Up
        Map<String,Object> table5=new HashMap<String,Object>();
        List<String> table5List=new ArrayList<String>();//621BR Fixed Leading−Edge Skin CSP−B−001 AMM 57−41−05−400−802;142AR Forward Potable−Water
        //第一个不匹配,匹配第二个
        table5List.add("([A-Z0-9]+ )(.+)( \\S+-\\S+-\\S+)( AMM \\S+)");
        table5List.add("([A-Z0-9]+ )?(.+)()()");
        table5.put("colMatch",table5List);//列值获取方式
        table5.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableCRJ.put("PANEL NAME MANUAL_NO REFERENCE",table5);//2 Job Set−Up;4 Close Out
        //此行如果值和表头数不对,认为是同一条数据
        crjMap.put("tableRule",tableCRJ);

        crjMap.put("temp","taskCardCRJT.docx");//模板名称
        crjMap.put("imageW",660);//图片宽
        crjMap.put("imageH",960);//图片高
        //页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意analyPdfM没值时图片数据先不赋进去))
        //前260字包含(7)(8)(9)(10)(11)表是测试页面
        Map<String, Integer> pageTypeCM=new LinkedCaseInsensitiveMap();
        pageTypeCM.put("AirlineDesignatorAircraft",1);//先判断1,其包含2的判断依据
        pageTypeCM.put("AircraftSeriesAircraftNumber",2);
        crjMap.put("pageType",pageTypeCM);
        //提取值规则定义
        List<Map<String,Object>> ruleCRJ=new ArrayList<Map<String,Object>>();
        Map<String, Object> mapRule1=new HashMap<String, Object>();
        mapRule1.put("tempKey","AircraftSeries");//对应模板值
        mapRule1.put("matchT","Aircraft Series");//匹配开始的正则
        mapRule1.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule1.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule1.put("matchI","^(\\S+/\\S+/\\S+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule1);
        Map<String, Object> mapRule1_2=new HashMap<String, Object>();
        mapRule1_2.put("tempKey","TaskCardNumber");//对应模板值
        mapRule1_2.put("matchT","Task Card Number");//匹配开始的正则
        mapRule1_2.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule1_2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule1_2.put("matchI","(\\S+-\\S+-\\S+ \\(\\S+ \\S+\\))$");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule1_2);
        Map<String, Object> mapRule2=new HashMap<String, Object>();
        mapRule2.put("tempKey","Rev");//对应模板值
        mapRule2.put("matchT","(Manual Rev:)|(Manual Re v:)");//匹配开始的正则
        mapRule2.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule2.put("matchI","Rev: ([0-9]+)|Re v: ([0-9]+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule2);
        Map<String, Object> mapRule3=new HashMap<String, Object>();
        mapRule3.put("tempKey","Amend");//对应模板值
        mapRule3.put("matchT","e\\S*n\\S*t:");//匹配开始的正则
        mapRule3.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule3.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule3.put("matchI","e\\S*n\\S*t: ([0-9]+)");//被提取值正则匹配规则,具名组匹配提值
        mapRule3.put("continueMatch","true");//当前行未匹配是否继续匹配下一个,因为此容易不匹配(老乱码),所以设此
        ruleCRJ.add(mapRule3);
        Map<String, Object> mapRule4=new HashMap<String, Object>();
        mapRule4.put("tempKey","TaskType");//对应模板值
        mapRule4.put("matchT","Task Type");//匹配开始的正则
        mapRule4.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule4.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        //先如下写,若不对可直接定义 值A|值B
        mapRule4.put("matchI","^([A-Z]+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule4);
        Map<String, Object> mapRule5=new HashMap<String, Object>();
        mapRule5.put("tempKey","Skill");//对应模板值
        mapRule5.put("matchT","Type Skill Labor");//匹配开始的正则
        mapRule5.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule5.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule5.put("matchI"," ([A-Z]+) ");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule5);
        Map<String, Object> mapRule6=new HashMap<String, Object>();
        mapRule6.put("tempKey","LaborHours");//对应模板值
        mapRule6.put("matchT","Labor Hours");//匹配开始的正则
        mapRule6.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule6.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule6.put("matchI","([0-9]+\\.[0-9]+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule6);
        Map<String, Object> mapRule7=new HashMap<String, Object>();
        mapRule7.put("tempKey","NbrOfPersons");//对应模板值
        mapRule7.put("matchT","Nbr of Persons");//匹配开始的正则
        mapRule7.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule7.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule7.put("matchI"," ([0-9]+)$");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule7);
        Map<String, Object> mapRule8=new HashMap<String, Object>();
        mapRule8.put("tempKey","Zones");//对应模板值
        mapRule8.put("matchT","Zone\\(s\\):");//匹配开始的正则
        mapRule8.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule8.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule8.put("matchI","Zone\\(s\\): (.+)$");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule8);
        Map<String, Object> mapRule9=new HashMap<String, Object>();
        mapRule9.put("tempKey","Effectivity");//对应模板值
        mapRule9.put("matchT","Aircraft");//匹配开始的正则
        mapRule9.put("valType","composite");//值类型:单行 single ,多行 rowset ,复合 composite
        List<Map<String, Object>> compositeList=new ArrayList<Map<String, Object>>();
        Map<String, Object> Effectivity1=new HashMap<String, Object>();
        Effectivity1.put("matchT","Aircraft");
        Effectivity1.put("valType","single");
        Effectivity1.put("indexI",0);
        Effectivity1.put("matchI","Aircraft (.+) Mechanic");
        compositeList.add(Effectivity1);
        Map<String, Object> Effectivity2=new HashMap<String, Object>();
        Effectivity2.put("matchT","Effectivity:");
        Effectivity2.put("valType","rowset");
        Effectivity2.put("indexI",0);
        Effectivity2.put("matchI","Effectivity: (.+)");//匹配开始()里是要的值
        Effectivity2.put("endMatch","DESCRIPTION:");//匹配结束
        Effectivity2.put("isChangeIndex","true");//改变i为当前行
        compositeList.add(Effectivity2);
        mapRule9.put("compositeList",compositeList);
        ruleCRJ.add(mapRule9);
        Map<String, Object> mapRule10=new HashMap<String, Object>();
        mapRule10.put("tempKey","Description");//对应模板值
        mapRule10.put("matchT","DESCRIPTION:");//匹配开始的正则
        mapRule10.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule10.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule10.put("matchI","DESCRIPTION: (.+)");//匹配开始()里是要的值
        mapRule10.put("endMatch","REFERENCE:");//匹配结束
        mapRule10.put("isChangeIndex","true");//改变i为当前行
        ruleCRJ.add(mapRule10);
        Map<String, Object> mapRule11=new HashMap<String, Object>();
        mapRule11.put("tempKey","Reference");//对应模板值
        mapRule11.put("matchT","REFERENCE:");//匹配开始的正则
        mapRule11.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule11.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule11.put("matchI","^REFERENCE: (.+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule11);
        Map<String, Object> mapRule12=new HashMap<String, Object>();
        mapRule12.put("tempKey","MrmReference");//对应模板值
        mapRule12.put("matchT","(MRM REFERENCE:)|(MRM REFERENCES:)");//匹配开始的正则
        mapRule12.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule12.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule12.put("matchI","^MRM REFERENCE: (.+)|^MRM REFERENCES: (.+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule12);
        Map<String, Object> mapRule121=new HashMap<String, Object>();
        mapRule121.put("tempKey","Note");//对应模板值
        mapRule121.put("matchT","(MRM REFERENCE:)|(MRM REFERENCES:)");//匹配开始的正则
        mapRule121.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule121.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite
        //现就多行如此:一开始根据matchI,就没匹配到内容,直接结束此(此时:matchT一般设为其前必有元素;)
        //noMatchTOver 是根据 matchT,就没匹配到内容,直接结束此
        mapRule121.put("noMatchIOver","true");
        mapRule121.put("matchI","^NOTE: (.+)");//被提取值正则匹配规则,具名组匹配提值
        mapRule121.put("endMatch","^[0-9]\\. (.+)");//匹配结束
        mapRule121.put("isChangeIndex","true");//改变i为当前行
        mapRule121.put("continueMatch","true");//未匹配表跳过继续匹配下一个
        ruleCRJ.add(mapRule121);
        Map<String, Object> mapRule13=new HashMap<String, Object>();
        mapRule13.put("tempKey","jobSet");//对应模板值
        mapRule13.put("matchT","^[0-9]\\. (.+)");//匹配开始的正则
        //mapRule13.put("endMatch","在word,此模板在最后所以就没设此");//模板匹配结束,遇到结束才允许删这条匹配规则(就因为有多个才用模板)
        mapRule13.put("valType","sections");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections
        mapRule13.put("isFirstS","true");//是否是首区块对
        List<Map> templateList13=new ArrayList<Map>();
            Map tempVal13_1=new HashMap();
            tempVal13_1.put("tempKey","titV");//对应模板值
            tempVal13_1.put("matchT","^[0-9]\\. ");
            tempVal13_1.put("valType","rowset");;//值类型
            tempVal13_1.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
            tempVal13_1.put("matchI","^([0-9]\\. .+)");//匹配开始()里是要的值
            tempVal13_1.put("endMatch","(^A.)|(^[0-9]\\.)");//匹配结束
            tempVal13_1.put("isChangeIndex","true");//改变i为当前行
            tempVal13_1.put("isCompSpace","true");
            templateList13.add(tempVal13_1);
            Map tempVal13_2=new HashMap();
            tempVal13_2.put("tempKey","tableTemp");//对应模板值
            tempVal13_2.put("matchT","(^[A-Z]\\.)|(^\\([0-9]+\\))");//匹配开始的正则
            tempVal13_2.put("valType","sections");
            tempVal13_2.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)");//匹配结束
                List<Map> templateList13_2=new ArrayList<Map>();
                Map tempVal13_2_1=new HashMap();
                    tempVal13_2_1.put("tempKey","startV");//对应模板值
                    tempVal13_2_1.put("matchT","(^[A-Z]\\.)|(^\\([0-9]+\\))");//匹配开始的正则
                    tempVal13_2_1.put("valType","rowset");//值类型
                    tempVal13_2_1.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                    tempVal13_2_1.put("matchI","(^[A-Z]\\..+)|(^\\([0-9]+\\).+)");//匹配开始()里是要的值 B....
                    tempVal13_2_1.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)");//匹配结束
                    tempVal13_2_1.put("matchEndTable","true");//结束标记是否匹配表头
                    tempVal13_2_1.put("isChangeIndex","true");//改变i为当前行
                    tempVal13_2_1.put("isCompSpace","true");
                templateList13_2.add(tempVal13_2_1);
                Map tempVal13_2_2=new HashMap();
                    tempVal13_2_2.put("tempKey","tablee");//对应模板值
                    tempVal13_2_2.put("valType","table");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
                    tempVal13_2_2.put("matchEndTable","true");//结束标记是否匹配表头
                        //表头同当前表头直接赋当前表里面
                        //若有多表紧挨,临时:再如{{#tablee2}},后期改用模板,动态生成多个表
                    tempVal13_2_2.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)|(^[A-Z]+:)|(^Refer)|(^ON )");//匹配结束
                    tempVal13_2_2.put("isChangeIndex","true");//改变i为当前行
                templateList13_2.add(tempVal13_2_2);
                Map tempVal13_2_3=new HashMap();
                    tempVal13_2_3.put("tempKey","endV");//对应模板值
                    tempVal13_2_3.put("matchT","(^[A-Z]+:)|(^Refer)|(^ON)");//匹配开始的正则
                    tempVal13_2_3.put("valType","rowset");;//值类型
                    tempVal13_2_3.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                    tempVal13_2_3.put("matchI","(^[A-Z]+: .+)|(^Refer .+)|(^ON .+)");//匹配开始()里是要的值
                    tempVal13_2_3.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)");//匹配结束
                    tempVal13_2_3.put("isChangeIndex","true");//改变i为当前行
                templateList13_2.add(tempVal13_2_3);
            tempVal13_2.put("templateList",templateList13_2);
            templateList13.add(tempVal13_2);
        mapRule13.put("templateList",templateList13);
        ruleCRJ.add(mapRule13);
        //valType 类型是table,未匹配表,且匹配结束标记,直接完结表规则
        crjMap.put("rule",ruleCRJ);
        //用空格排版
        Map<String,Integer> spaceRuleCRJ=new HashMap<String,Integer>();
        spaceRuleCRJ.put("^\\d\\. ",0);
        spaceRuleCRJ.put("^[A-Z]\\. ",3);//两个空格
        spaceRuleCRJ.put("^\\(\\d+\\) ",6);//五个空格
        spaceRuleCRJ.put("^\\([a-z]\\) ",9);//八个空格
        crjMap.put("spaceRule",spaceRuleCRJ);
        mapp.put("crj",crjMap);//对crj的整体定义
        //------------BOEING 开始--------------------
        Map<String,Object> boeingMap=new HashMap<String,Object>();
        boeingMap.put("temp","taskCardBoeingT.docx");//模板名称
        boeingMap.put("imageW",660);//图片宽
        boeingMap.put("imageH",960);//图片高
        //页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意 analyPdfM 没值时图片数据先不赋进去))
        //下根据 indexOf 去匹配的
        Map<String, Integer> pageTypeBM=new LinkedCaseInsensitiveMap();
        pageTypeBM.put("MECHINSP",2);
        pageTypeBM.put("(Continued)",2);
        pageTypeBM.put("TAILNUMBERWORKAREA",1);
        pageTypeBM.put("\\d+-\\d+-\\d+-\\d+[A-Z]\\.",3);
        //pageTypeBM.put("AIRLINECARDNO",9);//图片
        boeingMap.put("pageType",pageTypeBM);

        List<Map<String,Object>> ruleBoeing=new ArrayList<Map<String,Object>>();
        Map<String, Object> boRule1=new HashMap<String, Object>();
        boRule1.put("tempKey","TITLE");//对应模板值
        boRule1.put("matchT","TITLE");//匹配开始的正则
        boRule1.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule1.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule1.put("matchI","(.+)( \\d+-\\d+-\\d+-\\d+)|(.+)");//被提取值正则匹配规则,具名组匹配提值
        boRule1.put("endMatch","DATE TASK");//匹配结束
        ruleBoeing.add(boRule1);
        Map<String, Object> boRule2=new HashMap<String, Object>();
        boRule2.put("tempKey","CARDNUM");//对应模板值
        boRule2.put("matchT","BOEING CARD NO");//匹配开始的正则
        boRule2.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule2.put("matchI","(\\d+-\\d+-\\d+-\\d+)");//被提取值正则匹配规则,具名组匹配提值
        ruleBoeing.add(boRule2);
        Map<String, Object> boRule3=new HashMap<String, Object>();
        boRule3.put("tempKey","TASK");//对应模板值
        boRule3.put("matchT","DATE TASK");//匹配开始的正则
        boRule3.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule3.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule3.put("matchI","(.+)");//被提取值正则匹配规则,具名组匹配提值
        boRule3.put("endMatch","WORK AREA");//匹配结束
        ruleBoeing.add(boRule3);
        Map<String, Object> boRule4=new HashMap<String, Object>();
        boRule4.put("tempKey","WORKAREA");//对应模板值
        boRule4.put("matchT","WORK AREA");//匹配开始的正则
        boRule4.put("indexI",2);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule4.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule4.put("matchI","([A-Z ]+)");//被提取值正则匹配规则,具名组匹配提值
        boRule4.put("endMatch","STATION SKILL");//匹配结束
        boRule4.put("colIndexWay","true");
        boRule4.put("colHeadMatch","WORK AREA");
        ruleBoeing.add(boRule4);
        Map<String, Object> boRule5=new HashMap<String, Object>();
        boRule5.put("tempKey","VERSION");//对应模板值
        boRule5.put("matchT","VERSION THRESHOLD REPEAT");//匹配开始的正则
        boRule5.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule5.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule5.put("matchI","(\\d+\\.\\d+) [A-Z0-9]+ [A-Z]+ [A-Z0-9]+ [A-Z]+|(NOTE)");//被提取值正则匹配规则,具名组匹配提值,只提取第一个匹配的值
        boRule5.put("endMatch","ACCESS ZONE");//匹配结束
        boRule5.put("isAllMatch","true");//每一行值都从匹配规则中提取;//现多行类型是有此功能实现
        boRule5.put("colIndexWay","true");
        boRule5.put("colHeadMatch","VERSION THRESHOLD REPEAT");
        ruleBoeing.add(boRule5);
        Map<String, Object> boRule6=new HashMap<String, Object>();
        boRule6.put("tempKey","THRESHOLD");//对应模板值
        boRule6.put("matchT","VERSION THRESHOLD REPEAT");//匹配开始的正则
        boRule6.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule6.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule6.put("matchI","\\d+\\.\\d+( [A-Z0-9]+ [A-Z]+ )[A-Z0-9]+ [A-Z]+");//被提取值正则匹配规则,具名组匹配提值,只提取第一个匹配的值
        boRule6.put("endMatch","ACCESS ZONE");//匹配结束
        boRule6.put("isAllMatch","true");//每一行值都从匹配规则中提取;//现多行类型是有此功能实现
        boRule6.put("colIndexWay","true");
        boRule6.put("colHeadMatch","VERSION THRESHOLD REPEAT");
        ruleBoeing.add(boRule6);
        Map<String, Object> boRule7=new HashMap<String, Object>();
        boRule7.put("tempKey","REPEAT");//对应模板值
        boRule7.put("matchT","VERSION THRESHOLD REPEAT");//匹配开始的正则
        boRule7.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule7.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule7.put("matchI","\\d+\\.\\d+ [A-Z0-9]+ [A-Z]+( [A-Z0-9]+ [A-Z]+)");//被提取值正则匹配规则,具名组匹配提值,只提取第一个匹配的值
        boRule7.put("endMatch","ACCESS ZONE");//匹配结束
        boRule7.put("isAllMatch","true");//每一行值都从匹配规则中提取;//现多行类型是有此功能实现
        boRule7.put("colIndexWay","true");
        boRule7.put("colHeadMatch","VERSION THRESHOLD REPEAT");
        ruleBoeing.add(boRule7);
        Map<String, Object> boRule8=new HashMap<String, Object>();
        boRule8.put("tempKey","AIRPLANE");//对应模板值
        boRule8.put("matchT","AIRPLANE ENGINE");//匹配开始的正则
        boRule8.put("indexI",2);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule8.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        //若不准,直接写根据飞机型号匹配
        boRule8.put("matchI","(ALL) \\S+$|((\\d+[A-Z]? ?)+)");//被提取值正则匹配规则,具名组匹配提值
        boRule8.put("isAllMatch","true");//每一行值都从匹配规则中提取;//现多行类型是有此功能实现
        boRule8.put("endMatch","ACCESS ZONE");//匹配结束
        boRule8.put("colIndexWay","true");
        boRule8.put("colHeadMatch","AIRPLANE ENGINE");
        ruleBoeing.add(boRule8);
        Map<String, Object> boRule9=new HashMap<String, Object>();
        boRule9.put("tempKey","ENGINE");//对应模板值
        boRule9.put("matchT","AIRPLANE ENGINE");//匹配开始的正则
        boRule9.put("indexI",2);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule9.put("valType","rowset");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule9.put("matchI","ALL(( \\S+)+)$|\\d+[A-Z]?( \\S+)$|( ALL)$");//被提取值正则匹配规则,具名组匹配提值
        boRule9.put("isAllMatch","true");//每一行值都从匹配规则中提取;//现多行类型是有此功能实现
        boRule9.put("endMatch","ACCESS ZONE");//匹配结束
        boRule9.put("colIndexWay","true");
        boRule9.put("colHeadMatch","AIRPLANE ENGINE");
        ruleBoeing.add(boRule9);
        //此获取不准(可能右侧的值,影响获取值所在行)可换为多行试试
        Map<String, Object> boRule91=new HashMap<String, Object>();
        boRule91.put("tempKey","SKILL");//对应模板值
        boRule91.put("matchT","STATION SKILL");//匹配开始的正则
        boRule91.put("indexI",2);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        boRule91.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
        boRule91.put("matchI","([A-Z]+)");//被提取值正则匹配规则,具名组匹配提值
        ruleBoeing.add(boRule91);
        Map<String, Object> boRule10=new HashMap<String, Object>();
        boRule10.put("tempKey","ACCESS");
        boRule10.put("matchT","ACCESS ZONE");
        boRule10.put("indexI",1);
        boRule10.put("valType","rowset");
        boRule10.put("matchI","(.+)");
        boRule10.put("endMatch","[A-Z][a-zA-Z]+ ");
        boRule10.put("colIndexWay","true");
        boRule10.put("colHeadMatch","ACCESS");
        ruleBoeing.add(boRule10);
        Map<String, Object> boRule11=new HashMap<String, Object>();
        boRule11.put("tempKey","ZONE");
        boRule11.put("matchT","ACCESS ZONE");
        boRule11.put("indexI",1);
        boRule11.put("valType","rowset");
        boRule11.put("matchI","(.+)");
        boRule11.put("endMatch","[A-Z][a-zA-Z]+ ");
        boRule11.put("colIndexWay","true");
        boRule11.put("colHeadMatch","ZONE");
        boRule11.put("isChangeIndex","true");//改变i为当前行
        ruleBoeing.add(boRule11);
        Map<String, Object> boRule12=new HashMap<String, Object>();
        boRule12.put("tempKey","CONTENT");
        boRule12.put("matchT","[A-Z][a-zA-Z]+ ");
        boRule12.put("indexI",0);
        boRule12.put("valType","rowset");
        boRule12.put("matchI","(.+)");
        boRule12.put("endMatch","A\\. ");
        boRule12.put("isCompSpace","true");
        ruleBoeing.add(boRule12);
        Map<String, Object> boRule13=new HashMap<String, Object>();
        boRule13.put("tempKey","TABLETEMP");//对应模板值
        boRule13.put("matchT","^[A-Z]\\. [A-Z]");//匹配开始的正则
        boRule13.put("endMatch","^TASK \\S+-\\S+-\\S+-\\S+-\\S+$|^EWIS$");//模板匹配结束,遇到结束才允许删这条匹配规则(就因为有多个才用模板)
        boRule13.put("valType","sections");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections
        boRule13.put("isFirstS","true");//是否是首区块对
        List<Map> temListB13=new ArrayList<Map>();
            Map tempMapB13_1=new HashMap();
            tempMapB13_1.put("tempKey","STARTV");//对应模板值
            tempMapB13_1.put("matchT","^[A-Z]\\. [A-Z]");
            tempMapB13_1.put("valType","rowset");;//值类型
            tempMapB13_1.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
            tempMapB13_1.put("matchI","(.+)");//匹配开始()里是要的值
            tempMapB13_1.put("endMatch","^[A-Z]\\. [A-Z]|^TASK \\S+-\\S+-\\S+-\\S+-\\S+$|^EWIS$");//匹配结束
            tempMapB13_1.put("isChangeIndex","true");//改变i为当前行
            tempMapB13_1.put("matchEndTable","true");//结束标记是否匹配表头
            tempMapB13_1.put("isCompSpace","true");//是否校验排版规则,给前面赋值空格实现排版
            temListB13.add(tempMapB13_1);
            Map tempMapB13_2=new HashMap();
            tempMapB13_2.put("tempKey","TABLEE");//对应模板值
            tempMapB13_2.put("valType","table");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
            tempMapB13_2.put("matchEndTable","true");//结束标记是否匹配表头
            tempMapB13_2.put("endMatch","^[A-Z]\\. [A-Z]|^TASK \\S+-\\S+-\\S+-\\S+-\\S+$|^EWIS$");//匹配结束
            tempMapB13_2.put("isChangeIndex","true");//改变i为当前行
            temListB13.add(tempMapB13_2);
        boRule13.put("templateList",temListB13);
        ruleBoeing.add(boRule13);
        Map<String, Object> boRule14=new HashMap<String, Object>();
        boRule14.put("tempKey","TASKNTEMP");//对应模板值
        boRule14.put("matchT","\\S");//匹配开始的正则
        boRule14.put("valType","sections");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections
        boRule14.put("isFirstS","true");//是否是首区块对
        List<Map> temListB14=new ArrayList<Map>();
            Map tempMapB14_1=new HashMap();
            tempMapB14_1.put("tempKey","STAT");//对应模板值
            tempMapB14_1.put("matchT","\\S");
            tempMapB14_1.put("valType","rowset");;//值类型
            tempMapB14_1.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
            tempMapB14_1.put("matchI","(.+)");//匹配开始()里是要的值
            tempMapB14_1.put("isChangeIndex","true");//改变i为当前行
            tempMapB14_1.put("endMatch","^Table \\d+|^\\(Continued\\)");//匹配结束
            tempMapB14_1.put("matchEndTable","true");//结束标记是否匹配表头
            tempMapB14_1.put("matchEndOver","true"); //匹配matchT后,先不往下进行,此时匹配结束标记,匹配则直接结束
            tempMapB14_1.put("isCompSpace","true");//空格控制缩进
            Map<String,String> repFirB14_1=new HashMap<String,String>();
            repFirB14_1.put("^WARNING ","");
            repFirB14_1.put("^CAUTION ","");
            tempMapB14_1.put("replaceV",repFirB14_1); //实现替换功能
            temListB14.add(tempMapB14_1);
            Map tempMapB14_2=new HashMap();
            tempMapB14_2.put("tempKey","TABLET");//对应模板值
            tempMapB14_2.put("valType","table");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
            tempMapB14_2.put("matchEndTable","true");//结束标记是否匹配表头
            tempMapB14_2.put("endMatch","^SUBTASK \\S+-\\S+-\\S+-\\S+-\\S+$|^TASK \\S+-\\S+-\\S+-\\S+-\\S+$|END OF TASK|^\\([a-z0-9]+\\)|^[A-Z]\\. |^EWIS$|^NOTE:");//匹配结束
            tempMapB14_2.put("isChangeIndex","true");//改变i为当前行
            tempMapB14_2.put("continueMatch","true");//未匹配此跳过继续匹配下一个
            temListB14.add(tempMapB14_2);

            Map tempMapB14_3=new HashMap();
            tempMapB14_3.put("tempKey","IMAGET");//对应模板值
            tempMapB14_3.put("matchT","^Table \\d+");
            tempMapB14_3.put("superaddMatchT","^\\S+Table \\d+");
            tempMapB14_3.put("superaddIndexI",-1);
            tempMapB14_3.put("superaddType","add");
            tempMapB14_3.put("indexI",0);//图片从行哪开始
            tempMapB14_3.put("valType","image");//值类型:图片image
            tempMapB14_3.put("matchEndTable","true");//结束标记是否匹配表头
            tempMapB14_3.put("endMatch","^SUBTASK \\S+-\\S+-\\S+-\\S+-\\S+$|END OF TASK|^\\([a-z0-9]+\\)|^[A-Z]\\. |^EWIS$|^NOTE:");//匹配结束
            tempMapB14_3.put("isChangeIndex","true");//改变i为当前行
            tempMapB14_3.put("noMatchTOver","true");
            temListB14.add(tempMapB14_3);

        boRule14.put("templateList",temListB14);
        ruleBoeing.add(boRule14);

        //匹配规则绑定
        boeingMap.put("rule",ruleBoeing);
        //表规则绑定(Boeing 解析table 时若遇到 (Continued) 跳过进行下一行的解析)
        Map<String,Map> tableBoeing=new HashMap<String,Map>();
        Map<String,Object> tableB1=new HashMap<String,Object>();
        List<String> tableMB1=new ArrayList<String>();
        tableMB1.add("(AMM \\S+ )(.+)");
        tableMB1.add("(FIM [0-9\\-]+ TASK [0-9]+ )(.+)");
        tableMB1.add("(SWPM \\S+ \\d+ )(.+)");
        tableMB1.add("(SWPM \\d+, Standard Wiring )(.+)");
        tableMB1.add("(SWPM \\S+ )(.+)");
        tableMB1.add("(WDM \\S+ )(.+)");
        tableMB1.add("()(.*\\([^\\(\\)]+\\).*)");
        tableMB1.add("(\\S+)(.+)?");
        tableB1.put("colMatch",tableMB1);//列值获取方式
        tableB1.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并(有第二列空的情况)
        tableB1.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("Reference Title",tableB1);//refer

        Map<String,Object> tableB2=new HashMap<String,Object>();
        List<String> tableMB2=new ArrayList<String>();
        //B-2 可在列3
        tableMB2.add("([0-9A-Z]+ )?((\\S[^A-Z \\-]*$|\\S[^A-Z \\-]* |\\S-\\S[^A-Z ]+ ?|[A-Z][a-z]+-[A-Z][a-z]+ ?|BMS |NSBT ?|GPL\\S+ ?|DC-\\S+ ?|MS\\S+ ?)+)([A-Z]{1}[0-9A-Z\\-]+.*)?");
        List<Integer> setMatB2 = Arrays.asList(1, 2, 4);
        tableB2.put("setMat",setMatB2);//列与具名组匹配对应规则;
        tableB2.put("colMatch",tableMB2);//列值获取方式
        tableB2.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableB2.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("Reference Description Specification",tableB2);//mater

        Map<String,Object> tableB3=new HashMap<String,Object>();
        List<String> tableMB3=new ArrayList<String>();
        tableMB3.add("([A-Z]+-[0-9]+ )?(.+)");
        tableB3.put("colMatch",tableMB3);//列值获取方式
        tableB3.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableB3.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("Reference Description",tableB3);//tool

        Map<String,Object> tableB4=new HashMap<String,Object>();
        List<String> tableMB4=new ArrayList<String>();
        tableMB4.add("(\\S+)( .+)");
        tableB4.put("colMatch",tableMB4);//列值获取方式
        tableB4.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableB4.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("Number Name/Location",tableB4);

        Map<String,Object> tableB5=new HashMap<String,Object>();
        List<String> tableMB5=new ArrayList<String>();
        tableMB5.add("(\\d+ )(.+)(\\S+-\\S+-\\S+-\\S+-\\S+ )(.+)");
        tableMB5.add("()()(\\S+-\\S+-\\S+-\\S+-\\S+ )(.+)");
        tableMB5.add("(\\d+ )([A-Z][a-z ]+)([A-Z][a-z ]+)()");
        tableMB5.add("()()()(.+)");
        tableB5.put("colMatch",tableMB5);//列值获取方式
        tableB5.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并(可根据逗号判断是否是同一个 AIPC Reference)
        tableB5.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("AMM_Item Description AIPC_Reference AIPC_Effectivity",tableB5);

        Map<String,Object> tableB6=new HashMap<String,Object>();
        List<String> tableMB6=new ArrayList<String>();
        tableMB6.add("^(BEJ .+)()()()");  //后期最好改成合并单元格的写法
        tableMB6.add("(\\S+ )(\\S+ )(\\S+ )(.+)");
        tableB6.put("colMatch",tableMB6);//列值获取方式
        tableB6.put("valNVL","add");
        tableB6.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("Row Col Number Name",tableB6);

        /*Map<String,Object> tableB7=new HashMap<String,Object>();
        List<String> tableMB7=new ArrayList<String>();
        tableMB7.add("([A-Z][A-Z0-9]+ )?([A-Z][A-Z0-9]+ )?([0-9\\- ,]+)?([A-Z].+)?");
        tableB7.put("colMatch",tableMB7);//列值获取方式
        tableB7.put("valNVL","add");
        tableB7.put("indBlankAdd",0);
        tableB7.put("continueVal", Arrays.asList("^\\(Continued\\)$"));
        tableBoeing.put("WIRE_BUNDLE CONNECTOR WDM PNL_OR_MODULE",tableB7);*/

        boeingMap.put("tableRule",tableBoeing);
        //用空格排版(负数表当前行不变,下行再多空格)
        //注意:会默认下一行加3个空格
        Map<String,Integer> spaceRuleBoeing=new HashMap<String,Integer>();
        spaceRuleBoeing.put("^\\d\\. ",0);
        spaceRuleBoeing.put("^[A-Z]\\. ",3);
        spaceRuleBoeing.put("^TASK \\S+-\\S+-\\S+-\\S+-[^\\.\\s]+$",2);
        spaceRuleBoeing.put("^\\(\\d+\\) ",6);
        spaceRuleBoeing.put("^SUBTASK \\S+-\\S+-\\S+-\\S+-\\S+$",6);
        spaceRuleBoeing.put("^\\([a-z]\\) ",9);
        spaceRuleBoeing.put("^\\[0-9]+\\)",12);
        spaceRuleBoeing.put("^(\\S+ )?NOTE:",-3);
        boeingMap.put("spaceRule",spaceRuleBoeing);

        // END OF TASK 解析出来没带---;一个word会有多个 如 TASK 05-55-25-200-804  表其结束
        mapp.put("boeing",boeingMap);//对boeing的整体定义


        /*
        规则汇总
            未实现功能
            autoAddOne //匹配后下标是否自动加1(此功能暂时未写)
           通用
               tempKey //对应模板值
               matchT   //匹配开始的正则
               valType  //值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
               continueMatch    //未匹配此,跳过继续匹配下一个(猜:仅限一级匹配规则和区块对里的多规则)
               noMatchTOver  根据 matchT,就没匹配到内容,直接结束此
           单匹配
               indexI   //需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
               matchI   //被提取值正则匹配规则,具名组匹配提值
           多行匹配
               indexI
               matchI
               endMatch //匹配结束
               isChangeIndex    //改变i为当前行
               isAllMatch   //每一行值都从匹配规则中提取;//现多行类型是有此功能实现
               matchEndTable   //结束标记是否匹配表头
               noMatchIOver
                    //现就多行如此:一开始根据matchI,就没匹配到内容,直接结束此;(此时:matchT一般设为其前必有元素;)
                colIndexWay //现就多行实现此(要和 colHeadMatch 联用);值获取方式根据列去获取(即根据解析出的 rows行数据[下标]去提取值)
                colHeadMatch    //如上用到此;根据空格获取数组,值与行的每一列比对,若在列有此下标记录在集合里,每次取值(默认整行数值)根据多下标提值并拼接返回
                matchEndOver   //匹配matchT后,先不往下进行,此时匹配结束标记,匹配则直接结束
                isCompSpace	//是否校验排版规则,给前面赋值空格实现排版
                replaceV //根据正则替换数值(拨乱反正),("replaceV",Map<String,String> map);
           图片
                indexI  //图片从行哪开始
                matchEndTable
                endMatch
                isChangeIndex
                superaddType 值:all superaddMatchT MatchT 都匹配方为匹配正确 ; add 当MatchT不匹配时的追加匹配
                    superaddMatchT 辅助匹配规则存在,当MatchT不匹配时的二次匹配(配套指定如下)
                    superaddIndexI 相较于当前行和哪行匹配(-1 表上一行)
           复合匹配
                compositeList   //复合的规则
           区块对
                isFirstS    //是否是首区块对
                templateList    //块的规则
                matchEndTable
                endMatch
           table匹配
                matchEndTable
                isChangeIndex
                endMatch
        表规则
            colMatch  //列值获取方式
            valNVL //up 列无值时,取上行值; add 无值时和上行合并
                没设 valNVL, 直接赋值(猜:现默认是,列值都非空是新增数据)(后期可写匹配某成功直接新增)
            setMat //列与具名组匹配对应规则;
            .put("MANUAL_NO REFERENCE DESIGNATION",table3); //key是表列
            continueVal //表匹配后,里内容则匹配此规则直接跳过此行  ("continueVal",List<String> list);
            indBlankAdd  ("indBlankAdd",0)//从0开始  --用于处理列垂直对齐获取数据不准确
                //设置了indBlankAdd 表会去校验单元格是否有垂直对齐的方式,
                    如上设置参是0,则会在校验第一条数据的0列是否是空,直至不是空,记录一下数a(赋给indBlankAddIndex)(是0则不是垂直对齐)
                    当前行的下a条数据的0列有值,表当前数据是新增,否则是合并操作(即使当前行列都有值)
        * */
    }

    public List<Map<String,Object>> getNewRule() {
        Map<String,Object> mMap=mapp.get(fileType);
        List<Map<String,Object>> ruleList=(List)mMap.get("rule");
        String ruleStr=Helper.listToStringJSON(ruleList);
        List<Map<String,Object>> newRule=Helper.stringJSONToList(ruleStr);
        return newRule;
    }

    protected static final Logger logger = Logger.getLogger(FormPdf.class);
    /*把word 的table对象赋给 MAP
     * setMap 被赋值的Map
     * tabMap 表Map 描述集合
     *     tempKey 表的模板名
     *     tabHead 表头值
     *     tabBody 表主体
     */
    public void putTable(Map<String, Object> setMap,Map<String, Object> tabMap,String key){
        //表头值
        String[] tabHead=(String[]) tabMap.get("tabHead");
        //表主体
        List<String[]> tabBody=(List) tabMap.get("tabBody");
        List<RowRenderData> talist=new ArrayList<RowRenderData>();
        RowRenderData row0 = Rows.of(tabHead).textBold().center().create();//颜色即: .bgColor("607D8B") #607D8B
        talist.add(row0);
        for(int i=0;i<tabBody.size();i++){
            String[] strings = tabBody.get(i);
            RowRenderData tablRrow = Rows.of(strings).create();
            talist.add(tablRrow);
        }
        RowRenderData[] rowRenderData = talist.toArray(new RowRenderData[talist.size()]);
        TableRenderData tableRenderData = Tables.of(rowRenderData).mergeRule(null).create();
        TableStyle tableStyle=new TableStyle();tableStyle.setWidth("100%");
        tableRenderData.setTableStyle(tableStyle);
        setMap.put(key, tableRenderData);
    }
    /**
     * 生成word
     * folderName pdf上传时的文件名,工卡数据放此名文件夹下
     *  表是解析此pdf生成的工卡word
     * @author 子火
     * @Date 2021-01-07
     * @return  ReturnClass
     */
    public ReturnClass cWordT(Map<String,Object> analyPdfM)throws Exception{
        ReturnClass reC=Help.returnClassT(200,"生成word成功","");
        String filePath = ResourceUtils.getURL("classpath:").getPath();//D:/SpringCloud/predict/target/classes/
        // 模板路径 //实际地址 target\classes\META-INF\resources\wordtemplate
        Map<String,Object> tMap=mapp.get(fileType);
        //主模板名称
        String mainNameT=(String)tMap.get("temp");
        int imageW=(Integer)tMap.get("imageW");//图片宽
        int imageH=(Integer)tMap.get("imageH");//图片高
        filePath=filePath+"META-INF/resources/wordtemplate/";
        String templatePath = filePath+mainNameT;
        //System.out.println(filePath);
        XWPFTemplate template = XWPFTemplate.compile(templatePath);
        Map<String, Object> params = new HashMap<String, Object>();
        // 普通文本赋值|table|图片的赋值
        Map<String,String> vallMap=(Map)analyPdfM.get("vall");
        for(String key:vallMap.keySet()){
            String value = vallMap.get(key);
            if("table_".equals(value.substring(0,6>value.length()?value.length():6))){
                //说明是table表
                Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");//value是对表的说明,key是UUID,如 table_uuid值
                if(tableMap.containsKey(value)){
                    Map tabMap = tableMap.get(value);
                    //表的模板名
                    String tempKey=(String) tabMap.get("tempKey");
                    putTable(params,tabMap,tempKey);
                }else{
                    params.put(key, value);
                }
            }else if("imageSingle_".equals(value.substring(0,12>value.length()?value.length():12))){
                //说明是图片
                //str值例 imageSingle_IMW:100IMH:200IMEND;图片值
                strToImage(value,key,params);
            }else{
                params.put(key, value);
            }
        }
        // 图片赋值
        List<String> imagesB=(List)analyPdfM.get("imagesB");//元素是base64
        if(imagesB.size()>0){
            List<Map> subData = new ArrayList<Map>();
            for(int i=0;i<imagesB.size();i++){
                String base64 = imagesB.get(i);
                byte[] bytes =Helper.base64ToByte(base64);
                PictureRenderData pictureRenderData = Pictures.ofBytes(bytes, PictureType.PNG).size(imageW, imageH).create();
                Map s1 = new HashMap();
                s1.put("image", pictureRenderData);
                subData.add(s1);
            }
            params.put("images", new DocxRenderData(new File(filePath+"imageT.docx"), subData));
        }
        //区块对赋值
        Map<String,List> sectionsMap=(Map)analyPdfM.get("sections");
        for(String key:sectionsMap.keySet()){
            List list = sectionsMap.get(key);
            forListToTable(list,analyPdfM);
            params.put(key, list);
        }
        // 模板赋值
        template.render(params);
        //保存所在文件夹
        String saveUrl=(String) analyPdfM.get("saveUrl");
        //保存后文件名
        String saveName=(String) analyPdfM.get("saveName");
        template.writeToFile(saveUrl+"/"+saveName+".docx");
        template.close();
        return reC;
    }
    /**
     * 保存图片数据值进 analyPdfM
     */
    public void saveImageData(Map<String, Object> analyPdfM,PDDocument document,int pageN)throws Exception{
        PDFRenderer renderer = new PDFRenderer(document);
        //参1:页数(从0开始),参2:设800(也可为100等猜:影响图片的清晰度,越小越模糊体积越小)
        BufferedImage image = renderer.renderImageWithDPI((pageN-1),200);
        //BufferedImage -> byte[]
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] bytes = os.toByteArray();
        os.close();
        String base64 = Helper.byteToBase64(bytes);
        List<String> imagesB=(List<String>) analyPdfM.get("imagesB");
        imagesB.add(base64);
        //System.out.println(Helper.listToStringJSON(imagesB));
    }
    /**
     * 关键数据入库
     * folderName pdf上传时的文件名,工卡数据放此名文件夹下
     * @author 子火
     * @Date 2021-01-07
     * @return  ReturnClass
     */
    public ReturnClass saveToDatabase(Map<String,Object> analyPdfM,Integer AMM_FILE_ID,String folderName,JdbcTemplate jdbcTemplate)throws Exception{
        ReturnClass reC=Help.returnClassT(200,"数据入库成功","");
        //crj_card 表主键
        String CARD_ID="";
        //配置文件值获取
        ResourceBundle re = java.util.ResourceBundle.getBundle("application");//application.properties里值
        String saveMain = re.getString("saveurl.main");
        String saveExtend = re.getString("saveurl.taskcard.extend");
        //保存后的文件夹位置(要事先存在)
        String saveUrl=saveMain+saveExtend+folderName;
        // 创建文件夹
        File file = new File(saveUrl);
        if (!file.exists()) {
            file.mkdirs();
        }
        analyPdfM.put("saveUrl",saveUrl);
        String uuidd = UUID.randomUUID().toString();
        analyPdfM.put("UUID",uuidd);
        if("crj".equals(fileType)){
            // 普通文本赋值|table的赋值
            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
            String crjCardSql ="insert into crj_card\n" +
                    "(AMM_FILE_ID,UNIQUE_IDENTIFIER,WORD_PATH,AIRCRAFT_SERIES,\n" +
                    "MANUAL_REV,AMENDMENT,TASK_CARD_NUMBER,TASK_TYPE,SKILL,\n" +
                    "LABOR_HOURS,NBR_PERSONS,ZONES,AIRCRAFT_EFFECTIVITY,\n" +
                    "TASK_DESCRIPTION,REFERENCE,MRM_REFERENCE,NOTE,VERSION_DATE)\n" +
                    "values ("+AMM_FILE_ID+",'"+uuidd+"','"+saveUrl+"','"+
                    Helper.nvlString(vallMap.get("AircraftSeries"))+"','"+
                    Helper.nvlString(vallMap.get("Rev"))+"','"+
                    Helper.nvlString(vallMap.get("Amend"))+"','"+
                    Helper.nvlString(vallMap.get("TaskCardNumber"))+"','"+
                    Helper.nvlString(vallMap.get("TaskType"))+"','"+
                    Helper.nvlString(vallMap.get("Skill"))+"','"+
                    Helper.nvlString(vallMap.get("LaborHours"))+"','"+
                    Helper.nvlString(vallMap.get("NbrOfPersons"))+"','"+
                    Helper.nvlString(vallMap.get("Zones"))+"','"+
                    Helper.nvlString(vallMap.get("Effectivity"))+"','"+
                    Helper.nvlString(vallMap.get("Description"))+"','"+
                    Helper.nvlString(vallMap.get("Reference"))+"','"+
                    Helper.nvlString(vallMap.get("MrmReference"))+"','"+
                    Helper.nvlString(vallMap.get("Note"))+"','"+
                    analyPdfM.get("VERSION_DATE")+"')";
            jdbcTemplate.update(crjCardSql);
            //主键
            CARD_ID = String.valueOf(getCRJKey(uuidd,jdbcTemplate));

            //当前表数据提取
            Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");
            //区块对赋值
            Map<String,List> sectionsMap=(Map)analyPdfM.get("sections");
            Map<String,String> umap=new HashMap();
            getCRJTableKey(umap,sectionsMap);
            String toolStr = umap.get("toolStr");
            String materialsStr = umap.get("materialsStr");
            String referenceStr = umap.get("referenceStr");
            if(StringUtils.isNotBlank(toolStr)){
                //表描述:value是对表的说明,key是UUID,如 table_uuid值
                Map<String,Object> tablem=tableMap.get(toolStr);
                if(tablem!=null){
                    //表主体
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            String toolSql ="insert into crj_card_tool(CRJ_CARD_ID,REFERENCE,DESIGNATION) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"')";
                            jdbcTemplate.update(toolSql);
                        }
                    }
                }
            }
            if(StringUtils.isNotBlank(materialsStr)){
                Map<String,Object> tablem=tableMap.get(materialsStr);
                if(tablem!=null){
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            String materialsSql ="insert into crj_card_materials(CRJ_CARD_ID,REFERENCE,DESIGNATION) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"')";
                            jdbcTemplate.update(materialsSql);
                        }
                    }

                }
            }
            if(StringUtils.isNotBlank(referenceStr)){
                Map<String,Object> tablem=tableMap.get(referenceStr);
                if(tablem!=null){
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            String string2 ="";
                            if(strings.length>2){
                                string2 = strings[2];
                            }
                            String referenceSql ="insert into crj_card_reference(CRJ_CARD_ID,MANUAL_NO,REFERENCE,DESIGNATION) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"','"+string2.replaceAll("'","''")+"')";
                            jdbcTemplate.update(referenceSql);
                        }
                    }
                }
            }
        }else if("boeing".equals(fileType)) {
            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
            String cardSql ="insert into boeing_card " +
                    "(AMM_FILE_ID,UNIQUE_IDENTIFIER,WORD_PATH," +
                    "TITLE,CARDNUM,TASK,WORKAREA," +
                    "VERSION,THRESHOLD,REPEAT_T," +
                    "AIRPLANE,ENGINE_T,ACCESS,ZONE_T,VERSION_DATE,SKILL) " +
                    "values("+AMM_FILE_ID+",'"+uuidd+"','"+saveUrl+"','"+
                    Helper.nvlString(vallMap.get("TITLE"))+"','"+
                    Helper.nvlString(vallMap.get("CARDNUM"))+"','"+
                    Helper.nvlString(vallMap.get("TASK"))+"','"+
                    Helper.nvlString(vallMap.get("WORKAREA"))+"','"+
                    Helper.nvlString(vallMap.get("VERSION"))+"','"+
                    Helper.nvlString(vallMap.get("THRESHOLD"))+"','"+
                    Helper.nvlString(vallMap.get("REPEAT"))+"','"+
                    Helper.nvlString(vallMap.get("AIRPLANE"))+"','"+
                    Helper.nvlString(vallMap.get("ENGINE"))+"','"+
                    Helper.nvlString(vallMap.get("ACCESS"))+"','"+
                    Helper.nvlString(vallMap.get("ZONE"))+"','"+
                    analyPdfM.get("VERSION_DATE")+"','"+
                    Helper.nvlString(vallMap.get("SKILL"))+"')";
            jdbcTemplate.update(cardSql);
            //主键
            CARD_ID = String.valueOf(getBOEINGKey(uuidd,jdbcTemplate));
            //当前表数据提取
            Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");
            //区块对赋值
            Map<String,List> sectionsMap=(Map)analyPdfM.get("sections");
            Map<String,String> umap=new HashMap();
            getBoeingTableKey(umap,sectionsMap);
            String toolStr = umap.get("toolStr");
            String materialsStr = umap.get("materialsStr");
            String referenceStr = umap.get("referenceStr");
            if(StringUtils.isNotBlank(toolStr)){
                //表描述:value是对表的说明,key是UUID,如 table_uuid值
                Map<String,Object> tablem=tableMap.get(toolStr);
                if(tablem!=null){
                    //表主体
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            String toolSql ="insert into boeing_card_tool(BOEING_CARD_ID,REFERENCE,DESCRIPTION) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"')";
                            jdbcTemplate.update(toolSql);
                        }


                    }
                }
            }
            if(StringUtils.isNotBlank(materialsStr)){
                Map<String,Object> tablem=tableMap.get(materialsStr);
                if(tablem!=null){
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            String string2 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            if(strings.length>2){
                                string2 = strings[2];
                            }
                            String materialsSql ="insert into boeing_card_materials(BOEING_CARD_ID,REFERENCE,DESCRIPTION,SPECIFICATION) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"','"+string2.replaceAll("'","''")+"')";
                            jdbcTemplate.update(materialsSql);
                        }
                    }

                }
            }
            if(StringUtils.isNotBlank(referenceStr)){
                Map<String,Object> tablem=tableMap.get(referenceStr);
                if(tablem!=null){
                    List<String[]> tabBody=(List)tablem.get("tabBody");
                    for(int i=0;i<tabBody.size();i++){
                        String[] strings = tabBody.get(i);
                        if(strings.length>0){
                            String string0 = strings[0];
                            String string1 ="";
                            if(strings.length>1){
                                string1 = strings[1];
                            }
                            String referenceSql ="insert into boeing_card_reference(BOEING_CARD_ID,REFERENCE,TITLE) values ("+CARD_ID+",'"+string0.replaceAll("'","''")+"','"+string1.replaceAll("'","''")+"')";
                            jdbcTemplate.update(referenceSql);
                        }
                    }
                }
            }

        }
        //工卡主键做表名
        analyPdfM.put("saveName",CARD_ID);
        return reC;
    }
    public void getCRJTableKey(Map<String,String> umap,Object obj){
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object value:map.values()){
                if(value instanceof Map||(value instanceof List)){
                    getCRJTableKey(umap,value);
                }
            }
            String startV = (String)map.get("startV");
            if(StringUtils.isNoneBlank(startV)){
                int toolI = startV.indexOf("Tools and Equipment");
                int materialsI = startV.indexOf("Consumable Materials");
                int informationI = startV.indexOf("Reference Information");
                String tableeU = (String)map.get("tablee");
                if(toolI!=-1&&StringUtils.isBlank(umap.get("toolStr"))){
                    umap.put("toolStr",tableeU);
                }else if(materialsI!=-1&&StringUtils.isBlank(umap.get("materialsStr"))){
                    umap.put("materialsStr",tableeU);
                }else if(informationI!=-1&&StringUtils.isBlank(umap.get("referenceStr"))){
                    umap.put("referenceStr",tableeU);
                }
            }
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    getCRJTableKey(umap,value);
                }
            }
        }
    }
    public void getBoeingTableKey(Map<String,String> umap,Object obj){
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object value:map.values()){
                if(value instanceof Map||(value instanceof List)){
                    getBoeingTableKey(umap,value);
                }
            }
            String startV = (String)map.get("STARTV");
            if(StringUtils.isNoneBlank(startV)){
                int toolI = startV.indexOf(". Tools/Equipment");
                int materialsI = startV.indexOf(". Consumable Materials");
                int informationI = startV.indexOf(". References");
                String tableeU = (String)map.get("TABLEE");
                if(toolI!=-1&&StringUtils.isBlank(umap.get("toolStr"))){
                    umap.put("toolStr",tableeU);
                }else if(materialsI!=-1&&StringUtils.isBlank(umap.get("materialsStr"))){
                    umap.put("materialsStr",tableeU);
                }else if(informationI!=-1&&StringUtils.isBlank(umap.get("referenceStr"))){
                    umap.put("referenceStr",tableeU);
                }
            }
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    getBoeingTableKey(umap,value);
                }
            }
        }
    }
    //根据唯一标识获取主键值
    public int getCRJKey(String uuid,JdbcTemplate jdbcTemplate){
        Integer ki=0;
        String getSql="SELECT\n" +
                "f.CRJ_CARD_ID\n" +
                "FROM\n" +
                "crj_card AS f\n" +
                "WHERE\n" +
                "f.UNIQUE_IDENTIFIER = '"+uuid+"' ";
        List<Map<String, Object>> re=jdbcTemplate.queryForList(getSql);
        if(re.size()>0){
            Map<String, Object> stringObjectMap = re.get(0);
            ki=(Integer)stringObjectMap.get("CRJ_CARD_ID");
        }
        return ki;
    }
    //根据唯一标识获取主键值
    public int getBOEINGKey(String uuid,JdbcTemplate jdbcTemplate){
        Integer ki=0;
        String getSql="SELECT\n" +
                "f.BOEING_CARD_ID AS IDD\n" +
                "FROM\n" +
                "BOEING_CARD AS f\n" +
                "WHERE\n" +
                "f.UNIQUE_IDENTIFIER = '"+uuid+"' ";
        List<Map<String, Object>> re=jdbcTemplate.queryForList(getSql);
        if(re.size()>0){
            Map<String, Object> stringObjectMap = re.get(0);
            ki=(Integer)stringObjectMap.get("IDD");
        }
        return ki;
    }
    /**
     * 解析PDF
     * @author 子火
     * @Date 2021-01-08
     */
    public void analyPdf(Page page,Map<String,Object> analyPdfM,int pageTypeN,List<Map<String,Object>> ruleList,PDDocument document,int pageN){
        //根据流的方式去获取数据
        BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
        List<Table> talist=sea.extract(page);
        //循环table(用流方式获取,此一般就一个)
        Table table=talist.get(0);
        //获取页面数据结构,元素是行数据
        List<List<String>> rows=new ArrayList<List<String>>();
        List<List<RectangularTextContainer>> tableRows = table.getRows();
        for (int j = 0; j < tableRows.size(); j++) {
            List<RectangularTextContainer> row = tableRows.get(j);
            List<String> rowscol=new ArrayList<String>();
            for (int jj = 0; jj < row.size(); jj++) {
                rowscol.add(row.get(jj).getText());
            }
            rows.add(rowscol);
        }
        //临时数据
        Map temporaryMap=new HashMap();
        //头部数据初始位置
        int initI = getInitI(rows, pageTypeN);
        if(pageTypeN==1){
            //初始赋值
            List<String> imagesB=new ArrayList<String>();//元素是base64
            analyPdfM.put("imagesB",imagesB);
            Map<String,String> vallMap=new HashMap<>();
            analyPdfM.put("vall",vallMap);
            Map<String,List> sectionsMap=new HashMap<>();//此元素里的map,value若对应下的key,说明是个表
            analyPdfM.put("sections",sectionsMap);
            Map<String,Map> tableMap=new HashMap<>();//value是对表的说明,key是UUID,值如 table_uuid
                //表的模板名  tempKey - String
                //表头值  tabHead - String[]
                //表主体  tabBody - List<String[]>
            analyPdfM.put("tableMap",tableMap);
            //版本日期(即第一页的右下角的日期)
            String VERSION_DATE="";
            String pa;
            int inDown;
            if("boeing".equals(fileType)){
                inDown=2;
                pa="[A-Z][a-z]+ \\d+\\/\\d+$";
            }else{
                inDown=1;
                pa="[A-Z][a-z]+ \\d+\\/\\d+$";
            }
            //行数据
            int size = rows.size();
            if(size>inDown){
                size=size-inDown;
            }
            String rowV=getRowSte(rows,size,initI);
            //System.out.println(rowV);
            Pattern pattern = Pattern.compile(pa);
            Matcher matcher = pattern.matcher(rowV);
            if(matcher.find()){
                VERSION_DATE= matcher.group(0);
            }
            analyPdfM.put("VERSION_DATE",VERSION_DATE);
        }
        //下标记录(初始赋值)
        temporaryMap.put("index",initI);
        //当前页的行数据
        temporaryMap.put("rows",rows);
        //初始有效行数
        temporaryMap.put("initI",initI);
        //当前页的解析数据输出
        int ii=initI;
        while(ii<rows.size()){
            ii=(int)temporaryMap.get("index");
            //行数据
            String rowV=getRowSte(rows,ii,initI);
            //是否是尾的垃圾数据
            boolean endrow = isEndrow(rowV);
            if(endrow){
                break;
            }
            if(StringUtils.isBlank(rowV)){
                //更改当前下标(表)
                setIndex(temporaryMap,ii+1);
                continue;
            }
            //对数据的处理
            //System.out.println(rowV);
            matchRule(temporaryMap,ruleList,analyPdfM,document,pageN);
        }
        //System.out.println("-------------------------------");
    }
    //操作下标
    public void matchRule(Map temporaryMap,List<Map<String,Object>> ruleList,Map<String,Object> analyPdfM,PDDocument document,int pageN){
        //现循环所在的行数
        int index=(int)temporaryMap.get("index");
        //下次循环的行数
        index=index+1;
        //匹配提取值的规则
        for(int i=0;i<ruleList.size();i++){
            Map<String, Object> mapRule =ruleList.get(i);
            if("true".equals(mapRule.get("alreadyOver"))){//失效的匹配规则过滤
                continue;
            }
            //当前匹配完成则 mapRule.size() 会为0-会继续匹配下一个规则;
            // (未完,已完,已开始未完结)
            //匹配规则已开始未完结,则  mapRule.put("donotEnd","true");
            matchStr(document,pageN,mapRule,temporaryMap,analyPdfM,null);
            //完结当前规则时,当前行继续匹配下一个规则
            if("true".equals(mapRule.get("alreadyOver"))){
                continue;
            }else{
                //当前规则未完结,或未匹配到
                String continueMatch=(String)mapRule.get("continueMatch");
                if("true".equals(continueMatch)){ //设置true 则继续匹配规则
                    continue;
                }else{ //默认:已开始未完结,或未匹配到 直接break规则,获取下一行数据匹配规则去
                    break;
                }
            }
        }
        setIndex(temporaryMap,index);
        //return index;
    }
    //更改当前下标
    public void setIndex(Map temporaryMap,int index){
        Integer nowI= (Integer)temporaryMap.get("index");
        if(nowI==null){
            nowI=0;
        }
        //更新行下标
        if(index>nowI){//防止死循环
            temporaryMap.put("index",index);
        }
    }
    //获取字符串括号没关个数(-2说明缺两个开)
    public int bracketM(String str){
        int n=0;
        for (int i = 0; i < str.length(); i++) {
            char  item =  str.charAt(i);
            if('('==item){
                n++;
            }else if(')'==item){
                n--;
            }
        }
        return n;
    }


    /**
     * 累加行数据
     * @param presentStr 操作数组
     * @param arrI  操作下标
     * @param strP1 原值
     * @param strN  累加值
     */
    public void addRowData(String[] presentStr,int arrI,String strP1,String strN){
        if(StringUtils.isNotBlank(strN)){
            if(StringUtils.isNotBlank(strP1)){
                //累加
                presentStr[arrI]=strP1+"\n"+strN;
            }else{
                presentStr[arrI]=strN;
            }
        }
    }
    //是否有"("在上一行数据,返回true表有
    public boolean isNotC(String[] presentStr){
        boolean isNotC=false;
        for(int arr=0;arr<presentStr.length;arr++){
            //已存值
            String strP=Helper.nvlString(presentStr[arr]);
            //a(b)c( 返回1
            int bracketM = bracketM(strP);
            if(bracketM>0){
                isNotC=true;
                break;
            }
        }
        return isNotC;
    }
    //上无(,下有) //当前列赋给已存行的下一列
    public boolean noOHaveC(int bracketMN,int bracketMP,int array,String[] presentStr,String strN){
        boolean bol=false;
        if(bracketMN<0&&bracketMP==0){
            //防止下标超出
            int arrI=array+1;
            if(arrI>(presentStr.length-1)){
                arrI=presentStr.length-1;
            }
            //已存行的下一列的值
            String strP1=presentStr[arrI];
            //累加数据
            addRowData(presentStr,arrI,strP1,strN);
            bol=true;
        }
        return bol;
    }
    //给 tabBodyStr 赋值,返回是否有空的情况
    public boolean setTabBodyStr(String[] tabBodyStr,List<Integer> setMat,Matcher matcher,int colN) {
        boolean isBlank=false;
        if(setMat!=null&&setMat.size()!=0){
            //直接根据自定义list获取列值
            for(int array=0;array<setMat.size();array++){
                Integer integer = setMat.get(array);
                String group = Helper.nvlString(matcher.group(integer));
                //赋值
                tabBodyStr[array]=group;
                //是否有空的情况
                if(StringUtils.isBlank(group)){
                    isBlank=true;
                }
            }
        }else{
            //直接根据列数获取列值
            for(int array=0;array<colN;array++){
                String group =Helper.nvlString(matcher.group(array+1));
                //赋值
                tabBodyStr[array]=group;
                //是否有空的情况
                if(StringUtils.isBlank(group)){
                    isBlank=true;
                }
            }
        }
        return isBlank;
    }

    //上一行是否包括空
    public String isInblank(int i,Map<Integer,String> map){
        int bi=i-1;
        if(bi<0){
            bi=0;
        }
        String blank =map.get(bi);
        return blank;
    }
    //非空表新增行数据,其余表合并
    public boolean retType(boolean isBlank) {
        if(isBlank){
            return false;
        }else{
            return true;
        }
    }
    //行数据转为表格数据
    public  Map<String,Object> getCols(String rowset,List<String> colMatch,int colN,List<Integer> setMat){
        Map<String,Object> reMap=new HashMap();
        String[] tabBodyStr=null;
        for(int s=0;s<colMatch.size();s++){
            String match = colMatch.get(s);
            Pattern pattern = Pattern.compile(match);
            Matcher matcher = pattern.matcher(rowset);
            if(matcher.find()){
                //行数据
                tabBodyStr=new String[colN];
                //是否有空的情况
                boolean isBlank=false;
                if(setMat!=null&&setMat.size()!=0){
                    //直接根据自定义list获取列值
                    for(int array=0;array<setMat.size();array++){
                        Integer integer = setMat.get(array);
                        String group = Helper.nvlString(matcher.group(integer));
                        //赋值
                        tabBodyStr[array]=group;
                        //是否有空的情况
                        if(StringUtils.isBlank(group)){
                            isBlank=true;
                        }
                    }
                }else{
                    //直接根据列数获取列值
                    for(int array=0;array<colN;array++){
                        String group =Helper.nvlString(matcher.group(array+1));
                        //赋值
                        tabBodyStr[array]=group;
                        //是否有空的情况
                        if(StringUtils.isBlank(group)){
                            isBlank=true;
                        }
                    }
                }
                reMap.put("isBlank",isBlank);
                reMap.put("tabBodyStr",tabBodyStr);
                //匹配后停止往下循环
                break;
            }
        }
        return reMap;
    }

    //table的匹配方法
    public void matchStrTable(Map<String, Object> mapRule,Map temporaryMap,Map<String,Object> analyPdfM,Map sectionsMapT,String matchEndTable,Map<String,Map> tableRule,int index,int initI,List<List<String>> rows,String donotEnd){
        //改变i为当前行
        String isChangeIndex=(String) mapRule.get("isChangeIndex");
        //System.out.println("进入表匹配,开始行:"+index);
        Map<Integer,String> map=new HashMap<Integer,String>();
        for (int i=index;i<rows.size();i++){
            //行数据
            String rowsetV=getRowSte(rows,i,initI);
            //表未完结
            if("true".equals(donotEnd)){
                //是否是尾的垃圾数据
                boolean endrow = isEndrow(rowsetV);
                if(endrow){
                    //更改当前下标(表)
                    setIndex(temporaryMap,i);
                    break;
                }
                //更改当前下标(表)
                setIndex(temporaryMap,i);
                if(StringUtils.isBlank(rowsetV)){
                    continue;
                }
                //有效校验数据结束
                //获取当前table的uuid标记
                String newTableUUID= (String)analyPdfM.get("newTableUUID");
                //当前表数据提取
                Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");
                //表描述:value是对表的说明,key是UUID,如 table_uuid值
                Map<String,Object> tablem=tableMap.get(newTableUUID);
                //表头行数据记录
                String tabROWV=(String)tablem.get("tabROWV");
                //若行匹配跳过的规则,直接跳过
                List<String> continueVal=(List)tablem.get("continueVal");
                if(continueVal!=null&&continueVal.size()>0){
                    boolean bol=false;
                    for(String val: continueVal){
                        Pattern pattern = Pattern.compile(val);
                        Matcher matcher = pattern.matcher(rowsetV);
                        if(matcher.find()){
                            bol=true;
                            break;
                        }
                    }
                    if(bol){
                        continue;
                    }
                }
                //如果内容同当前表的表头,内容紧接着当前表
                if(tabROWV.equals(rowsetV)){
                    continue;
                }else{
                    //匹配结束标记(//结束标记是否匹配表头在下)
                    boolean rsEndMatch=isEndMatch(mapRule,rowsetV);
                    //结束标记是否匹配表头
                    boolean matchEndT=matchTabH(matchEndTable,rowsetV,tableRule);
                    if(rsEndMatch||matchEndT){
                        //触发结束
                        matchEnd(isChangeIndex,i,index,temporaryMap,mapRule);
                        break;
                    }
                }
                //测试
                /*if(rowsetV.indexOf("(2) Open this access panel:")!=-1){
                    System.out.println(rowsetV);
                }*/
                //表头值
                String[] tabHead=(String[])tablem.get("tabHead");
                //列个数
                int colN=tabHead.length;//从1开始
                //表主体
                List<String[]> tabBody=(List)tablem.get("tabBody");
                //列值获取方式
                List<String> colMatch=(List)tablem.get("colMatch");
                //up 列无值时,取上行值; add 无值时和上行合并
                String valNVL=(String)tablem.get("valNVL");
                //列与具名组匹配对应规则;
                List<Integer> setMat=(List)tablem.get("setMat");
                //用于处理列垂直对齐获取数据不准确
                Integer indBlankAdd=(Integer)tablem.get("indBlankAdd"); //从0开始
                Integer indBlankAddIndex=(Integer)tablem.get("indBlankAddIndex"); //从0开始(1表是当前行的下一行)
                String endBlankAdd=(String)tablem.get("endBlankAdd");//为true表计算indBlankAddIndex结束
                //行数据转为表格数据
                Map<String,Object> reMap = getCols(rowsetV, colMatch, colN,setMat);
                if(reMap.size()!=0){
                    //行数据
                    String[] tabBodyStr=(String[])reMap.get("tabBodyStr");
                    //当前行是否有空列
                    boolean isBlank=(boolean)reMap.get("isBlank");
                    if(isBlank){map.put(i,"true");}
                    int size = tabBody.size();
                    //是否直接赋值
                    Boolean directAdd=true;
                    if(size>0){ //不是初始
                        directAdd=false;//是初始直接赋数据,否则需如下处理
                        //值校验是否有垂直对齐的情况,并统计差了多少行,就能补全缺失数据
                        if(indBlankAdd!=null&&!"true".equals(endBlankAdd)){
                            String[] tabBody0 = tabBody.get(0);//获取第一行数据
                            String valIndex = tabBody0[indBlankAdd];
                            if (StringUtils.isBlank(valIndex)){
                                indBlankAddIndex++;
                                tablem.put("indBlankAddIndex",indBlankAddIndex);
                                tablem.put("endBlankAdd","true");
                            }
                        }
                        //最新的一行数据
                        String[] presentStr= tabBody.get(size - 1);
                        //当前最新行数据有开括号即 "("
                        boolean isNotC=isNotC(presentStr);
                        if(isNotC){ //当前最新行数据有开括号即 "("
                            //上一行是否包括空
                            String blank=isInblank(i,map);
                            if(!isBlank&&"true".equals(blank)){ //当前行无空格,上行有空格,是新添数据(防止上数据只有开括号就从来没关过,书写错误导致的无限合并)
                                directAdd=true;//直接add进去,就不用循环列了  A处
                            }
                            //else{ 最新行数据有 "(" ,直接合并值 directAdd=false; }
                        }
                        if(!directAdd){ //和上 A处 对应
                            //循环当前行的每一列
                            for(int array=0;array<tabBodyStr.length;array++){
                                //是否直接赋值
                                //Boolean directAddFor=directAdd;
                                //已存值
                                String strP=presentStr[array];
                                //当前解析列值
                                String strN=tabBodyStr[array];
                                if("up".equals(valNVL)){ //up 列无值时,取上行值
                                    if(StringUtils.isBlank(strN)){
                                        tabBodyStr[array]=strP;
                                    }
                                    directAdd=true;//循环列后直接add即可
                                    continue;
                                }else if("add".equals(valNVL)){ // add 无值时和上行合并
                                    directAdd=false;
                                    if(isNotC){ //当前最新行数据有开括号即 "("
                                        //获取字符串,括号没关个数:-2说明有两个)没匹配到(
                                        int bracketMP = bracketM(strP);
                                        int bracketMN = bracketM(strN);
                                        //上无(,下有) //当前列赋给已存行的下一列
                                        boolean noOHaveC = noOHaveC(bracketMN, bracketMP, array, presentStr, strN);
                                        if(noOHaveC){
                                            continue; //上 noOHaveC 就已经累加过了就不往下走了
                                        }
                                    }
                                    // 设置了indBlankAdd 表会去校验单元格是否有垂直对齐的方式,
                                    if(indBlankAdd!=null){
                                        //未触发必有值行(只有第一次查落差行数,endBlankAdd才会为非true,则必是合并)
                                        if(!"true".equals(endBlankAdd)){
                                            directAdd=false;
                                        }else{ //已经获取到落差行
                                            //已获取因垂直对齐导致的落差行数
                                            if(indBlankAddIndex>0){
                                                Integer ai=i+indBlankAddIndex;
                                                //落差行不能获取到
                                                if(ai>=rows.size()){
                                                    //当落差行所在,超出 rows 的下标,直接累加
                                                    directAdd=false;
                                                }else{
                                                    //获取落差行数据
                                                    String rowsetA=getRowSte(rows,ai,initI);
                                                    //根据行获取列值
                                                    Map<String,Object> reMapc=getCols(rowsetA, colMatch, colN,setMat);
                                                    String[] cols =(String[])reMapc.get("tabBodyStr");
                                                    if(reMapc.size()>0&&cols!=null){
                                                        String valIndex = cols[indBlankAdd];
                                                        //当前行的下的落差行的关键列有值,
                                                        //表当前数据是新增,否则是合并操作(即使当前行列都有值)
                                                        if (StringUtils.isNotBlank(valIndex)){
                                                            directAdd=true;
                                                        }else {
                                                            directAdd=false;
                                                        }
                                                    }
                                                }
                                            }else{ //没有落差行,非空表新增行数据,其余表合并
                                                directAdd=retType(isBlank);//返回false表要累加,true表直接新增行数据
                                            }
                                        }
                                    }else{//不校验单元格是否有垂直对齐
                                        directAdd=retType(isBlank);
                                    }
                                }else{
                                    //没设valNVL,直接赋值
                                    directAdd=true;
                                }
                                if(!directAdd){  //当前循环列值,累加到数据
                                    addRowData(presentStr,array,strP,strN);
                                }
                            } //循环当前行的每一列结束
                                // if(isBlank){ 有空的情况} else {没空直接赋值}
                        }  //最新行数据有开括号即 "(" 结束
                    } //不是初始结束
                    if(directAdd){//默认是直接赋数据(如:是初始直接赋数据)
                        tabBody.add(tabBodyStr);
                    }
                }

            }else{
                //表类型还未正式开启
                Map<String,Object> tablerul=new HashMap<String,Object>();
                String newRow=rowsetV.replaceAll("\\s", "").replaceAll("_", "");
                //表头值
                String[] tabHead=null;
                //遍历表规则
                for(String key:tableRule.keySet()){
                    String newKey=key.replaceAll("\\s", "").replaceAll("_", "");
                    if(newKey.equals(newRow)){//匹配表规则成功
                        tablerul=tableRule.get(key);
                        tabHead=key.split(" ");
                        //此规则开启;
                        mapRule.put("donotEnd","true");
                        donotEnd="true";
                        break;
                    }
                }
                //匹配表规则
                if(tablerul.size()!=0){
                    //System.out.println("表匹配");
                    //对应模板值
                    String tempKey=(String) mapRule.get("tempKey");
                    //当前最新table的uuid标记
                    String uuidS = "table_"+UUID.randomUUID().toString();
                    analyPdfM.put("newTableUUID",uuidS);
                    //表说明
                    Map<String,Object> tablem=new HashMap<String,Object>();
                    //表头行数据记录
                    tablem.put("tabROWV",rowsetV);
                    //表的模板说明
                    tablem.put("tempKey",tempKey);
                    //表头值
                    tablem.put("tabHead",tabHead);
                    //表主体
                    List<String[]> tabBody=new ArrayList<String[]>();
                    tablem.put("tabBody",tabBody);
                    //列值获取方式
                    List<String> colMatch=(List)tablerul.get("colMatch");
                    tablem.put("colMatch",colMatch);
                    //up 列无值时,取上行值; add 无值时和上行合并
                    String valNVL=(String)tablerul.get("valNVL");
                    tablem.put("valNVL",valNVL);
                    //若行匹配直接跳过
                    List<String> continueVal=(List)tablerul.get("continueVal");
                    tablem.put("continueVal",continueVal);
                    //用于处理列垂直对齐获取数据不准确
                    Integer indBlankAdd=(Integer)tablerul.get("indBlankAdd");
                    tablem.put("indBlankAdd",indBlankAdd);
                    if(indBlankAdd!=null){
                        tablem.put("indBlankAddIndex",0);
                    }
                    //列与具名组匹配对应规则;
                    List<Integer> setMat=(List)tablerul.get("setMat");
                    tablem.put("setMat",setMat);
                    //表描述存在集合里//value是对表的说明,key是UUID,如 table_uuid值
                    Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");
                    tableMap.put(uuidS,tablem);

                    if(sectionsMapT!=null){ //说明是从区块对传过来的
                        sectionsMapT.put(tempKey,uuidS);
                    }else{
                        Map<String,String> vallMap=(Map)analyPdfM.get("vall");
                        vallMap.put(tempKey,uuidS);
                    }
                }else{
                    //未匹配表,且匹配结束标记,直接完结表规则
                    boolean rsEndMatch=isEndMatch(mapRule,rowsetV);
                    if(rsEndMatch){
                        if("true".equals(isChangeIndex)){
                            //更改当前下标
                            setIndex(temporaryMap,i);
                        }
                        //标记为结束
                        mapRule.put("alreadyOver","true");
                        mapRule.put("donotEnd","false");
                        break;
                    }
                    String continueMatch=(String)mapRule.get("continueMatch");
                    if("true".equals(continueMatch)){ //设置true 则当不匹配表时,终止当前循环行数据,继续匹配下一条规则
                        break;
                    }
                }

            }
        }
    }
    public boolean superaddF(Map<String, Object> mapRule,List<List<String>> rows,int index,int initI){
        boolean st;
        int superaddIndexI=(int) mapRule.get("superaddIndexI");
        String superaddMatchT=(String) mapRule.get("superaddMatchT");
        String superaddRowV=getRowSte(rows,index+superaddIndexI,initI);
        Pattern pat = Pattern.compile(superaddMatchT);
        Matcher mat = pat.matcher(superaddRowV);
        st = mat.find();
        return st;
    }
    //匹配matchT的规则;superaddMatchT是追加匹配
    public boolean matchT(String matchT,String rowV,Map<String, Object> mapRule,List<List<String>> rows,int index,int initI){
        boolean mt;
        Pattern pattern = Pattern.compile(matchT);
        Matcher matcher = pattern.matcher(rowV);
        mt = matcher.find();
        String superaddType=(String) mapRule.get("superaddType");
        if(StringUtils.isNotBlank(superaddType)){
            if("all".equals(superaddType)){
                if(mt){ // 都匹配方为匹配正确
                    boolean b = superaddF(mapRule, rows, index, initI);
                    if(!b){
                        mt=false;
                    }
                }
            }else if("add".equals(superaddType)){
                boolean b = superaddF(mapRule, rows, index, initI);
                if(!mt){ //当MatchT不匹配时,superaddMatchT匹配,认为是匹配的
                    if(b){
                        mt=true;
                    }
                }
            }
        }
        return mt;
    }
    //返回当前匹配的字符串
    public String matchStr(PDDocument document,int pageN,Map<String, Object> mapRule,Map temporaryMap,Map<String,Object> analyPdfM,Map sectionsMapT){
        String resStr="";
        //现行数
        int index=(int)temporaryMap.get("index");
        //当前页的行数据
        List<List<String>> rows=(List)temporaryMap.get("rows");
        //初始有效行数
        int initI=(int)temporaryMap.get("initI");
        //当前行数据
        String rowV=getRowSte(rows,index,initI);
        //测试
        /*if(pageN==13&&rowV.indexOf("Name/Location")!=-1){
            System.out.println(rowV);
        }*/
        //值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表 table,图片
        String valType=(String) mapRule.get("valType");
        //结束标记是否匹配表头
        String matchEndTable=(String) mapRule.get("matchEndTable");
        //表匹配规则
        Map<String,Object> mMap=mapp.get(fileType);
        Map<String,Map> tableRule=(Map)mMap.get("tableRule");
        //rs 为true 表初次匹配
        boolean rs =false;
        //上次的规则未完待续判断
        String donotEnd=(String) mapRule.get("donotEnd");
        //是否匹配
        String matchT=(String) mapRule.get("matchT");//匹配开始的正则
        //是否是为尾垃圾数据
        boolean isendrow = isEndrow(rowV);
        //是否匹配的处理
        if(!isendrow&&StringUtils.isNotBlank(matchT)){
            rs=matchT(matchT,rowV,mapRule,rows,index,initI);
            if(!rs){ //若不匹配
                //rs为false表未匹配,noMatchTOver为true直接结束此
                String noMatchTOver=(String) mapRule.get("noMatchTOver");
                if("true".equals(noMatchTOver)){
                    //标记为结束
                    mapRule.put("alreadyOver","true");
                    mapRule.put("donotEnd","false");
                    return resStr;
                }
            }
        }
        if("table".equals(valType)){
            //table的匹配方法
            matchStrTable(mapRule,temporaryMap,analyPdfM,sectionsMapT,matchEndTable,tableRule,index,initI,rows,donotEnd);
        }else if("sections".equals(valType)){   //区块对(只要没 alreadyOver ,此时重新又匹配到了就认为是个新的)
            String isFirstS=(String) mapRule.get("isFirstS");
            //又不匹配,又没开启
            if(!rs&&(!"true".equals(donotEnd))){
                return resStr;
            }
            //当前模板的Map
            Map sectionsMap=new HashMap();
            String tempKey=(String) mapRule.get("tempKey");//对应模板值
            List<Map> list =new ArrayList<Map>();
            if("true".equals(isFirstS)){ //是首区块对
                //是首提取对应的模板集合
                Map<String,List<Map>> sections=(Map)analyPdfM.get("sections");//N个模板所放位置
                list = sections.get(tempKey);//提取出叫"tempKey" 模板集合
                //赋初始值
                if(list==null){
                    list =new ArrayList<Map>();
                    sections.put(tempKey,list);
                }
            }else {
                //非首模板,表是其他模板递归过来的
                list = (List)sectionsMapT.get(tempKey);//提取出叫"tempKey" 模板集合
                //赋初始值
                if(list==null){
                    list =new ArrayList<Map>();
                    sectionsMapT.put(tempKey,list);
                }
            }
            if(rs){ //匹配开启个新的
                //清除标记(区块对的规则可被重复利用,所以此每次重启清除下标记)
                clearTag(mapRule);
                //此规则开启;
                mapRule.put("donotEnd","true");
                donotEnd="true";
                //是首,往从模板集合赋个空集合
                list.add(sectionsMap);
            }else {
                //匹配结束标记(//结束标记是否匹配表头在下)
                boolean rsEndMatch=isEndMatch(mapRule,rowV);
                //结束标记是否匹配表头
                boolean matchEndT=matchTabH(matchEndTable,rowV,tableRule);
                if(rsEndMatch||matchEndT){
                    //标记为结束
                    mapRule.put("alreadyOver","true");
                    mapRule.put("donotEnd","false");
                    return resStr;
                }
                if(list.size()==0){
                    System.out.println("-------list为0:"+list.size());
                }
                if(list.size()>0){//防止报错
                    //若是继续操作,则:提取老数据操作
                    sectionsMap=list.get(list.size()-1);
                }
            }
            List<Map<String, Object>> templateList=(List) mapRule.get("templateList");//区块对匹配规则
            //System.out.println("当前行:"+temporaryMap.get("index"));
            for(int i=0;i<templateList.size();i++){
                Map<String, Object> tempValMap=templateList.get(i);
                if("true".equals(tempValMap.get("alreadyOver"))){//失效的匹配规则过滤
                    continue;
                }
                matchStr(document,pageN,tempValMap, temporaryMap, analyPdfM,sectionsMap);
                if("true".equals(tempValMap.get("alreadyOver"))){
                    continue;
                }else{
                    //当前规则未完结,或未匹配到
                    String continueMatch=(String)tempValMap.get("continueMatch");
                    if("true".equals(continueMatch)){ //设置true 则继续匹配规则
                        continue;
                    }else{ //默认:已开始未完结,或未匹配到 直接break规则,获取下一行数据匹配规则去
                        break;
                    }
                }
            }
        }else{
            if(!"true".equals(donotEnd)){
                if(rs){
                    //此规则开启;
                    mapRule.put("donotEnd","true");
                    donotEnd="true";
                }
            }
            //规则响应
            if("true".equals(donotEnd)){
                //比如 复合型的单行匹配是没对应模板值的,所以如下有值的才往 analyPdfM 赋值
                String tempKey=(String) mapRule.get("tempKey");//对应模板值
                if("single".equals(valType)){
                    String matchI=(String) mapRule.get("matchI");//被提取值正则匹配规则,具名组匹配提值
                    Integer indexI=(Integer) mapRule.get("indexI");//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                    if(indexI==null){
                        indexI=0;
                    }
                    String rowVNew="";
                    if(indexI==0){
                        rowVNew=rowV;
                    }else {
                        rowVNew=getRowSte(rows, index+indexI,initI);
                    }
                    Pattern patternSingle = Pattern.compile(matchI);
                    Matcher matcherSingle = patternSingle.matcher(rowVNew);
                    String groupSingle ="";
                    if(matcherSingle.find()){
                        int groupCount = matcherSingle.groupCount();
                        for(int i=1;i<=groupCount;i++){
                            String group = matcherSingle.group(i);
                            if(StringUtils.isNotBlank(group)){
                                groupSingle=Helper.nvlString(group);
                                break;
                            }
                        }
                        resStr=groupSingle;
                    }
                    if(StringUtils.isNotBlank(tempKey)){
                        if(sectionsMapT!=null){ //说明是从区块对传过来的
                            sectionsMapT.put(tempKey,groupSingle);
                        }else{
                            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
                            vallMap.put(tempKey,groupSingle);
                        }
                    }
                    //标记为结束
                    mapRule.put("alreadyOver","true");
                    mapRule.put("donotEnd","false");
                }else if("image".equals(valType)){
                    //图片是在这一页上应该是有始有终(就不应该有跨页问题,即使跨页也要多个图,图片不能跨页生成)
                    Integer indexI=0;
                    String rowsetStr="";
                    //改变为当前行
                    String isChangeIndex=(String) mapRule.get("isChangeIndex");
                    indexI=(Integer) mapRule.get("indexI");
                    int newIndex=index+indexI;
                    int fontIndexTop=0;
                    int fontIndexBottom=0;
                    for (int i=newIndex;i<rows.size();i++){
                        //行数据
                        String rowsetV =getRowSte(rows,i,initI);
                        if(i==newIndex){ //第一次匹配值
                            fontIndexTop = getFontIndex(i, rows,"top");
                        }
                        if(StringUtils.isBlank(rowsetV)){
                            if("true".equals(isChangeIndex)){
                                //更改当前下标
                                setIndex(temporaryMap,i+1);
                            }
                            continue;
                        }
                        //是否是尾的垃圾数据
                        boolean endrow = isEndrow(rowsetV);
                        //匹配结束标记
                        boolean rsEndMatch=isEndMatch(mapRule,rowsetV);
                        //结束标记是否匹配表头
                        boolean matchEndT=matchTabH(matchEndTable,rowsetV,tableRule);
                        if(rsEndMatch||matchEndT||endrow){
                            //获取底
                            fontIndexBottom = getFontIndex(i, rows,"bottom");
                            matchEnd(isChangeIndex,i,index,temporaryMap,mapRule);//触发结束
                            break;
                        }
                    }
                    rowsetStr = getImageStr(document, pageN, fontIndexTop, fontIndexBottom);
                    //此方法return出去的值
                    resStr=rowsetStr;
                    if(StringUtils.isNotBlank(tempKey)){
                        if(sectionsMapT!=null){ //说明是从区块对传过来的
                            sectionsMapT.put(tempKey,rowsetStr);
                        }else{
                            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
                            vallMap.put(tempKey,rowsetStr);
                        }
                    }
                }
                else if("rowset".equals(valType)){
                    Integer indexI=0;
                    //改变为当前行
                    String isChangeIndex=(String) mapRule.get("isChangeIndex");
                    //是否根据单元格列去匹配内容
                    String colIndexWay=(String)mapRule.get("colIndexWay");
                    //是否校验排版规则,给前面赋值空格实现排版
                    String isCompSpace=(String) mapRule.get("isCompSpace");
                    //是否有替换功能
                    Map<String,String> replaceV=(Map) mapRule.get("replaceV");
                    //当前规则匹配的值
                    String rowsetStr="";
                    if(rs){ //初次匹配规则
                        //匹配matchT后若直接匹配结束标记,是否直接结束
                        String matchEndOver=(String) mapRule.get("matchEndOver");
                        if("true".equals(matchEndOver)){
                            //匹配结束标记
                            boolean rsEndMatch=isEndMatch(mapRule,rowV);
                            //结束标记是否匹配表头
                            boolean matchEndT=matchTabH(matchEndTable,rowV,tableRule);
                            if(rsEndMatch||matchEndT){
                                //标记为结束
                                mapRule.put("alreadyOver","true");
                                mapRule.put("donotEnd","false");
                                return resStr;
                            }
                        }
                        //测试
                        /*if(tempKey.equals("AAA")){
                            System.out.println(tempKey);
                        }*/
                        indexI=(Integer) mapRule.get("indexI"); //需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                        if(indexI==null){
                            indexI=0;
                        }
                        if("true".equals(colIndexWay)){
                            //为获取列所在列数;依据此值匹配
                            String colHeadMatch=(String)mapRule.get("colHeadMatch");
                            LinkedHashSet<Integer> colIndex=getColIndexL(rows,index,colHeadMatch);
                            mapRule.put("colIndex",colIndex);
                        }
                    }
                    int newIndex=index+indexI;
                    for (int i=newIndex;i<rows.size();i++){
                        String value ="";
                        if("true".equals(colIndexWay)){
                            LinkedHashSet<Integer> colIndex=(LinkedHashSet) mapRule.get("colIndex");
                            value= getColV(rows, i, colIndex);
                        }else{
                            //行数据
                            value =getRowSte(rows,i,initI);
                        }
                        if(rs&&(i==newIndex)){ //第一次匹配值
                            String matchI=(String) mapRule.get("matchI");//被提取值正则匹配规则,具名组匹配提值
                            Pattern patternRowset = Pattern.compile(matchI);
                            Matcher matcherRowset = patternRowset.matcher(value);
                            if(matcherRowset.find()){
                                int groupCount = matcherRowset.groupCount();
                                for(int gc=1;gc<=groupCount;gc++){
                                    String group = matcherRowset.group(gc);
                                    if(StringUtils.isNotBlank(group)){
                                        rowsetStr=Helper.nvlString(group);
                                        break;
                                    }
                                }
                                //是否有替换功能
                                if(replaceV!=null&&replaceV.size()>0){
                                    rowsetStr=replaceM(replaceV,rowsetStr);
                                }
                                //是否校验排版规则,给前面赋值空格实现排版
                                if("true".equals(isCompSpace)){
                                    rowsetStr=setSpace(mMap,rowsetStr,mapRule);
                                }
                            }else{
                                String noMatchIOver=(String) mapRule.get("noMatchIOver");
                                //当多行时,一开始根据matchI,就没匹配到内容,直接结束此
                                if("true".equals(noMatchIOver)){
                                    //标记为结束
                                    mapRule.put("alreadyOver","true");
                                    mapRule.put("donotEnd","false");
                                    break;
                                }
                            }
                        }else{
                            //行数据
                            String rowsetV=getRowSte(rows,i,initI);
                            //是否是尾的垃圾数据
                            boolean endrow = isEndrow(rowsetV);
                            if(endrow){
                                setIndex(temporaryMap,i);
                                break;
                            }
                            if(StringUtils.isBlank(rowsetV)){
                                if("true".equals(isChangeIndex)){
                                    //更改当前下标
                                    setIndex(temporaryMap,i+1);
                                }
                                continue;
                            }
                            //匹配结束标记
                            boolean rsEndMatch=isEndMatch(mapRule,rowsetV);
                            //结束标记是否匹配表头
                            boolean matchEndT=matchTabH(matchEndTable,rowsetV,tableRule);
                            if(rsEndMatch||matchEndT){
                                //触发结束
                                matchEnd(isChangeIndex,i,index,temporaryMap,mapRule);
                                break;
                            }
                            if(StringUtils.isBlank(value)){
                                if("true".equals(isChangeIndex)){
                                    //更改当前下标
                                    setIndex(temporaryMap,i+1);
                                }
                                continue;
                            }
                            String isAllMatch=(String) mapRule.get("isAllMatch");
                            String vall="";
                            if("true".equals(isAllMatch)){
                                //每一行值都从匹配规则中提取
                                String matchI=(String) mapRule.get("matchI");//被提取值正则匹配规则,具名组匹配提值
                                Pattern patternRowset = Pattern.compile(matchI);
                                Matcher matcherRowset = patternRowset.matcher(value);
                                if(matcherRowset.find()){
                                    int groupCount = matcherRowset.groupCount();
                                    for(int gc=1;gc<=groupCount;gc++){
                                        String group = matcherRowset.group(gc);
                                        if(StringUtils.isNotBlank(group)){
                                            vall=Helper.nvlString(group);
                                            break;
                                        }
                                    }
                                }
                            }else{
                                vall=value;
                            }
                            if(StringUtils.isNotBlank(vall)){//value必然有值,所以新设变量vall,这样未匹配时是空
                                //是否有替换功能
                                if(replaceV!=null&&replaceV.size()>0){
                                    vall=replaceM(replaceV,vall);
                                }
                                //是否校验排版规则,给前面赋值空格实现排版
                                if("true".equals(isCompSpace)){
                                    vall=setSpace(mMap,vall,mapRule);
                                }
                                if(StringUtils.isBlank(rowsetStr)){
                                    rowsetStr=vall;
                                }else{
                                    rowsetStr=rowsetStr+"\n"+vall;//循环行给rowsetStr累加赋值
                                }
                            }
                        }
                    }
                    //此方法return出去的值
                    resStr=rowsetStr;
                    if(StringUtils.isNotBlank(tempKey)){
                        if(sectionsMapT!=null){ //说明是从区块对传过来的
                            String str=(String) sectionsMapT.get(tempKey);
                            if(StringUtils.isBlank(str)){
                                sectionsMapT.put(tempKey,rowsetStr);
                            }else{
                                sectionsMapT.put(tempKey,str+"\n"+rowsetStr);
                            }
                        }else{//猜:解决跨页时的多行累加问题
                            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
                            String str=vallMap.get(tempKey);
                            if(StringUtils.isBlank(str)){
                                vallMap.put(tempKey,rowsetStr);
                            }else{
                                vallMap.put(tempKey,str+"\n"+rowsetStr);
                            }
                        }
                    }
                }else if("composite".equals(valType)){ //复合形(可多个单行或多行)
                    Map<String,String> vallMap=(Map)analyPdfM.get("vall");
                    String groupSingle = Helper.nvlString(vallMap.get(tempKey));
                    List<Map<String, Object>> compositeLis=(List) mapRule.get("compositeList");//对应模板值
                    int conN=0;
                    for(int i=0;i<compositeLis.size();i++){
                        Map<String, Object> Effectivity=compositeLis.get(i);
                        if("true".equals(Effectivity.get("alreadyOver"))){//失效的匹配规则过滤
                            conN++;
                            continue;
                        }
                        String s = matchStr(document,pageN,Effectivity, temporaryMap,analyPdfM,null);
                        if("true".equals(Effectivity.get("alreadyOver"))){
                            conN++;
                        }
                        //空数据排除
                        if(StringUtils.isBlank(s)){
                            continue;
                        }
                        if(i==0){
                            groupSingle=groupSingle+s;
                        }else{
                            groupSingle=groupSingle+"\n"+s;
                        }
                    }
                    if(StringUtils.isNotBlank(tempKey)){
                        vallMap.put(tempKey,groupSingle);
                    }
                    if(conN==compositeLis.size()){
                        //标记为结束
                        mapRule.put("alreadyOver","true");
                        mapRule.put("donotEnd","false");
                    }
                }
            }
        }
        return resStr;
    }
    public String getImageStr(PDDocument document,int pageN,int fontIndexTop, int fontIndexBottom){
        String imageStr="";
        try {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI((pageN-1),73);
            //image=image.getSubimage(99,34,100,100);
            int w = image.getWidth();
            int height = image.getHeight();
            if(fontIndexBottom>height){ fontIndexBottom=height; }
            int h=fontIndexBottom-fontIndexTop;
            image=image.getSubimage(0,fontIndexTop,w,h);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            byte[] bytes = os.toByteArray();
            os.close();
            String base64 = Helper.byteToBase64(bytes);
            //本地图片
            //ImageIO.write(image, "PNG", new File("C:/Users/18722/Desktop/tolg/cord/word/"+pageN+"x"+fontIndexTop+"x"+fontIndexBottom+".png"));
            //返回例 imageSingle_IMW:100IMH:200IMEND;图片值
            if(StringUtils.isNotBlank(base64)){
                imageStr="imageSingle_IMW:"+w+"IMH:"+h+"IMEND;"+base64;
            }
        }catch (Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
        }
        return imageStr;
    }
    //已知行,获取此行第一个字对应下标
    public int getFontIndex(int ind,List<List<String>> rows,String tb){
        //获取顶坐标
        StringBuilder sb=new StringBuilder();
        for (int r=0;r<ind;r++){ //循环行
            List<String> rowscol=rows.get(r);
            for(int rc=0;rc<rowscol.size();rc++){ //循环列
                String str=rowscol.get(rc);
                sb.append(str);
            }
        }
        //此处没有 .replaceAll("−","-"),没必要
        String rowStr=sb.toString().replaceAll(" ","");
        int fontS=rowStr.length();
        TextElement textElement = pageTextT.get(fontS);
        /*StringBuilder sbe=new StringBuilder();
        for (int r=0;r<pageTextT.size();r++){ //循环行
            String val=pageTextT.get(r).getText();
            sbe.append(val);
        }*/

        int n=0;
        if("top".equals(tb)){
            double y = textElement.getY();
            n=(int)Math.floor(y);
        }else {
            double y = textElement.getBottom();
            n=(int)Math.ceil(y);
        }
        return n;
    }
    //替换功能的实现
    public String replaceM(Map<String,String>replaceV,String rowvl){
        if(replaceV!=null&&replaceV.size()>0){
            for(String key:replaceV.keySet()){
                String val = replaceV.get(key);
                if(StringUtils.isBlank(rowvl)){
                     break;
                }
                rowvl=rowvl.replaceFirst(key,val);
            }
        }
        return rowvl;
    }
    //验排版规则,给前面赋值空格实现排版
    public String setSpace(Map mMap,String value,Map<String,Object> mapRule){
        Map<String,Integer> spaceRule=(Map)mMap.get("spaceRule");
        boolean bol=true;
        for(String key:spaceRule.keySet()){
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(value);
            if(matcher.find()){
                Integer can=0;
                Integer integer = spaceRule.get(key);
                if(integer>0){
                    can=integer;
                }else if(integer!=null){
                    //若是负数,当前行不变空格数.下一行空格数变
                    Integer spaceN =(Integer) mapRule.get("spaceN");
                    if(spaceN!=null){
                        can=spaceN;
                    }
                    //负数转正数
                    integer=can-integer;
                }
                value=StringUtils.leftPad("",can," ")+value;
                bol=false;
                mapRule.put("spaceN",integer);
                break;
            }
        }
        if(bol){
            Integer integer =(Integer)mapRule.get("spaceN");
            if(integer!=null){
                //比存值多三空格
                integer=integer+3;
                value=StringUtils.leftPad("",integer," ")+value;
            }
        }
        return value;
    }
    //是否匹配表头
    public boolean matchTabH(String matchEndTable,String rowsetV,Map<String,Map> tableRule){
        boolean matchEndT=false;
        if("true".equals(matchEndTable)){
            //行去掉多余内容方便比对
            String newRow=rowsetV.replaceAll("\\s", "").replaceAll("_", "");
            //遍历表规则
            for(String key:tableRule.keySet()){
                String newKey=key.replaceAll("\\s", "").replaceAll("_", "");
                if(newKey.equals(newRow)){//匹配表规则成功
                    matchEndT=true;
                    break;
                }
            }
        }
        return matchEndT;
    }
    //触发结束
    public void matchEnd(String isChangeIndex,int i,int index,Map temporaryMap,Map<String, Object> mapRule){
        if("true".equals(isChangeIndex)){
            //更改当前下标
            setIndex(temporaryMap,i);
        }
        //标记为结束
        mapRule.put("alreadyOver","true");
        mapRule.put("donotEnd","false");
    }
    //匹配结束的正则
    public boolean isEndMatch(Map<String, Object> mapRule,String rowsetV){
        String endMatch=(String) mapRule.get("endMatch");
        if(StringUtils.isBlank(endMatch)){
            return false;
        }
        Pattern patternEndMatch = Pattern.compile(endMatch);
        Matcher matcherEndMatch = patternEndMatch.matcher(rowsetV);
        boolean rsEndMatch = matcherEndMatch.find();
        return  rsEndMatch;
    }
    public void strToImage(String str,String key,Map<String,Object> map){
        int imageW=10;
        int imageH=10;
        String base64="";
        String p="imageSingle_IMW:(\\d+)IMH:(\\d+)IMEND;([\\s\\S]+)";
        Pattern pattern = Pattern.compile(p);
        Matcher matcherRowset = pattern.matcher(str);
        if(matcherRowset.find()){
            String v1= matcherRowset.group(1);
            String v2= matcherRowset.group(2);
            String v3 = matcherRowset.group(3);
            if(Helper.isInt(v1,false)){
                imageW=Integer.parseInt(v1);
            }
            if(Helper.isInt(v2,false)){
                imageH=Integer.parseInt(v2);
            }
            if(StringUtils.isNotBlank(v3)){
                base64=v3;
            }
        }
        if(StringUtils.isNotBlank(base64)){
            byte[] bytes =Helper.base64ToByte(base64);
            //不设宽高图会没有
            System.out.println(base64.length());
            PictureRenderData pictureRenderData = Pictures.ofBytes(bytes, PictureType.PNG).size(imageW, imageH).create();
            map.put(key, pictureRenderData);
        }
    }
    //集合里包括表|图片转为表数据,图数据(参1是List或Map此才生效)
    public void forListToTable(Object obj,Map<String,Object> analyPdfM){
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(String key:map.keySet()){
                Object value=map.get(key);
                if(value instanceof Map||(value instanceof List)){
                    forListToTable(value,analyPdfM);
                }else if(value instanceof String){
                    //校验value是否是表uuid,是则value替换为表TableRenderData类
                    String str=(String)value;
                    if("table_".equals(str.substring(0,6>str.length()?str.length():6))){
                        //说明是table表
                        Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");//value是对表的说明,key是UUID,如 table_uuid值
                        //根据uuid找对应的表解释数据
                        if(tableMap.containsKey(str)){
                            Map tabMap = tableMap.get(str);
                            putTable(map,tabMap,key);
                        }
                    }else if("imageSingle_".equals(str.substring(0,12>str.length()?str.length():12))){//这样是不是比 indexOf方式 更快
                        //说明是图片
                        //str值例 imageSingle_IMW:100IMH:200IMEND;图片值
                        strToImage(str,key,map);
                    }
                }
            }
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    forListToTable(value,analyPdfM);
                }
            }
        }
    }
    //清除标记
    public void clearTag(Object obj){
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object value:map.values()){
                if((value instanceof Map)||(value instanceof List)){
                    clearTag(value);
                }
            }
            map.remove("alreadyOver");
            map.remove("donotEnd");
        }else if(obj instanceof List){
            List list=(List)obj;
            for(int i=0;i<list.size();i++){
                Object value = list.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    clearTag(value);
                }
            }
        }
    }
    //返回处理后的行数据
    public String getRowSte(List<List<String>> rows,int ii,int initI){
        List<String> rowscol=rows.get(ii);
        StringBuilder sb=new StringBuilder();
        for(int iii=0;iii<rowscol.size();iii++){
            String str=rowscol.get(iii);
            sb.append(" "+str);
        }
        String rowV=sb.toString().trim().replaceAll("−","-").replaceAll("\\s{2,}"," ");
        //清理pdf里的脏数据-CRJ
        if(ii==initI){
            rowV=rowV.replaceAll("Mechanic Inspector","").trim();
        }
        return rowV;
    }
    //返回单元格值;colI 列所在下标(从0开始);ind 所在行数
    public String getColV(List<List<String>> rows,int ind,LinkedHashSet<Integer> colIndex){
        //行数据获取
        List<String> rowscol=rows.get(ind);
        StringBuilder sb=new StringBuilder();
        Iterator it=colIndex.iterator();
        while(it.hasNext()){//返回true或false
            int colI=(Integer) it.next();
            //防止超出下标
            if(colI<rowscol.size()){
                String str=rowscol.get(colI);
                sb.append(str+" ");
            }
        }
        String rowV=sb.toString().trim().replaceAll("−","-").replaceAll("\\s{2,}"," ");
        return rowV;
    }
    //返回匹配列所在列数(从0开始)
    public LinkedHashSet<Integer> getColIndexL(List<List<String>> rows,int ind,String matchStr){
        String[] split = matchStr.split(" ");
        LinkedHashSet<Integer> dexList=new LinkedHashSet<>();
        //行数据获取
        List<String> rowscol=rows.get(ind);
        for(String str:split){
            for(int i=0;i<rowscol.size();i++){
                String rowstr=rowscol.get(i);
                if(rowstr.indexOf(str)!=-1){
                    dexList.add(i);
                }
            }
        }
        return dexList;
    }
    /*public int getColIndex(List<List<String>> rows,int ind,String matchStr){
        int dex=0;
        //行数据获取
        List<String> rowscol=rows.get(ind);
        for(int i=0;i<rowscol.size();i++){
            String str=rowscol.get(i);
            if(Helper.nvlString(matchStr).equals(str)){
                return i;
            }
        }
        return dex;
    }*/

    /**
     * 解析PDF 返回,能直接用的数据
     * @author 子火
     * @Date 2021-01-07
     * @return  Boolean 是否已获取一个完整的word数据,需要生成word了
     */
    public Boolean analyPdfToMap(Page page,PDDocument document,int pageN,Map<String,Object> analyPdfM,int pageTypeN,List<Map<String,Object>> ruleList)throws Exception{
        Boolean bol=false;

        if(analyPdfM.size()!=0&&pageTypeN==0){
            //图数据保存
            saveImageData(analyPdfM,document,pageN);
        }else{
            analyPdf(page,analyPdfM,pageTypeN,ruleList,document,pageN);
        }
        return bol;
    }
    /**
     * 判断页面类型
     * 页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意没值时图片数据先不赋进去))
     * 前260字包含(1)and(2)and(3)表是测试页面就返回0表忽略
     * @author 子火
     * @Date 2021-01-08
     * @return  1:word的首页;2:需解析的页面;
     */
    public int pageType(Page page)throws Exception{
        int typeN=0;
        List<TextElement> pageText = page.getText();
        //过滤空
        pageText =pageText.stream().filter(n -> {
            String text = n.getText();
            if(StringUtils.isNotBlank(text)){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        //赋值
        setPageTextT(pageText);
        //取前260个字
        String str="";
        for(int i=0;i<pageText.size()&&i<260;i++){
            TextElement textElement = pageText.get(i);
            str+=textElement.getText();
        }
        Map<String,Object> fileTypeMap=mapp.get(fileType);
        //页面类型匹配规则获取
        Map<String, Integer> pageTypeM=(LinkedCaseInsensitiveMap)fileTypeMap.get("pageType");
        if(str.indexOf("(7)(8)(9)(10)(11)")!=-1){//识别测试页;crj是如此-前260字包含其表是测试页面
            return typeN;
        }
        for(String key:pageTypeM.keySet()){
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(str);
            if(matcher.find()){
                typeN=pageTypeM.get(key);
                return typeN;
            }
           /* //int i="青春无悔".indexOf("春无");返回1;
            int indexx = str.indexOf(key);
            if(indexx!=-1){
                typeN=pageTypeM.get(key);
                return typeN;
            }*/
        }
        return typeN;
    }
    /**
     * 生成word,入数据库
     * @author 子火
     * @Date 2021-01-08
     * @return
     */
    public ReturnClass run(String folderName,Map<String,Object> analyPdfM,Integer AMM_FILE_ID,JdbcTemplate jdbcTemplate)throws Exception{
        ReturnClass reC;
        //关键数据入库
        reC=saveToDatabase(analyPdfM,AMM_FILE_ID,folderName,jdbcTemplate);
        if(!reC.getStatusCode().equals("200")){
            return reC;
        }
        //生成word
        reC=cWordT(analyPdfM);
        if(!reC.getStatusCode().equals("200")){
            return reC;
        }

        return reC;
    }
    /**
     * 返回 PDDocument
     * @author 子火
     * @Date 2021-01-07
     * @return  PDDocument
     */
    public PDDocument returnPDDocument(InputStream input)throws Exception{
        //根据页面page类获取
        PDDocument document = PDDocument.load(input);
        return document;
    }
    /**
     * 返回页面总数
     * @author 子火
     * @Date 2021-01-07
     * @return  页面总数
     */
    public int retPagenum(PDDocument document)throws Exception{
        //pdf页数(第1页到第200页,此返回200)
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int pagenum=pages.getCount();
        return pagenum;
    }
    /**
     * 返回page
     * @author 子火
     * @Date 2021-01-07
     */
    public Page retPageC(ObjectExtractor oe,int pageN)throws Exception{
        //pdf页数(第1页到第200页,此返回200)
        Page page = oe.extract(pageN);
        return page;
    }

    //是否是最后一行
    public boolean isEndrow(String rowStr) {//匹配值例 "CSP B−089 − MASTER"
        String reg = "(CSP .+ MASTER)|(EFFECTIVITY SOURCE( .+)?)";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean bol =rowStr.matches(reg);
        return bol;
    }
    //头部数据初始位置
    public int getInitI(List<List<String>> rows,int pageTypeN){
        int i=0;
        for(int ii=0;ii<rows.size()&&ii<6;ii++){
            List<String> rowscol=rows.get(ii);
            StringBuilder sb=new StringBuilder();
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                sb.append(" "+str);
            }
            //行数据
            String rowV=sb.toString().trim().replaceAll("−","-").replaceAll("\\s{2,}"," ");
            //System.out.println(fileType);
            String p="";
            int jumpN=0;
            if("boeing".equals(fileType)){
                //首页
                if(pageTypeN==1){
                    p="TASK CARDS";
                    jumpN=1;
                }else if(pageTypeN==2){
                    p="BOEING CARD NO";
                    jumpN=3;
                }else if(pageTypeN==3){
                    p="BOEING CARD NO";
                    jumpN=2;
                }
            }else if("crj".equals(fileType)){
                //首页
                if(pageTypeN==1){
                    p="MAINTENANCE TASK";
                    jumpN=1;
                }else{
                    p="Task Card Number";
                    jumpN=2;
                }
            }
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(rowV);
            boolean rs = matcher.find();
            if(rs){
                i=ii+jumpN;
                break;
            }
        }
        return i;
    }
    /**
     * 关
     * @author 子火
     * @Date 2020-12-23
     */
    public void closed(ObjectExtractor oe,PDDocument document,InputStream input)throws Exception{
        //根据页面page类获取
        oe.close();
        document.close();
        input.close();
    }




}
