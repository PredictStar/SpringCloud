package cn.nzxxx.predict.config.pdftable;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.jbig2.SegmentData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


//解析pdf,form 数据 生成word
public class FormPdf {
    private Map<String,Map<String,Object>> mapp=new HashMap<String,Map<String,Object>>();
    //初始化
    public FormPdf() {
        //------------CRJ 开始--------------------
        Map<String,Object> crjMap=new HashMap<String,Object>();
        crjMap.put("temp","taskCardCRJT.docx");//模板名称
        //页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意analyPdfM没值时图片数据先不赋进去))
        //前260字包含(7)(8)(9)(10)(11)表是测试页面
        Map<String, Integer> pageTypeCM=new LinkedCaseInsensitiveMap();
        pageTypeCM.put("AirlineDesignatorAircraft",1);//先判断1,其包含2的判断依据
        pageTypeCM.put("AircraftSeriesAircraftNumber",2);
        crjMap.put("pageType",pageTypeCM);
        //提取值规则定义,key是字段触发开始依据
        List<Map<String,Object>> ruleCRJ=new ArrayList<Map<String,Object>>();
        Map<String, Object> mapRule1=new HashMap<String, Object>();
        mapRule1.put("tempKey","AircraftSeries");//对应模板值
        mapRule1.put("matchT","Aircraft Series");//匹配开始的正则
        mapRule1.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule1.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule1.put("matchI","^(\\S+/\\S+/\\S+)");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule1);
        Map<String, Object> mapRule1_2=new HashMap<String, Object>();
        mapRule1_2.put("tempKey","TaskCardNumber");//对应模板值
        mapRule1_2.put("matchT","Task Card Number");//匹配开始的正则
        mapRule1_2.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule1_2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule1_2.put("matchI","(\\S+-\\S+-\\S+ \\(\\S+ \\S+\\))$");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule1_2);
        Map<String, Object> mapRule2=new HashMap<String, Object>();
        mapRule2.put("tempKey","ManualRev");//对应模板值
        mapRule2.put("matchT","Manual Rev:");//匹配开始的正则
        mapRule2.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule2.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule2.put("matchI","Rev: ([0-9]+)");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule2);
        Map<String, Object> mapRule3=new HashMap<String, Object>();
        mapRule3.put("tempKey","Amendment");//对应模板值
        mapRule3.put("matchT","nt:");//匹配开始的正则
        mapRule3.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule3.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule3.put("matchI","nt: ([0-9]+)");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule3);
        Map<String, Object> mapRule4=new HashMap<String, Object>();
        mapRule4.put("tempKey","TaskType");//对应模板值
        mapRule4.put("matchT","Task Type");//匹配开始的正则
        mapRule4.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule4.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        //先如下写,若不对可直接定义 值A|值B
        mapRule4.put("matchI","^([A-Z]+)");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule4);
        Map<String, Object> mapRule5=new HashMap<String, Object>();
        mapRule5.put("tempKey","Skill");//对应模板值
        mapRule5.put("matchT","Type Skill Labor");//匹配开始的正则
        mapRule5.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule5.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule5.put("matchI"," ([A-Z]+) ");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule5);
        Map<String, Object> mapRule6=new HashMap<String, Object>();
        mapRule6.put("tempKey","LaborHours");//对应模板值
        mapRule6.put("matchT","Labor Hours");//匹配开始的正则
        mapRule6.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule6.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule6.put("matchI","([0-9]+\\.[0-9]+)");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule6);
        Map<String, Object> mapRule7=new HashMap<String, Object>();
        mapRule7.put("tempKey","NbrOfPersons");//对应模板值
        mapRule7.put("matchT","Nbr of Persons");//匹配开始的正则
        mapRule7.put("indexI",1);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule7.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule7.put("matchI"," ([0-9]+)$");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule7);
        Map<String, Object> mapRule8=new HashMap<String, Object>();
        mapRule8.put("tempKey","Zone(s)");//对应模板值
        mapRule8.put("matchT","Zone(s):");//匹配开始的正则
        mapRule8.put("indexI",0);//需提取值开始提取时,相对于触发依据所在行位置"-1"即在上一行
        mapRule8.put("valType","single");//值类型:单行 single ,多行 rowset ,复合 composite
        mapRule8.put("matchI","Zone\\(s\\): (.+)$");//被提取值正则匹配规则,具名组匹配提值 matcher.group(1)
        ruleCRJ.add(mapRule8);
        crjMap.put("rule",ruleCRJ);
        mapp.put("crj",crjMap);//对crj的整体定义
        //------------BOEING 开始--------------------
        Map<String,Object> boeingMap=new HashMap<String,Object>();
        boeingMap.put("temp","taskCardBoeingT.docx");//模板名称
        //页面类型规则定义(1:word的首页;2:需解析的页面;剩余解析成图片(注意analyPdfM没值时图片数据先不赋进去))
        Map<String, Integer> pageTypeBM=new LinkedCaseInsensitiveMap();
        pageTypeBM.put("AIRLINECARDNO",1);//先判断1,其包含2的判断依据
        pageTypeBM.put("MECHINSP",2);
        boeingMap.put("pageType",pageTypeBM);
        mapp.put("boeing",boeingMap);//对boeing的整体定义
    }
    protected static final Logger logger = Logger.getLogger(FormPdf.class);
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
        Map<String,Object> crjMap=mapp.get(fileType);
        //主模板名称
        String mainNameT=(String)crjMap.get("temp");
        String templatePath = filePath+"META-INF/resources/wordtemplate"+File.separator+mainNameT;
        //System.out.println(filePath);
        XWPFTemplate template = XWPFTemplate.compile(templatePath);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("test", analyPdfM.get("test"));  //会带值进  {{abc}}

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
        if(imagesB==null){
            imagesB=new ArrayList<byte[]>();
            analyPdfM.put("imagesB",imagesB);
        }
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
    public Map analyPdf(Page page){
        Map resmap=new HashMap();
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
        //解析数据输出
        for(int ii=0;ii<rows.size();ii++){
            List<String> rowscol=rows.get(ii);
            StringBuilder sb=new StringBuilder();
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                sb.append(" "+str);
                System.out.print(str);
                // \t方便复制到xls时有格式,后期注释掉
                System.out.print("	");
            }
            //当前行结尾,后期注释掉
            System.out.println("*");
            //行数据
            String rowV=sb.toString().trim().replaceAll("−","-").replaceAll("\\s{2,}"," ");
        } System.out.println("-------------------------------");

        return resmap;
    }
    /**
     * 解析PDF 返回,能直接用的数据
     * @author 子火
     * @Date 2021-01-07
     * @return  Boolean 是否已获取一个完整的word数据,需要生成word了
     */
    public Boolean analyPdfToMap(Page page,PDDocument document,int pageN,Map<String,Object> analyPdfM,int pageTypeN)throws Exception{
        Boolean bol=false;

        if(analyPdfM.size()!=0&&pageTypeN==0){
            //图数据保存
            saveImageData(analyPdfM,document,pageN);
        }else{
            Map resmap=analyPdf(page);
            if(pageTypeN==1){
                String taskCardNumber="000−21−310−141 (Config A04)";
                analyPdfM.put("saveName",taskCardNumber);
                analyPdfM.put("TaskCardNumber",taskCardNumber);
            }else if(pageTypeN==2){

            }
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
    public int pageType(Page page,String urll,String fileName,String fileType)throws Exception{
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
    public ReturnClass run(Page page,String urll,String fileName,String fileType,Map<String,Object> analyPdfM)throws Exception{
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
    public boolean isEndrow(List<String> list) {
        boolean bol=false;
        String s=list.get(0);//匹配值例 "Feb 15/2020"
        String reg = "^[A-Za-z]{3} [0-9]{1,2}\\/2[0-9]{3}$";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean rs =s.matches(reg);
        if(rs){
            bol=true;
        }
        return bol;
    }
    //是否是最后一行
    public boolean isEndrowCrjS(List<String> list) {
        boolean bol=false;
        String s=list.get(0);//匹配值例 "CSP B−136 −  Maintenance Planning Document"
        String reg = "^CSP .+ Maintenance Planning Document$";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean rs =s.matches(reg);
        if(rs){
            bol=true;
        }
        return bol;
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
