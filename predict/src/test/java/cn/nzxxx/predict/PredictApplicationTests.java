package cn.nzxxx.predict;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.test;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.hibernate.validator.internal.util.StringHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.xmlunit.util.Convert;
import sun.misc.BASE64Encoder;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringRunner.class)//引入spring对JUnit4的支持
@SpringBootTest
public class PredictApplicationTests {
    private Logger log = LoggerFactory.getLogger(this.getClass());	//import org.slf4j.LoggerFactory;

    /*private MockMvc mockMvc;
    @Value("${spring.freemarker.charset}")
    private String springbootUrl;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
    }


    @Test
    public void test() throws Exception {
        Long A=12l;
        System.out.println(A.getClass().getName());
        //System.out.println(null.getClass().getName());


    }
    @Autowired
    private MongoTemplate mongoTemplate;*/



    /* //判断正整数是否是质数,是则返回true
     public boolean isPrimeNum(int num){
         boolean isPrimeNum=false;
         List<Integer> pnums=new ArrayList<Integer>(16);//当前质数集
         for(int i=2;i<=num;i++){//1不是质数
             boolean bol=true;//当前i是集合里没有的质数
             for (int j=0;j<pnums.size();j++){//可用迭代器简化一下,这样就无需bol参数了
                 int primeNum =pnums.get(j);
                 if(i%primeNum==0){//说明此非质数
                     bol=false;
                     break;
                 }
             }
             if(bol){
                 pnums.add(i);
             }
         }
         //System.out.println(Helper.listToStringJSON(pnums));
         int sizeL=pnums.size();
         if(sizeL!=0&&pnums.get(sizeL-1)==num){
             isPrimeNum=true;
         }
         return  isPrimeNum;
     }*/
    public static String[][] tableToArrayOfRows(Table table) {
        List<List<RectangularTextContainer>> tableRows = table.getRows();

        int maxColCount = 0;

        for (int i = 0; i < tableRows.size(); i++) {
            List<RectangularTextContainer> row = tableRows.get(i);
            if (maxColCount < row.size()) {
                maxColCount = row.size();
            }
        }

        Assert.assertEquals(maxColCount, table.getColCount());

        String[][] rv = new String[tableRows.size()][maxColCount];

        for (int i = 0; i < tableRows.size(); i++) {
            List<RectangularTextContainer> row = tableRows.get(i);
            for (int j = 0; j < row.size(); j++) {
                rv[i][j] = table.getCell(i, j).getText();
            }
        }

        return rv;
    }
    //@Test
    public void bb() throws Exception{
        String urll="C:/Users/18722/Desktop/tolg/CRJ/SLOC.pdf";
        File file = new File(urll);
        System.out.println("AV 大\r d\n".replaceAll("\\s", ""));
    }
    //@Test
    public void aa() throws Exception{


        PDDocument document = PDDocument.load(new File("C:/Users/18722/Desktop/tolg/CRJ/SLOC.pdf"));
        //pdf页数(第1页到第200页,此返回200)
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int pagenum=pages.getCount();
        ObjectExtractor oe  = new ObjectExtractor(document);
        //循环页面
        Page page = oe.extract(6);//从1开始,表第一页
			/*
			 //根据区域范围提取内容
			 technology.tabula.Rectangle area = new technology.tabula.Rectangle(60, 20, 460,600);
			 page=page.getArea(area);
			*/
        //根据table分割线,获取数据
        //表是有横线竖线(没横线会认为是同一个单元格内容,多个仅仅空格隔开())
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        //根据流的方式去获取数据
        //BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
        List<Table> a=sea.extract(page);
			//System.out.println(a.size());
			//System.out.println(page.getRulings());
			//此页面数据读取,获取坐标等数据
        String strrr="";
        //取前260个字
        List<TextElement> text2 = page.getText();
        for(int i=0;i<text2.size()&&i<260;i++){
            TextElement textElement = text2.get(i);
            strrr+=textElement.getText();
        }
        System.out.println(strrr);

        /*List<TextElement> text2 = page.getText();
        for(int i=0;i<text2.size();i++){
            TextElement textElement = text2.get(i);
            System.out.print(textElement.getText());
            System.out.print(" X:"+ textElement.getX());
            System.out.print(" Y:"+ textElement.getY());
            System.out.print(" W:"+ textElement.getWidth());
            System.out.println(" H:"+ textElement.getHeight());
        }
        */
        System.out.println("获取table数:"+a.size());
        //循环table
        Table table=a.get(0);
        List<List<String>> rows=new ArrayList<List<String>>();
        List<List<RectangularTextContainer>> tableRows = table.getRows();
        for (int j = 0; j < tableRows.size(); j++) {
            List<RectangularTextContainer> row = tableRows.get(j);
            List<String> rowscol=new ArrayList<String>();
            for (int jj = 0; jj < row.size(); jj++) {
                rowscol.add(table.getCell(j, jj).getText());
            }
            rows.add(rowscol);
        }
        //原数据输出
        for(int ii=0;ii<rows.size();ii++){
            List<String> rowscol=rows.get(ii);
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                //如下输出,会输出展示全,如 System.out.println("sa\rdd"); 输出 dd
                System.out.print(str.replaceAll("\r\n?","<换行>"));
                // \t方便复制到xls时有格式,后期注释掉
                System.out.print("	");
            }
            //当前行结尾,后期注释掉
            System.out.println("*");
        }
    }
    @Test
    public void parsePdf() throws Exception{
        //实际功能注意事项(表的线比较全,直接用 JAVA\字符,字节流+上传下载\操作PDF文件 A处)
            //b.pdf 列MPD ITEM NUMBER 有值的才录入,因为有的表主体第一行 是 ATA(即章节说明)
            //判断是否是最后一行的方法,需要根据需求看一下是否需要更改
            //常用数据记录 colmap 需要 改一下(map里的表头数据需要重新计算吗)

        //常用数据记录(若要改为相同类型表复用此map,需要通过addRe扩展页面初始数据列,然后解析主体数据)
        Map colmap=new HashMap();
        colmap.put("fontindex", 0);//当前字位置下标
        //表头的同一单元格内容,空格隔开的两个字,其实际x位置超过此,认为需要拆分单元格(要double类型)
        colmap.put("intervalMinX", 10.0);
        //colmap.put("deviationX", 3);//允许的误差值-没用上
        //colmap.put("coln", 11);//总列数-没用上
        colmap.put("titn", 0);//表头所在行数,从1开始
        colmap.put("colXY", null);//表头的定位说明
        colmap.put("tableTit", null);//第一行表头的数值暂定,做展示勇 List<String>
        colmap.put("tableNum", 0);//表头字符串长度
        colmap.put("addRe", null);////页面初始数据需要扩展列时,要用到此

        //根据页面page类获取
        PDDocument document=null;
        document = PDDocument.load(new File("C:/Users/18722/Desktop/tolg/CRJ/SLOC.pdf"));
        //pdf页数(第1页到第200页,此返回200)
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int pagenum=pages.getCount();
        ObjectExtractor oe  = new ObjectExtractor(document);
        //此处应该循环页数
        //for(int i=1;i<=pagenum;i++){}
        Page page = oe.extract(16);//从1开始,表第一页
        //区域截取主要用于,表头前垃圾数据的去除功能
        //通过区域去除没必要解析数据,一般留表头用于判断处理,宽高设足些防止数据被截取,导致缺失
        //截止到某值(包含);参1:,参2:,参3:值的X+W-参2(取大整数),参4:值的Y+H-参1(取大整数);(不包含某值Y-参1即可(取小整数))
        //technology.tabula.Rectangle area = new technology.tabula.Rectangle(80, 0, 800,550);//上左宽高
        //page=page.getArea(area);
        //根据流的方式去获取数据
        //BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<Table> talist=sea.extract(page);
        //获取全部数据的值,坐标等信息(不会包括空格)
        List<TextElement> textList= page.getText();
        for(int i=0;i<textList.size();i++){
            TextElement textElement = textList.get(i);
            System.out.print(textElement.getText());//char值
            System.out.print(" X:"+ textElement.getX());//X
            System.out.print(" Y:"+ textElement.getY());//Y
            System.out.print(" W:"+ textElement.getWidth());//宽
            System.out.print(" H:"+ textElement.getHeight());//高
            System.out.println(" 下标:"+ i);
        }
        //循环table(用流方式获取,此一般就一个)
        for(int i=0;i<talist.size();i++){
            Table table=talist.get(i);
            //获取页面数据结构,元素是行数据
            List<List<String>> rows=new ArrayList<List<String>>();
            List<List<RectangularTextContainer>> tableRows = table.getRows();
            for (int j = 0; j < tableRows.size(); j++) {
                List<RectangularTextContainer> row = tableRows.get(j);
                List<String> rowscol=new ArrayList<String>();
                for (int jj = 0; jj < row.size(); jj++) {
                    rowscol.add(table.getCell(j, jj).getText());
                }
                rows.add(rowscol);
            }
            //原数据输出
            for(int ii=0;ii<rows.size();ii++){
                List<String> rowscol=rows.get(ii);
                for(int iii=0;iii<rowscol.size();iii++){
                    String str=rowscol.get(iii);
                    System.out.print(str);
                    // \t方便复制到xls时有格式,后期注释掉
                    System.out.print("	");
                }
                //当前行结尾,后期注释掉
                System.out.println("*");
            }
            //解析的数据结构
            List<List<String>> newrows=new ArrayList<List<String>>();
            //扩展表数据获取(即添加多少列给原数据)
            extendTable(rows,textList,colmap,newrows);
            //newrows遍历输出
            /*for(int ii=0;ii<newrows.size();ii++){
                List<String> rowscol=newrows.get(ii);
                for(int iii=0;iii<rowscol.size();iii++){
                    String str=rowscol.get(iii);
                    System.out.print(str);
                    // \t方便复制到xls时有格式,后期注释掉
                    System.out.print("	");
                }
                //当前行结尾,后期注释掉
                System.out.println("*");
            }*/

        }
    }
    /** 扩展表数据获取(即添加多少列给原数据)
     * list 需要解析的数据
     * colmap 一些关联数据
     * extendTable(11,3)//表前三行是表头,共11列
     */
    public void extendTable(List<List<String>> list,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
        //原理:当检测到单元格里内容间距大于intervalMinX就认为需要拆分单元格
        int titn=(int)colmap.get("titn"); // titn 表头所在行数,从1开始
        double intervalMinX=(double)colmap.get("intervalMinX");
        int fontindex=(int)colmap.get("fontindex");
        LinkedHashMap<String,Integer> addRe=new LinkedHashMap<String,Integer>();  //新增列记录
        for(int i=0;i<titn;i++){ //行循环
            List<String> rowscol=list.get(i);//此是行数据
            for(int ii=0;ii<rowscol.size();ii++){//此是行列循环
                String str=rowscol.get(ii);
                String strNext=null;
                if(ii+1<titn) {
                    strNext=rowscol.get(ii+1);
                }
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");
                }
                String[] arrayStr=str.split(" ");
                //新增列数
                int addcol=0;
                //循环数组
                for(int array=0;array<arrayStr.length;array++){
                    //当前字的描述
                    TextElement textElementF =textList.get(fontindex);
                    String bstr=arrayStr[array];
                    int strleg=bstr.length();
                    if(array>0) {
                        TextElement upF=textList.get(fontindex-1);//上次的尾字的描述
                        double x = upF.getX();
                        double w = upF.getWidth();
                        double xw=x+w;
                        double xx = textElementF.getX();
                        double diff=xx-xw;
                        //System.out.println(i+upF.getText()+textElementF.getText()+ii+";diff:"+diff);
                        if(diff>intervalMinX) {
                            addcol++;
                        }
                    }
                    fontindex=fontindex+strleg;//下一个循环字的坐标
                }
                //此列后面要新增列
                if(addcol>0){
                    if(StringUtils.isBlank(strNext)) {
                        addcol=addcol-1;//其后有空格列则就不新增列,而是把值移过去
                    }
                    Integer integer=addRe.get(ii);
                    if(integer==null) {
                        integer=0;
                    }
                    if(addcol>integer) {
                        addRe.put(String.valueOf(ii), addcol);
                    }
                }
            }
        }
        colmap.put("addRe",addRe);
        //扩展表实现
        extendTableT(list,textList,colmap, newrows);
    }

    //重新新增列
    public void addTableC(List<List<String>> list,Map colmap) {
        LinkedHashMap<String,Integer> addRe=(LinkedHashMap<String,Integer>)colmap.get("addRe");
        for(int i=0;i<list.size();i++){ //行循环
            List<String> rowscol=list.get(i);//此是行数据
            int addA=0;//新增总数
            //给addRe新增列
            for(String key:addRe.keySet()){
                Integer integer = addRe.get(key);
                int keyn=Integer.parseInt(key)+1;
                keyn+=addA;
                for(int k=0;k<integer;k++) {
                    rowscol.add(keyn, "");
                }
                addA+=integer;
            }
        }
    }
    /**
     * 表新增列实现
     */
    public void extendTableT(List<List<String>> list,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
            //重新新增列
        addTableC( list,colmap);
        //表头重新定位,并获取每列的定位
        int titn=(int)colmap.get("titn"); // titn 表头所在行数,从1开始
        double intervalMinX=(double)colmap.get("intervalMinX");
        //每一列表头的具体说明,如定位等
        List<Map> colXY=new ArrayList<Map>();
        //表头的数值描述
        //List<List<String>> tableTit=new ArrayList<List<String>>();
        int fontindex=0;
        for(int i=0;i<titn;i++){ //行循环
            List<String> rowscol=list.get(i);//此是行数据
            for(int ii=0;ii<rowscol.size();ii++){ //此是行列循环
                String str=rowscol.get(ii);
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");
                }
                //赋初始定位
                Map colXYEle=new HashMap();
                //开始节点
                TextElement FTE= textList.get(fontindex);
                colXYEle.put("F",FTE.getX());
                colXYEle.put("FT",FTE.getText());
                //结束节点
                TextElement ETE=textList.get(fontindex+(str.replaceAll(" ","").length()-1));
                colXYEle.put("E",ETE.getX()+ETE.getWidth());
                colXYEle.put("ET",ETE.getText());
                //表头节点内容
                colXYEle.put("T",str);
                int intI=(ii+1)-colXY.size();
                for (int k =intI; k >0; k--){
                    colXY.add(null);
                }
                colXY.set(ii,colXYEle);

                String[] arrayStr=str.split(" ");
                if(arrayStr.length==0){//表头单元格就一个就不往后迁移(即"a b"这种才有单元格拆分的问题 )
                    continue;
                }
                boolean bol=false;
                String valla="";
                String vallb="";
                //循环数组
                for(int array=0;array<arrayStr.length;array++){
                    String bstr=arrayStr[array];
                    if((!bol)&&(array>0)) { //第一个就不对比了
                        //当前字的描述
                        TextElement textElementF =textList .get(fontindex);
                        /*TextElement upF=null;
                        if (fontindex==0){ //防止-1的情况(上if里的array>0就已经排除此了)
                            upF=textList.get(0);
                        }else{
                            upF=textList.get(fontindex-1);//上次的尾字的描述
                        }*/
                        TextElement upF=textList.get(fontindex-1);//上次的尾字的描述
                        double x = upF.getX();
                        double w = upF.getWidth();
                        double xw=x+w;
                        double xx = textElementF.getX();
                        double diff=xx-xw;
                        if(diff>intervalMinX) {
                            //System.out.println(i+upF.getText()+textElementF.getText()+ii+";diff:"+diff);
                            //System.out.println("valla:"+valla);
                            bol=true;
                        }
                    }
                    if(!bol) {
                        valla+=bstr+" ";
                        int strleg=Helper.nvlString(bstr).length();
                        fontindex=fontindex+strleg;//下一个循环字的坐标
                    }else {
                        vallb+=bstr+" ";
                    }

                }
                if(bol) {
                    valla=Helper.nvlString(valla);
                    rowscol.set(ii,valla);
                    int in=ii+1;
                    if(in>=rowscol.size()){//防止set超出下标报错
                        rowscol.add("");
                    }
                    rowscol.set(in,Helper.nvlString(vallb));
                    //修订定位
                    Map colXYEleT=new HashMap();
                    int endf=fontindex-1;
                    //开始节点
                    TextElement FTET=textList.get(endf-(valla.replaceAll(" ","").length()-1));
                    colXYEleT.put("F",FTET.getX());
                    colXYEleT.put("FT",FTET.getText());
                    //结束节点
                    TextElement ETET=textList.get(endf);
                    colXYEleT.put("E",ETET.getX()+ETET.getWidth());
                    colXYEleT.put("ET",ETET.getText());
                    //表头节点内容
                    colXYEleT.put("T",valla);
                    colXY.set(ii,colXYEleT);
                }

            }
            //tableTit.add(rowscol);
        }
        //表头的数值描述
        List<String> tableTit=new ArrayList<String>();
        //中间有空着的,后面的移过来(即[有,null,有]->[有,有]})
        Iterator it=colXY.iterator();
        while(it.hasNext()){//返回true或false
            Map map=(Map)it.next();
            if(map==null){
                it.remove();
            }else{
                String t=(String)map.get("T");
                tableTit.add(t);
            }
        }
        //每一列表头的具体说明,如定位等
        colmap.put("colXY", colXY);
        //表头的数值描述
        colmap.put("tableNum", fontindex);
        /*TextElement tt=textList.get(fontindex);
        System.out.println(tt.getText());*/
        colmap.put("tableTit", tableTit);
        //表实体数据纠正
        extendTableB(list,textList,colmap,newrows);
    }
    //表主体数据纠正
    public void extendTableB(List<List<String>> list,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
        //主体纠正原理:此内容的开始坐标在上个列的结尾后面,在下个列的开头前面,则此内容在这列
        int titn=(int)colmap.get("titn"); // titn 表头所在行数,从1开始
        //每一列表头的具体说明,如定位等
        List<Map> colXY=(List<Map> )colmap.get("colXY");
        //第一行表头的展示(同列以后为准)
        List<String> tableTit=(List<String>)colmap.get("tableTit");
        int tableNum=(int)colmap.get("tableNum");//表头字符串长度
        //赋表头数据(空没去掉,反正此仅作展示用,实际表头处理依据上colXY)
        newrows.add(tableTit);
        //System.out.println(Helper.listToStringJSON(newrows));
        //遍历输出
        for(int ii=titn;ii<list.size();ii++){
            List<String> rowscol=list.get(ii);
            //是否是最后一行
            boolean isEndrow=isEndrow(rowscol);
            if(isEndrow) {
                break;
            }
            List<String> newrow=new ArrayList();
            //赋行初始数据
            for(int n=0;n<colXY.size();n++){
                newrow.add("");
            }
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");//任何空白字符(包括空格、制表符、换页符等等)替换为空格
                }
                String[] b=str.split(" ");
                //单元格里内容根据空格分隔
                for(int array=0;array<b.length;array++){
                    String s=b[array];
                    int strleg=s.length();
                    //当前内容最后一个字位置下标
                    TextElement f=textList.get(tableNum+(strleg-1));
                    double fx=f.getX();
                    //double fxw=f.getX()+f.getWidth();
                    //根据x轴坐标获取放在哪列(从0开始)
                    int index= getIndex(colXY,fx);
                    String val=newrow.get(index);
                    if(StringUtils.isNoneBlank()){
                        val+=" ";
                    }
                    newrow.set(index,val+s);
                    //当前字长度

                    //下一个循环字的坐标
                    tableNum+=strleg;
                }
            }
            newrows.add(newrow);
        }
    }
    //根据x轴坐标获取放在哪列(从0开始),参数x是内容的最后一个字的x(不是xw)
    public int getIndex(List<Map> colXY,double x) {
        //此内容的开始坐标在上个列的结尾后面,在下个列的开头前面,则此内容在这列
        Map endmap=null;
        int size = colXY.size();
        if(size>2){
            endmap=colXY.get(size-2);
        }else{
            endmap=colXY.get(0);
        }
        double ee=(double)endmap.get("E");
        //x直接大于倒数第二列的结束位置,直接放最后一位
        if(x>ee){
            return size-1;
        }
        for(int i=0;i<colXY.size();i++){
            double first=0;
            double end=0;
            if(i>0){
                Map map=colXY.get(i-1);
                first=(double)map.get("E");
            }
            Map mape=colXY.get(i+1);
            end=(double)mape.get("F");
            if(x>first&&x<end){
                return i;
            }
        }
        return 0;
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

}
