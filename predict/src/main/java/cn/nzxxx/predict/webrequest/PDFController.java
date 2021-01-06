package cn.nzxxx.predict.webrequest;

import cn.nzxxx.predict.config.pdftable.ParsePdf;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.tool.Helper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/pdf")
public class PDFController {
    private final Logger logger=Logger.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
    /**
     *  http://localhost:8181/pdf/analysis?param=null
     *  urll 文件地址 "C:/Users/18722/Desktop/tolg/CRJ/section2.pdf"
     *  fileName 文件名 "section2.pdf"
     * @return 状态说明
     * @throws Exception
     */
    @RequestMapping(value="/analysis")
    public String analysisInit(String param) throws Exception{
        String resstr;
        try{
            if(StringUtils.isBlank(param)){
                resstr=Help.returnClass(500,"参数异常","param值为空");
                return resstr;
            }
            Map map = Helper.stringJSONToMap(param);
            String urll=(String)map.get("urll");
            //urll="C:/Users/18722/Desktop/tolg/BOEING/HI___100.PDF";//"C:/Users/18722/Desktop/tolg/CRJ/SLOC.pdf";
            String fileName=(String)map.get("fileName");
            //fileName="hi___100.pdf";//"sloc.pdf";
            resstr=Help.return5002Describe(urll,fileName);
            if(resstr!=null){
                return resstr;
            }
            Date sdate=new Date();
            File file = new File(urll);
            InputStream input=new FileInputStream(file);
            //文件名要小写
            fileName=fileName.toLowerCase();
            ParsePdf parPdf=new ParsePdf();
            PDDocument document=parPdf.returnPDDocument(input);
            ObjectExtractor oe  = new ObjectExtractor(document);
            //页面总数
            //每一页单独处理
            int pagenum=parPdf.retPagenum(document);
            int cou=0;//sql执行条数
            //有效页面记录-测试用
            //String yxym="";
            for(int i=1;i<=pagenum;i++){
                Map conditionsMap=new HashMap();
                List<List<String>> newrows=new ArrayList<List<String>>();
                //如果是 sloc.pdf 文件 通过原生表线方式去获取
                if(fileName.equals("sloc.pdf")){
                    newrows= parPdf.reTaData(oe,i,conditionsMap,fileName);
                    if(newrows.size()==0){
                        continue;
                    }
                }else{
                    conditionsMap=parPdf.retCondMap(oe,"x",i,fileName);//页数从1开始
                    if(conditionsMap.size()==0){
                        continue;
                    }
                    newrows= parPdf.parsePdf(conditionsMap);
                }
                String sql=parPdf.retInSql(newrows,conditionsMap);
                if(StringUtils.isBlank(sql)){
                    continue;
                }
                //yxym+=i+";";
                //页面会有未完待续,然后下个页面继续录入的情况,所以提取数据时要注意此情况
                int update = jdbcTemplate.update(sql);
                cou+=update;
            }
            //单独测试某页(测试时一般开启 "数据输出" )
            /*int testpage=83;//s1-46 68  39  s2-24     sloc-5 4 12 649
            Map conditionsMap=new HashMap();
            List<List<String>> newrows=new ArrayList<List<String>>();
            //如果是 sloc.pdf 文件 通过原生表线方式去获取
            if(fileName.equals("sloc.pdf")){
                newrows= parPdf.reTaData(oe,testpage,conditionsMap,fileName);
                if(newrows.size()==0){
                    return "[\"页面未匹配\"]";
                }
            }else{
                conditionsMap=parPdf.retCondMap(oe,"x",testpage,fileName);//页数从1开始
                if(conditionsMap.size()==0){
                    return "[\"拦截页面了\"]";
                }
                newrows= parPdf.parsePdf(conditionsMap);
            }
            String sql=parPdf.retInSql(newrows,conditionsMap);
            System.out.println(sql);
            int update = jdbcTemplate.update(sql);
            cou+=update;*/

            Date edate=new Date();
            String timedes="执行完成;执行时间;"+(edate.getTime()-sdate.getTime())/1000+"s";
            resstr=Help.returnClass(200,timedes,"插入"+cou+"条");
            //关
            parPdf.closed(oe,document,input);

        }catch (Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
            resstr=Help.returnClass(500,"接口异常",strEInfo);
        }
        return resstr;

    }


}
