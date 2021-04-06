package cn.nzxxx.predict.webrequest.service.impl;

import cn.nzxxx.predict.business.amms.ammsjobcard.mapper.AmmsJobCardMapper;
import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardBody;
import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.mapper.JobCardBodyMapper;
import cn.nzxxx.predict.webrequest.service.TrInStorageServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service("trInStorageServiceImpl")
public class TrInStorageServiceImpl implements TrInStorageServiceI {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    PdfServiceImpl pdfServiceImpl;
    @Autowired
    private JobCardBodyMapper jobCardBodyMapper;
    /**
     * 入job_card_body库
     * analyPdfM :所有数据
     * translateTemp 需要操作的内容及类型等数据
     * sentenceL 句柄数据
     */

    public ReturnClass transInStorage(Map analyPdfM, LinkedHashMap<String,Map<String,String>> translateTemp, String sentenceL, Integer jobCardId)throws Exception{
        ReturnClass reC=Help.returnClassT(200,"翻译成功并入库成功","");
        //需要入库的数据
        List<JobCardBody> list=new ArrayList<JobCardBody>();
        //结构值获取
        Map sectionsMap=(Map)analyPdfM.get("sections");
        String fileType=(String) analyPdfM.get("fileType");
        List<String> imagesB=(List)analyPdfM.get("imagesB");
        Map tableMap=(Map)analyPdfM.get("tableMap");
        transInStorageSections(sectionsMap,translateTemp,sentenceL,list,fileType,tableMap);
        //图片入数据集
        for(String str: imagesB){
            //图片数据的二次处理
            str=str.replaceFirst("^imageSingle_IMW[^;]+;|^","data:image/png;base64,");
            JobCardBody jcb=new JobCardBody();
            jcb.setBodytype("IMAGE");
            jcb.setBodyval("<div><img src='"+str+"'/></div>");
            list.add(jcb);
        }
        //入库job_card_body
        for(int i=0;i<list.size();i++){
            JobCardBody jcb = list.get(i);
            jcb.setJobcardid(jobCardId);
            double di=i;
            jcb.setOrderby(di);
            jobCardBodyMapper.insert(jcb);
        }
        return reC;
    }

    /**
     * 对 analyPdfM 里的 sections(区块对) 进行获取并翻译
     * translateTemp 需要入库的key
     * sentenceL 翻译句子的集合
     * list 需要入库的数据集
     * fileType 类型如(crj boeing)
     * tableMap 表实际数据
     * Map<String,Map<String,String>> translateTemp
     */
    public void transInStorageSections(Object obj,LinkedHashMap<String,Map<String,String>> translateTemp,String sentenceL,List<JobCardBody> list,String fileType,Map tableMap){
        if(translateTemp==null||obj==null){
            return;
        }
        if(obj instanceof Map){
            Map<String,Object> map=(Map)obj;
            //为了有序所以需要此
            for(String keyy:translateTemp.keySet()){
                if(map.containsKey(keyy)){
                    Map temN=(Map)translateTemp.get(keyy);
                    //存储的数据值
                    String value=(String)map.get(keyy);
                    //类型(txt;image;table)
                    String temType=(String) temN.get("type");
                    if("txt".equals(temType)){
                        //翻译并赋值 BODYVAL MATCHN TRANSLATERES
                        pdfServiceImpl.translateEToC(value,fileType,sentenceL,list);
                    }else if("image".equals(temType)){
                        //图片数据的二次处理
                        value=value.replaceFirst("^imageSingle_IMW[^;]+;|^","data:image/png;base64,");
                        JobCardBody jcb=new JobCardBody();
                        jcb.setBodytype("IMAGE");
                        jcb.setBodyval("<div><img src='"+value+"'/></div>");
                        list.add(jcb);
                    }else if("table".equals(temType)){
                        //value 就是表的key
                        if(tableMap.containsKey(value)){
                            Map tablemap=(Map)tableMap.get(value);
                            //表头 "tabHead" : ["MANUAL_NO", "REFERENCE", "DESIGNATION"],
                            List<String> tabHeadList=(List)tablemap.get("tabHead");
                            //表主体 "tabBody" : [["CSP-B-001", "AMM 35-00-00-910-801", "Oxygen Safety Precautions"]],
                            List<List<String>> tabBodyList=(List)tablemap.get("tabBody");
                            JobCardBody jcb=new JobCardBody();
                            jcb.setBodytype("TXT");
                            StringBuilder sb=new StringBuilder();
                            sb.append("<table border='0' width='100%' cellpadding='0' cellspacing='0'><tbody><tr>");
                            for(String tabHead: tabHeadList){
                                sb.append("<th>"+tabHead+"</th>");
                            }
                            sb.append("</tr>");
                            for(List<String> tabBodyL: tabBodyList){
                                sb.append("<tr>");
                                for(String tabBody: tabBodyL){
                                    sb.append("<td>"+tabBody+"</td>");
                                }
                                sb.append("</tr>");
                            }
                            sb.append("</tbody></table>");
                            String tableEle=sb.toString();
                            jcb.setBodyval("<div>"+tableEle+"</div>");
                            list.add(jcb);
                        }
                    }

                }
            }
            for(Object key:map.keySet()){
                String keyy=(String)key;
                if(translateTemp.containsKey(keyy)){
                   continue;
                }else{
                    Object value=map.get(keyy);
                    if(value instanceof Map||(value instanceof List)){
                        transInStorageSections(value,translateTemp,sentenceL,list,fileType,tableMap);
                    }
                }
            }
        }else if(obj instanceof List){
            List listobj=(List)obj;
            for(int i=0;i<listobj.size();i++){
                Object value = listobj.get(i);
                if((value instanceof Map)||(value instanceof List)){
                    transInStorageSections(value,translateTemp,sentenceL,list,fileType,tableMap);
                }
            }
        }
    }



}
