package cn.nzxxx.predict.config.pdftable;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import com.deepoove.poi.data.style.TableStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.jbig2.SegmentData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
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
        table1List.add("^(\\S+)( .+)?$");
        table1.put("colMatch",table1List);//列值获取方式
        table1.put("valNVL","add");// //up 列无值时,取上行值; add 无值时和上行合并
        tableCRJ.put("REFERENCE DESIGNATION",table1);                      //1. Consumable Materials; Tools and Equipment;
        Map<String,Object> table3=new HashMap<String,Object>();
        List<String> table3List=new ArrayList<String>();
        table3List.add("^(\\S+ )?(AMM \\S+ )?(.+)$");
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
        mapRule2.put("matchT","Manual Rev:");//匹配开始的正则
        mapRule2.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule2.put("matchI","Rev: ([0-9]+)");//被提取值正则匹配规则,具名组匹配提值
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
        mapRule12.put("matchT","MRM REFERENCE:");//匹配开始的正则
        mapRule12.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule12.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule12.put("matchI","^MRM REFERENCE: (.+)");//被提取值正则匹配规则,具名组匹配提值
        ruleCRJ.add(mapRule12);
        Map<String, Object> mapRule13=new HashMap<String, Object>();
        mapRule13.put("tempKey","jobSet");//对应模板值
        mapRule13.put("matchT","^[0-9]\\. (.+)");//匹配开始的正则
        //mapRule13.put("endMatch","在word,此模板在最后所以就没设此");//模板匹配结束
            //遇到结束才允许删这条匹配规则(就因为有多个才用模板)
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
                templateList13_2.add(tempVal13_2_1);
                Map tempVal13_2_2=new HashMap();
                    tempVal13_2_2.put("tempKey","tablee");//对应模板值
                    tempVal13_2_2.put("valType","table");//值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表table
                    tempVal13_2_2.put("matchEndTable","true");//结束标记是否匹配表头
                        //表头同当前表头直接赋当前表里面
                        //若有多表紧挨,临时:再如{{#tablee2}},后期改用模板,动态生成多个表
                    tempVal13_2_2.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)|([A-Z]+:)|(^Refer)|(^ON )");//匹配结束
                    tempVal13_2_2.put("isChangeIndex","true");//改变i为当前行
                templateList13_2.add(tempVal13_2_2);
                Map tempVal13_2_3=new HashMap();
                    tempVal13_2_3.put("tempKey","endV");//对应模板值
                    tempVal13_2_3.put("matchT","([A-Z]+:)|(^Refer)|(^ON)");//匹配开始的正则
                    tempVal13_2_3.put("valType","rowset");;//值类型
                    tempVal13_2_3.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                    tempVal13_2_3.put("matchI","([A-Z]+: .+)|(^Refer .+)|(^ON .+)");//匹配开始()里是要的值
                    tempVal13_2_3.put("endMatch","(^[A-Z]\\.)|(^\\([0-9]+\\))|(^[0-9]\\.)");//匹配结束
                    //tempVal13_2_3.put("matchEndTable","true");//结束标记是否匹配表头
                    tempVal13_2_3.put("isChangeIndex","true");//改变i为当前行
                templateList13_2.add(tempVal13_2_3);
            //.put("continueMatch","true");//未匹配表跳过继续匹配下一个
            tempVal13_2.put("templateList",templateList13_2);
            templateList13.add(tempVal13_2);
        mapRule13.put("templateList",templateList13);
        ruleCRJ.add(mapRule13);
        crjMap.put("rule",ruleCRJ);
        //生成word 根据哪个模板值
        crjMap.put("saveNameTemN","TaskCardNumber");
        mapp.put("crj",crjMap);//对crj的整体定义
        //------------BOEING 开始--------------------
        Map<String,Object> boeingMap=new HashMap<String,Object>();
        boeingMap.put("temp","taskCardBoeingT.docx");//模板名称
        boeingMap.put("imageW",600);//图片宽
        boeingMap.put("imageH",400);//图片高
        //页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意analyPdfM没值时图片数据先不赋进去))
        Map<String, Integer> pageTypeBM=new LinkedCaseInsensitiveMap();
        pageTypeBM.put("AIRLINECARDNO",1);//先判断1,其包含2的判断依据
        pageTypeBM.put("MECHINSP",2);
        boeingMap.put("pageType",pageTypeBM);
        mapp.put("boeing",boeingMap);//对boeing的整体定义
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
        setMap.put(key, Tables.create(rowRenderData));
    }
    /**
     * 生成word
     * @author 子火
     * @Date 2021-01-07
     * @return  ReturnClass
     */
    public ReturnClass cWordT(String urll,String fileName,String fileType,Map<String,Object> analyPdfM)throws Exception{
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
        // 普通文本赋值|table的赋值
        Map<String,String> vallMap=(Map)analyPdfM.get("vall");
        for(String key:vallMap.keySet()){
            String value = vallMap.get(key);
            if(value.indexOf("table_")!=-1){
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
            }else{
                params.put(key, value);
            }
        }
        // 图片赋值
        List<byte[]> imagesB=(List)analyPdfM.get("imagesB");
        if(imagesB.size()>0){
            List<Map> subData = new ArrayList<Map>();
            for(int i=0;i<imagesB.size();i++){
                byte[] bytes = imagesB.get(i);
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
        //配置文件值获取
        ResourceBundle re = java.util.ResourceBundle.getBundle("application");//application.properties里值
        String saveMain = re.getString("saveurl.main");
        String saveExtend = re.getString("saveurl.taskcard.extend");
        //保存后文件名
        String saveName=(String) analyPdfM.get("saveName");
        //保存后的文件夹位置(要事先存在)
        String saveUrl=saveMain+saveExtend+fileName;
        // 创建文件夹
        File file = new File(saveUrl);
        if (!file.exists()) {
            file.mkdirs();
        }
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
        List<byte[]> imagesB=(List<byte[]>) analyPdfM.get("imagesB");
        imagesB.add(bytes);
    }
    /**
     * 关键数据入库
     * @author 子火
     * @Date 2021-01-07
     * @return  ReturnClass
     */
    public ReturnClass saveToDatabase(Map<String,Object> analyPdfM)throws Exception{
        ReturnClass reC=Help.returnClassT(200,"数据入库成功","");

        return reC;
    }

    /**
     * 解析PDF
     * @author 子火
     * @Date 2021-01-08
     */
    public void analyPdf(Page page,Map<String,Object> analyPdfM,int pageTypeN,List<Map<String,Object>> ruleList){
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
        if(pageTypeN==1){
            //初始赋值
            List<byte[]> imagesB=new ArrayList<byte[]>();
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
        }
        //临时数据
        Map temporaryMap=new HashMap();
        //头部数据初始位置
        int initI = getInitI(rows, pageTypeN);
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
            matchRule(temporaryMap,ruleList,analyPdfM);
        }
        //System.out.println("-------------------------------");
        if(pageTypeN==1){
            //文件夹依据哪个模板值
            Map<String,Object> mMap=mapp.get(fileType);
            String saveNameTemN=(String)mMap.get("saveNameTemN");// 返回例 TaskCardNumber
            Map<String,String> vallMap=(Map)analyPdfM.get("vall");
            String taskCardNumber=vallMap.get(saveNameTemN);
            analyPdfM.put("saveName",taskCardNumber);
        }
    }
    //操作下标
    public void matchRule(Map temporaryMap,List<Map<String,Object>> ruleList,Map<String,Object> analyPdfM){
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
            matchStr(mapRule,temporaryMap,analyPdfM,null);
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
        //实时更新行
        if(index>nowI){//防止死循环
            temporaryMap.put("index",index);
        }
    }
    //table的匹配方法
    public void matchStrTable(Map<String, Object> mapRule,Map temporaryMap,Map<String,Object> analyPdfM,Map sectionsMapT,String matchEndTable,Map<String,Map> tableRule,int index,int initI,List<List<String>> rows,String donotEnd){
        //改变i为当前行
        String isChangeIndex=(String) mapRule.get("isChangeIndex");
        //System.out.println("进入表匹配,开始行:"+index);
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
                //匹配行数据
                for(int c=0;c<colMatch.size();c++){
                    String matchh = colMatch.get(c);
                    Pattern pattern = Pattern.compile(matchh);
                    Matcher matcher = pattern.matcher(rowsetV);
                    if(matcher.find()){
                        //行数据
                        String[] tabBodyStr=new String[colN];
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
                        if(isBlank){ //有空的情况
                            int size = tabBody.size();
                            if(size>0){
                                //最新的一行数据
                                String[] presentStr= tabBody.get(size - 1);
                                //up 列无值时,取上行值; add 无值时和上行合并
                                for(int array=0;array<tabBodyStr.length;array++){
                                    //已存值
                                    String strP=presentStr[array];
                                    //当前解析值
                                    String strN=tabBodyStr[array];
                                    if("up".equals(valNVL)){
                                        if(StringUtils.isBlank(strN)){
                                            //up 列无值时,取上行值
                                            tabBodyStr[array]=strP;
                                        }
                                    }else if("add".equals(valNVL)){
                                        if(StringUtils.isNotBlank(strN)){
                                            presentStr[array]=strP+"\n"+strN;
                                        }
                                    }
                                }
                                if("up".equals(valNVL)){
                                    tabBody.add(tabBodyStr);
                                }
                            }else {
                                tabBody.add(tabBodyStr);
                            }
                        }else {
                            tabBody.add(tabBodyStr);
                        }
                    }
                    break;
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
                    //未匹配表,且匹配结束标记,直接完结表
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
                }
            }
        }
    }
    //返回当前匹配的字符串
    public String matchStr(Map<String, Object> mapRule,Map temporaryMap,Map<String,Object> analyPdfM,Map sectionsMapT){
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
        /*if(rowV.indexOf("24-00-00-861-801")!=-1){
            System.out.println(rowV);
        }*/
        //值类型:单行 single ,多行 rowset ,复合 composite,区块对 sections,表 table
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
        if("table".equals(valType)){
            //table的匹配方法
            matchStrTable(mapRule,temporaryMap,analyPdfM,sectionsMapT,matchEndTable,tableRule,index,initI,rows,donotEnd);
        }else if("sections".equals(valType)){   //区块对(只要没 alreadyOver ,此时重新又匹配到了就认为是个新的)
            String isFirstS=(String) mapRule.get("isFirstS");
            String matchT=(String) mapRule.get("matchT");//匹配开始的正则
            if(StringUtils.isBlank(matchT)){
                return resStr;
            }
            Pattern pattern = Pattern.compile(matchT);
            Matcher matcher = pattern.matcher(rowV);
            rs = matcher.find();
            //又不匹配,又没开启
            if(!rs&&(!"true".equals(donotEnd))){
                return resStr;
            }
            //当前模板的Map
            Map sectionsMap=new HashMap();
            String tempKey=(String) mapRule.get("tempKey");//对应模板值
            List<Map> list =new ArrayList<Map>();
            if("true".equals(isFirstS)){//是首区块对
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
                    //是继续操作则,提取老数据操作
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
                matchStr(tempValMap, temporaryMap, analyPdfM,sectionsMap);
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
                String matchT=(String) mapRule.get("matchT");//匹配开始的正则
                if(StringUtils.isBlank(matchT)){
                    return resStr;
                }
                Pattern pattern = Pattern.compile(matchT);
                Matcher matcher = pattern.matcher(rowV);
                rs = matcher.find();
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
                        //groupSingle = matcherSingle.group(1);
                        int groupCount = matcherSingle.groupCount();
                        for(int i=1;i<=groupCount;i++){
                            String group = matcherSingle.group(i);
                            if(StringUtils.isNotBlank(group)){
                                groupSingle=group;
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
                }else if("rowset".equals(valType)){
                    Integer indexI=0;
                    //改变为当前行
                    String isChangeIndex=(String) mapRule.get("isChangeIndex");
                    //当前规则匹配的值
                    String rowsetStr="";
                    if(rs){ //初次匹配规则
                        indexI=(Integer) mapRule.get("indexI"); //需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
                        if(indexI==null){
                            indexI=0;
                        }
                    }
                    int newIndex=index+indexI;
                    for (int i=newIndex;i<rows.size();i++){
                        //行数据
                        String rowsetV=getRowSte(rows,i,initI);
                        if(rs&&(i==newIndex)){ //第一次匹配值
                            String matchI=(String) mapRule.get("matchI");//被提取值正则匹配规则,具名组匹配提值
                            Pattern patternRowset = Pattern.compile(matchI);
                            Matcher matcherRowset = patternRowset.matcher(rowsetV);
                            if(matcherRowset.find()){
                                //rowsetStr= matcherRowset.group(1);
                                int groupCount = matcherRowset.groupCount();
                                for(int gc=1;gc<=groupCount;gc++){
                                    String group = matcherRowset.group(gc);
                                    if(StringUtils.isNotBlank(group)){
                                        rowsetStr=group;
                                        break;
                                    }
                                }
                            }
                        }else{
                            //是否是尾的垃圾数据
                            boolean endrow = isEndrow(rowsetV);
                            if(endrow){
                                setIndex(temporaryMap,i);
                                break;
                            }
                            if(StringUtils.isBlank(rowsetV)){
                                //更改当前下标(表)
                                setIndex(temporaryMap,i+1);
                                continue;
                            }
                            //匹配结束标记(//结束标记是否匹配表头在下)
                            boolean rsEndMatch=isEndMatch(mapRule,rowsetV);
                            //结束标记是否匹配表头
                            boolean matchEndT=matchTabH(matchEndTable,rowsetV,tableRule);
                            if(rsEndMatch||matchEndT){
                                //触发结束
                                matchEnd(isChangeIndex,i,index,temporaryMap,mapRule);
                                break;
                            }
                            if(StringUtils.isBlank(rowsetStr)){
                                rowsetStr=rowsetV;
                            }else{
                                rowsetStr=rowsetStr+"\n"+rowsetV;
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
                        }else{
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
                        String s = matchStr(Effectivity, temporaryMap,analyPdfM,null);
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
    //集合里包括表转为表数据(参1是List或Map此才生效)
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
                    if(str.indexOf("table_")!=-1){
                        //说明是table表
                        Map<String,Map> tableMap=(Map)analyPdfM.get("tableMap");//value是对表的说明,key是UUID,如 table_uuid值
                        //根据uuid找对应的表解释数据
                        if(tableMap.containsKey(str)){
                            Map tabMap = tableMap.get(str);
                            putTable(map,tabMap,key);
                        }
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
            map.put("alreadyOver",null);
            map.put("donotEnd",null);
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
            analyPdf(page,analyPdfM,pageTypeN,ruleList);
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
        //取前260个字
        String str="";
        for(int i=0;i<pageText.size()&&i<260;i++){
            TextElement textElement = pageText.get(i);
            str+=textElement.getText();
        }
        Map<String,Object> crjMap=mapp.get(fileType);
        //页面类型匹配规则获取
        Map<String, Integer> pageTypeM=(LinkedCaseInsensitiveMap)crjMap.get("pageType");
        if(str.indexOf("(7)(8)(9)(10)(11)")!=-1){//识别测试页;crj是如此-前260字包含其表是测试页面
            return typeN;
        }
        for(String key:pageTypeM.keySet()){
            //int i="青春无悔".indexOf("春无");返回1;
            int indexx = str.indexOf(key);
            if(indexx!=-1){
                typeN=pageTypeM.get(key);
                return typeN;
            }
        }
        return typeN;
    }
    /**
     * 生成word,入数据库
     * @author 子火
     * @Date 2021-01-08
     * @return
     */
    public ReturnClass run(Page page,String urll,String fileName,Map<String,Object> analyPdfM)throws Exception{
        ReturnClass reC;
        //生成word
        reC=cWordT(urll,fileName,fileType,analyPdfM);
        if(!reC.getStatusCode().equals("200")){
            return reC;
        }
        //关键数据入库
        reC=saveToDatabase(analyPdfM);
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

    /**
     * 此是解析结构数据返回sq,过滤页面数据写此处
     * 返回此页的插入sql
     * @author 子火
     * @Date 2020-12-23
     * @return  页面插入sql(返回空表拦截了,直接 continue;)
     */
    public String retInSql(List<List<String>> newrows,Map conditionsMap)throws Exception{
        //表头list获取,用于校验那个列缺失
        List<String> tabTList = newrows.get(0);

        String sql="";
        StringBuilder zdB=new StringBuilder("");
        String type=(String)conditionsMap.get("type");
        String tabnam=(String)conditionsMap.get("tabnam");
        List<String> tabcols=(List<String>)conditionsMap.get("tabcols");
        for(int i=0;i<tabcols.size();i++){
            String tabcol= "`"+tabcols.get(i)+"`";
            if(i==0){
                zdB.append(tabcol);
            }else{
                zdB.append(","+tabcol);
            }
        }
        //sql要的list
        List<List<String>> sqllist=new ArrayList<List<String>>();
        //第一列有值,才认为是一条需要插入的数据,其它列有值但第一列无值则认为和上是同一条数据
        //"b".equals(type)||"hi".equals(type) 现模板都需如下,所以直接写true
        if(true){
            for(int i=1;i<newrows.size();i++){ //循环行(正式数据从1开始,0是表头)
                List<String> strings = newrows.get(i);
                for(int ii=0;ii<strings.size();ii++){//循环列
                    String s = strings.get(ii);
                    if(StringUtils.isBlank(s)){ //此列无值continue
                        continue;
                    }else{
                        s=s.replaceAll("'","‘");//单引号转为中文的单引号
                    }
                    //插入数据过滤
                    if(("hi".equals(type))&&(ii==0)&&(s.indexOf("SECTION")!=-1)){
                        break;//退出二重循环
                    }if(("sloc".equals(type))&&(ii==0)&&(s.indexOf("(Continued)")!=-1)){
                        s=s.replaceAll("\\(Continued\\)","");
                    }
                    if(ii==0){ //第一列有值,才认为是一条需要插入的数据
                        List<String> col=new ArrayList<String>();
                        //赋初始值
                        for(int j=0;j<strings.size();j++){
                            col.add("");
                        }
                        col.set(ii,s);
                        sqllist.add(col);
                    }else {
                        int size = sqllist.size();
                        if(size>0){
                            //获取最后一个
                            List<String> strings1 = sqllist.get(size - 1);
                            //原有值,拼一起,换行符 表示换行
                            String s1 = strings1.get(ii);
                            if(StringUtils.isNotBlank(s1)){
                                s1=s1+"\\r\\n"+s;
                                strings1.set(ii,s1);
                            }else{
                                s1=s;
                            }
                            strings1.set(ii,s1);
                        }
                    }
                }
            }
        }
        for(int i=0;i<sqllist.size();i++){ //每一行
            List<String> sqlrow = sqllist.get(i);
            int sdiff=tabcols.size()-sqlrow.size();
            if(sdiff>0){
                //列补全计划
                for(int c=0;c<tabcols.size();c++){
                    String t = tabcols.get(c);
                    if(c>=sqlrow.size()){
                        sqlrow.add("");
                        continue;
                    }
                    String s = tabTList.get(c).replaceAll(" ","_");
                    if(t.indexOf(s)==-1){
                        tabTList.add(c,"");
                        sqlrow.add(c,"");
                        sdiff=sdiff-1;
                        if(sdiff==0){
                            break;
                        }
                    }
                }
            }
            String val="(";//(2,值)  此行对应的插入内容
            for(int c=0;c<tabcols.size();c++){
                String sval = sqlrow.get(c);
                //特殊符修改防止入库为?
                sval=sval.replaceAll("−","-");
                String s = "'"+sval+"'";
                if(c==0){
                    val=val+s;
                }else{
                    val=val+","+s;
                }
            }
            val=val+")";
            if(i==0){
                sql="insert into "+tabnam+"("+zdB.toString()+") values "+val;
            }else{
                sql=sql+","+val;
            }
        }
        return sql;
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
            //System.out.println(rowV);
            //非首页情况,即默认
            String p="(Task Card Number)|(BOEING CARD NO)";
            int jumpN=2;
            //首页
            if(pageTypeN==1){
                p="(MAINTENANCE TASK)|(TASK CARDS)";
                jumpN=1;
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
