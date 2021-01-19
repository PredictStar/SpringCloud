package cn.nzxxx.predict;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.test;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.HelloController;


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
    @Test
    public void b()throws Exception{
        //549 556

        String a="621BR Fixed Leading−Edge Skin CSP-B-001 AMM 57-41-05-400-802";
        String aa="621BR Fixed Leading−Edge Skin";
        String aaa="Fiyxed";
        List List=new ArrayList();
        List.add("([A-Z0-9]+ )(.+)( \\S+-\\S+-\\S+)( AMM \\S+)");
        List.add("([A-Z0-9]+ )?(.+)()()");
        String pp=Helper.listToStringJSON(List);
        List list = Helper.stringJSONToList(pp);
       /* for(int i=0;i<list.size();i++){
            String s = (String)list.get(i);
            Pattern pattern = Pattern.compile(s);
            Matcher matcher = pattern.matcher(aaa);
            if(matcher.find()){
                System.out.println(matcher.group(0));
                System.out.println(matcher.group(1));
                System.out.println(matcher.group(2));
                System.out.println(matcher.group(3));
                System.out.println(matcher.group(4));*//*
                System.out.println(matcher.group(5));
                System.out.println(matcher.group(6));
                System.out.println(matcher.group(7));
                System.out.println(matcher.group(8));*//*
                break;
            }
        }*/

        Pattern pattern = Pattern.compile("(aa)|(b)|(c)");
        Matcher matcher = pattern.matcher("xabcd");
        if(matcher.find()){
            int groupCount = matcher.groupCount();
            for(int i=1;i<=groupCount;i++){
                String group = matcher.group(i);
                if(StringUtils.isNotBlank(group)){
                    System.out.println(group);
                    break;
                }
            }
        }
    }
    public void clearTag(Object obj){
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            for(Object value:map.values()){
                if(value instanceof Map){
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
     //@Test
     public void a()throws Exception{
         /*try {

         }catch (Exception e){

         }*/
         PDDocument document = PDDocument.load(new File("C:/Users/18722/Desktop/tolg/taskcard/CRJ/CRJ7910MTCM-MAST-R57-V02.pdf"));
         //pdf页数(第1页到第200页,此返回200)
         PDPageTree pages = document.getDocumentCatalog().getPages();
         int pagenum=pages.getCount();
         ObjectExtractor oe  = new ObjectExtractor(document);
         //循环页面
         Page page = oe.extract(40);//从1开始,表第一页
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
             System.out.println("---------------------------");

         }

     }

}
