package cn.nzxxx.predict.webrequest;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.config.pdftable.ParsePdf;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.*;
import com.deepoove.poi.policy.ListRenderPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.pdfbox.jbig2.SegmentData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    /**
     *  http://localhost:8181/pdf/pdfToWord?param=null
     *  urll pdf存储地址 "C:/Users/18722/Desktop/tolg/CRJ/section2.pdf"
     *  fileName 被解析pdf的文件名(不要.后缀)
     *  fileType 文件类型,表适应哪种模板(现有 crj boeing)
     * @return 状态说明
     * @throws Exception
     */
    @RequestMapping("/pdfToWord")
    public String pdfToWord(String param, String picData){
        String resstr;
        ReturnClass reC=Help.returnClassT(200,"接口操作成功","");
        try{
            if(StringUtils.isBlank(param)){
                resstr=Help.returnClass(500,"参数异常","param值为空");
                return resstr;
            }
            Map map = Helper.stringJSONToMap(param);
            String urll=(String)map.get("urll");
            //测试-后期去掉
            urll="C:/Users/18722/Desktop/tolg/taskcard/CRJ/CRJ7910MTCM-MAST-R57-V02.pdf";
            String fileName=(String)map.get("fileName");
            //测试-后期去掉
            fileName="CRJ7910MTCM-MAST-R57-V02";
            String fileType=(String)map.get("fileType");
            //测试-后期去掉
            fileType="crj";
            resstr=Help.return5003Describe(urll,fileName,fileType);
            if(resstr!=null){
                return resstr;
            }
            Date sdate=new Date();
            File file = new File(urll);
            InputStream input=new FileInputStream(file);
            //初始化FormPdf类
            FormPdf fpdf=new FormPdf();
            PDDocument document=fpdf.returnPDDocument(input);
            ObjectExtractor oe  = new ObjectExtractor(document);
            //页面总数(从1开始)
            int pagenum=fpdf.retPagenum(document);
            //是否是一条完整的解析(一个word可能由多个pdf页构成)
            int num=0;
            //解析pdf,后赋word所需内容
            Map<String,Object> analyPdfM=new HashMap<String,Object>();
            //提取值规则定义
            List<Map<String,Object>> ruleList=fpdf.getNewRule(fileType);
            //循环所有pdf页 -暂时先循环一次
            for(int i=151;i<=pagenum;i++){ //测试-后期去掉
            //for(int i=1;i<=pagenum;i++){
                Page page=fpdf.retPageC(oe,i);
                //当前页的类型(1:word的首页;2:需解析的页面;)
                int pageTypeN = fpdf.pageType(page, urll, fileName, fileType);
                if(pageTypeN==0&&analyPdfM.size()==0){ //去掉无用的页面(在数据后的0是图)
                    continue;
                }
                //测试-后期去掉
                i=pagenum;
                if(i==pagenum){ //最后一页
                    //解析PDF
                    fpdf.analyPdfToMap(page,document,i,analyPdfM,pageTypeN,ruleList);
                    if(analyPdfM.size()!=0){
                        //生成word,入数据库
                        reC=fpdf.run(page,urll,fileName,fileType,analyPdfM);
                        //清空analyPdfM
                        analyPdfM=new HashMap<String,Object>();
                        ruleList=fpdf.getNewRule(fileType);
                        num++;
                    }
                }else{
                    if(pageTypeN==1){
                        if(analyPdfM.size()!=0){
                            //生成word,入数据库
                            reC=fpdf.run(page,urll,fileName,fileType,analyPdfM);
                            //清空analyPdfM
                            analyPdfM=new HashMap<String,Object>();
                            ruleList=fpdf.getNewRule(fileType);
                            num++;
                        }
                    }
                    //解析PDF
                    fpdf.analyPdfToMap(page,document,i,analyPdfM,pageTypeN,ruleList);
                }
                if(!reC.getStatusCode().equals("200")){
                    return Helper.pojoToStringJSON(reC);
                }
                //测试-后期去掉
                if(i==156){
                    i=pagenum;
                }
            }
            //关
            fpdf.closed(oe,document,input);
            return Help.returnClass(200,"接口操作成功","生成个数:"+num);
        }catch(Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
            resstr=Help.returnClass(500,"pdfToWord接口异常",strEInfo);
            return resstr;
        }
    }
}
