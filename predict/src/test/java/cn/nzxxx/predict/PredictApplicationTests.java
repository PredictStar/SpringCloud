package cn.nzxxx.predict;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.test;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;


import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import com.deepoove.poi.data.style.TableStyle;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xmlunit.util.Convert;
import sun.misc.BASE64Encoder;
import sun.misc.GC;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//引入spring对JUnit4的支持
@RunWith(SpringRunner.class)
@SpringBootTest
public class PredictApplicationTests {
    private Logger log = LoggerFactory.getLogger(this.getClass());	//import org.slf4j.LoggerFactory;

    /*@Value("${spring.freemarker.charset}")
    private String springbootUrl;*/
     //判断正整数是否是质数,是则返回true
     /*public boolean isPrimeNum(int num){
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
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void b()throws Exception{
        String a="3 O-ring 21-51-40-01-025 BEJ 001, 002, 004, 005,";
        String aa="21-51-40-01-025 BEJ 001, 002, 004, 005,";
        String aaa="Removal (P/B 201)";
        String aaaa="Formerly Chromate - Synthetic Rubber) B-2 0";

        List<String> tableMB1=new ArrayList<String>();
        tableMB1.add("([0-9A-Z]+ )?((\\S[^A-Z \\-]*$|\\S[^A-Z \\-]* |\\S-\\S[^A-Z ]+ ?|[A-Z][a-z]+-[A-Z][a-z]+ ?|BMS |NSBT ?|GPL\\S+ ?|DC-\\S+ ?|MS\\S+ ?)+)([A-Z]{1}[0-9A-Z\\-]+.*)?");

        for(int i=0;i<tableMB1.size();i++){
            String s = (String)tableMB1.get(i);
            Pattern pattern = Pattern.compile(s);
            Matcher matcher = pattern.matcher(aaaa);
            if(matcher.find()){
                //System.out.println(matcher.group(0));
                System.out.println(matcher.group(1));
                System.out.println(matcher.group(2));
                System.out.println(matcher.group(4));
                /*System.out.println(matcher.group(2));
                System.out.println(matcher.group(3));
                System.out.println(matcher.group(4));*/
                /*
                System.out.println(matcher.group(4));
                System.out.println(matcher.group(5));
                System.out.println(matcher.group(6));
                System.out.println(matcher.group(7));
                System.out.println(matcher.group(8));*/
                break;
            }
        }
        /*String a="YDALLTY";
        String aa="AMM 21-25-03-000-801 Recirculation Fan Check Valve Removal (P/B 401)";
        String aaa="ALL DA";
        String rowsetStr="";
        Pattern pattern = Pattern.compile("(AMM \\S+)( .+)");
        Matcher matcherRowset = pattern.matcher(aa);
        //Matcher matcherRowset = pattern.matcher("LEFT WING 1.1 15000 FH 15010 FH ");
        int groupCount = matcherRowset.groupCount();
        if(matcherRowset.find()){

            for(int gc=1;gc<=groupCount;gc++){
                //for(int gc=0;gc<=groupCount;gc++){
                String group = matcherRowset.group(gc);
                //System.out.println(matcherRowset.group(gc));
                if(StringUtils.isNotBlank(group)){
                    System.out.println(gc);
                    rowsetStr=group;
                    break;//
                }
            }
        }
        System.out.println(matcherRowset.group(0));
        System.out.println(rowsetStr);*/

    }
    @Test
    public void bracketM(){
        String urlV="table_imageSingle_IMW:100IMH:200IMEND;图片值";
       /* Pattern pattern = Pattern.compile("imageSingle_IMW:(\\d+)IMH:(\\d+)IMEND;(.+)");
        Matcher matcherRowset = pattern.matcher(urlV);
        if(matcherRowset.find()){
            String v1 = matcherRowset.group(1);
            String v2 = matcherRowset.group(2);
            String v3 = matcherRowset.group(3);
            System.out.println(v1);
            System.out.println(v2);
            System.out.println(v3);
        }*/
        System.out.println("CRJ_ST3".indexOf("CRJ_ST"));
        System.out.println("CRJ_ST3".indexOf("CRJ_ST")!=-1);

    }
    @Test
    public void opWord(){
        try{
            String templatePath = "D:/SpringCloud/predict/src/main/resources/META-INF/resources/wordtemplate/"+"fftReport.docx";
            System.out.println( ResourceUtils.getURL("classpath:").getPath());
            XWPFTemplate template = XWPFTemplate.compile(templatePath);//模板里占位符 {{@pics}}  {{abc}}

            Map<String, Object> params = new HashMap<String, Object>();
            //put的key可不存在于模板,不报错
            params.put("abc", "xce\r\n   shix");  //会带值进(换行和多个空格生效)  {{abc}}


            //{{#tablee}} 如下只是例子  A处
            //表的模板名
            String tempKey="tablea";
            //表头值
            String[] tabHead={"a","b","x"};
            String[] b1={"1","2","3"};
            String[] b2={"11","22","33"};
            //表主体
            List<String[]> tabBody=new ArrayList<>();
            tabBody.add(b1);
            tabBody.add(b2);
            List<RowRenderData> talist=new ArrayList<RowRenderData>();
            RowRenderData row0 = Rows.of(tabHead).textBold().bgColor("607D8B").center().create();//颜色即: #607D8B
            talist.add(row0);
            for(int i=0;i<tabBody.size();i++){
                String[] strings = tabBody.get(i);
                RowRenderData tablRrow = Rows.of(strings).create();
                talist.add(tablRrow);
            }
            //设置合并单元格(假如有一个表头,两行数据,i是行数0:表头行,1:第一行数据,为3(此时超出)保错;j是列数,0是第一列,超出也会报错)
            //合并时,会覆盖值;如下 1,1会覆盖1,2的值
            MergeCellRule rule = MergeCellRule.builder().map(MergeCellRule.Grid.of(1, 1), MergeCellRule.Grid.of(1, 2)).build();
            //rule=null;//设置为null不影响
            RowRenderData[] rowRenderData = talist.toArray(new RowRenderData[talist.size()]);
            params.put(tempKey, Tables.of(rowRenderData).mergeRule(rule).create());

            // 赋值
            template.render(params);

            //直接本地生成,就不用OutputStream了

			//保存后的文件夹位置(要事先存在)
			String saveUrl="C:/Users/18722/Desktop/tolg/cord/word/";
			// 创建文件夹
			File file = new File(saveUrl);
			if (!file.exists()) {
				file.mkdirs();
			}
			template.writeToFile(saveUrl+"测试.docx");
            template.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
     @Test
     public void a()throws Exception{
         /*try {

         }catch (Exception e){

         }*/
         PDDocument document = PDDocument.load(new File("C:/Users/18722/Desktop/tolg/taskcard/BOEING/23___073.PDF"));
         //pdf页数(第1页到第200页,此返回200)
         PDPageTree pages = document.getDocumentCatalog().getPages();
         int pagenum=pages.getCount();
         ObjectExtractor oe  = new ObjectExtractor(document);
         //循环页面
         Page page = oe.extract(43);//从1开始,表第一页  67
			/*
			 //根据区域范围提取内容
			 technology.tabula.Rectangle area = new technology.tabula.Rectangle(60, 20, 460,600);
			 page=page.getArea(area);
			*/
         //根据table分割线,获取数据
         //表是有横线竖线(没横线会认为是同一个单元格内容,多个仅仅空格隔开())
         //SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
         //根据流的方式去获取数据
         BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
         List<Table> tabs=sea.extract(page);
         System.out.println("获取table数:"+tabs.size());
			/*System.out.println(page.getRulings());
			//此页面数据读取,获取坐标等数据
			List<TextElement> text2 = page.getText();
			for(int i=0;i<text2.size();i++){
				TextElement textElement = text2.get(i);
				System.out.print(textElement.getText());
				System.out.print(" X:"+ textElement.getX());
				System.out.print(" Y:"+ textElement.getY());
				System.out.print(" W:"+ textElement.getWidth());
				System.out.println(" H:"+ textElement.getHeight());
			}*/

         //循环table
         for(int i=0;i<tabs.size();i++){
             Table table=tabs.get(i);
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
             /*for(int ii=0;ii<rows.size();ii++){
                 List<String> rowscol=rows.get(ii);
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
    @Test
    public void l()throws Exception{
        PDDocument document = PDDocument.load(new File("C:/Users/18722/Desktop/tolg/CRJ/section10.pdf"));
        //pdf页数(第1页到第200页,此返回200)
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int pagenum=pages.getCount();
        ObjectExtractor oe  = new ObjectExtractor(document);
        //循环页面
        Page page = oe.extract(5);//从1开始,表第一页  67
			/*
			 //根据区域范围提取内容
			 technology.tabula.Rectangle area = new technology.tabula.Rectangle(60, 20, 460,600);
			 page=page.getArea(area);
			*/
        //根据table分割线,获取数据
        //表是有横线竖线(没横线会认为是同一个单元格内容,多个仅仅空格隔开())
        //SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        //根据流的方式去获取数据
        BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
        List<Table> tabs=sea.extract(page);
        System.out.println("获取table数:"+tabs.size());
			/*System.out.println(page.getRulings());
			//此页面数据读取,获取坐标等数据
			List<TextElement> text2 = page.getText();
			for(int i=0;i<text2.size();i++){
				TextElement textElement = text2.get(i);
				System.out.print(textElement.getText());
				System.out.print(" X:"+ textElement.getX());
				System.out.print(" Y:"+ textElement.getY());
				System.out.print(" W:"+ textElement.getWidth());
				System.out.println(" H:"+ textElement.getHeight());
			}*/

        //循环table
        for(int i=0;i<tabs.size();i++){
            Table table=tabs.get(i);
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
            /*for(int ii=0;ii<rows.size();ii++){
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
            }*/
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
        }

    }

}
