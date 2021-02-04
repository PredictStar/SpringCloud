package cn.nzxxx.predict.webrequest;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.config.pdftable.TablePdf;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import technology.tabula.*;

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
     *  http://localhost:8081/pdf/analysis?param=null
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
            TablePdf parPdf=new TablePdf();
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
     * http://localhost:8081/pdf/executePDFForm
     * @return 状态说明
     * @throws Exception
     */
    @RequestMapping("/executePDFForm")
    public ReturnClass executePDFForm(){
        ReturnClass reC=Help.returnClassT(200,"executePDFForm操作成功","");
        Date sdate=new Date();
        //每次查多少个(最好少于450,in里达到459个的时候就不会用索引了)
        int limitt=2;
        List<Map<String, Object>> pdf = getPDF(limitt);
        if(pdf.size()==0){
            reC=Help.returnClassT(200,"文件表无查询结果","");
        }else{
            for(int i=0;i<pdf.size();i++){
                Map<String, Object> object = pdf.get(i);
                ReturnClass ReC = analysisPDFForm(object);
                //System.out.println(ReC);
                if(!ReC.getStatusCode().equals("200")){
                    //报错回滚数据:使 IS_EXECUTE=0
                    update( pdf,i,0);
                    return ReC;
                }
            }
        }
        Date edate=new Date();
        String timedes="执行时间;"+(edate.getTime()-sdate.getTime())/1000+"s";
        reC.setValueDescribe(timedes);
        return reC;
    }
    //查询需操作文件
    public List<Map<String, Object>> getPDF(int limitt){
        String sql="SELECT\n" +
                "f.AMM_FILE_ID,\n" +
                "f.FILENAME,\n" +
                "f.FILETYPE,\n" +
                "f.AMMPATH\n" +
                "FROM\n" +
                "amm_file AS f\n" +
                "WHERE\n" +
                "f.IS_EXECUTE = 0\n" +
                "LIMIT "+limitt;
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        if(re.size()>0){
            //占数据,使 IS_EXECUTE=1
            int updateN=update( re,0,1);
            if(updateN!=re.size()){
                System.out.println("数据争取存在!!!");
            }
        }
        return re;
    }
    public int update(List<Map<String, Object>> re,int init,int IS_EXECUTE){
        String strin="";
        for(int i=init;i<re.size();i++){
            Map<String, Object> object = re.get(i);
            Integer key=(Integer) object.get("AMM_FILE_ID");
            if(key==null){
                key=0;
            }
            if(i==0){
                strin=String.valueOf(key);
            }else {
                strin+=","+String.valueOf(key);
            }
        }
        String updatesql="update amm_file set IS_EXECUTE="+IS_EXECUTE+" where AMM_FILE_ID in ("+strin+");";
        int update = jdbcTemplate.update(updatesql);
        return update;
    }
    public ReturnClass analysisPDFForm(Map pdfMap){
        ReturnClass reC=Help.returnClassT(200,"analysisPDFForm操作成功","");
        String urll=(String)pdfMap.get("AMMPATH");
        //文件存储的上级文件夹名,这样就能通过文件夹指定工卡通过此pdf生成的,存的文件名是工卡表主键(如 CRJ_CARD BOEING_CARD)
        String folderName=(String)pdfMap.get("FILENAME");
        String fileType=(String)pdfMap.get("FILETYPE");
        Integer AMM_FILE_ID=(Integer)pdfMap.get("AMM_FILE_ID");
        ReturnClass reP=Help.return5003DescribeT(urll,folderName,fileType);
        if(reP!=null){
            return reP;
        }
        try{
            Date sdate=new Date();
            File file = new File(urll);
            InputStream input=new FileInputStream(file);
            //初始化FormPdf类
            FormPdf fpdf=new FormPdf();
            fpdf.setFileType(fileType);
            PDDocument document=fpdf.returnPDDocument(input);
            ObjectExtractor oe  = new ObjectExtractor(document);
            //页面总数(从1开始)
            int pagenum=fpdf.retPagenum(document);
            //是否是一条完整的解析(一个word可能由多个pdf页构成)
            int num=0;
            //解析pdf,后赋word所需内容
            Map<String,Object> analyPdfM=new HashMap<String,Object>();
            //提取值规则定义
            List<Map<String,Object>> ruleList=fpdf.getNewRule();
            //循环所有pdf页 -暂时先循环一次
            //for(int i=107;i<=pagenum;i++){ //测试-后期去掉
            for(int i=1;i<=pagenum;i++){
                Page page=fpdf.retPageC(oe,i);
                //当前页的类型(1:word的首页;2:需解析的页面;)
                int pageTypeN = fpdf.pageType(page);
                if(pageTypeN==0&&analyPdfM.size()==0){ //去掉无用的页面(在数据后的0是图)
                    continue;
                }
                //测试-后期去掉
               /*if(i==150){
                    i=pagenum;
                }*/
                if(pageTypeN==1){
                    //之前解析好的数据,生成word,入数据库
                    if(analyPdfM.size()!=0){
                        //生成word,入数据库
                        reC=fpdf.run(folderName,analyPdfM,AMM_FILE_ID,jdbcTemplate);
                        //清空analyPdfM
                        analyPdfM=new HashMap<String,Object>();
                        ruleList=fpdf.getNewRule();
                        num++;
                    }
                }
                //解析PDF
                fpdf.analyPdfToMap(page,document,i,analyPdfM,pageTypeN,ruleList);
                if(i==pagenum){ //最后一页
                    if(analyPdfM.size()!=0){
                        //生成word,入数据库
                        reC=fpdf.run(folderName,analyPdfM,AMM_FILE_ID,jdbcTemplate);
                        //清空analyPdfM
                        analyPdfM=new HashMap<String,Object>();
                        ruleList=fpdf.getNewRule();
                        num++;
                    }
                }
                if(!reC.getStatusCode().equals("200")){
                    return reC;
                }
            }
            //关
            fpdf.closed(oe,document,input);
            Date edate=new Date();
            String timedes=";执行时间;"+(edate.getTime()-sdate.getTime())/1000+"s";
            return Help.returnClassT(200,"解析"+folderName+"成功","生成个数:"+num+timedes);
        }catch(Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
            reC=Help.returnClassT(500,"解析"+folderName+"异常",strEInfo);
            return reC;
        }
    }
}
