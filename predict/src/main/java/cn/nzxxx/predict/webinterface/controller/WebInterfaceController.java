package cn.nzxxx.predict.webinterface.controller;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webinterface.service.InterfaceServiceI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller 接口类
 * @author 子火
 * @Date 2021年3月3日11:14:30
 */
@RestController
@RequestMapping("/interface")
public class WebInterfaceController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InterfaceServiceI interfaceServiceI;

    /**
     *  同步数据到 job_card job_card_tool job_card_materials job_card_reference
     *  localhost:8081/interface/syncJobCard?param=%7BACTYPE:%22CRJ700/900/1000%22,CARDSOURCE:%22CRJ%22,JOBCARDNO:%22000-25-900-101%20(Config%20A43)%22%7D
     *  CARDSOURCE 值: CRJ BOEING  AIRBUS
     * @throws Exception
     */
    @RequestMapping(value="/syncJobCard")
    public String syncJobCard(String param){
        String resstr;
        try{
            if(StringUtils.isBlank(param)){
                resstr=Help.returnClass(500,"参数异常","param值为空");
                return resstr;
            }
            Map map = Helper.stringJSONToMap(param);
            //机型
            String ACTYPE=(String)map.get("ACTYPE");
            //来源
            String CARDSOURCE=(String)map.get("CARDSOURCE");
            //工卡号
            String JOBCARDNO=(String)map.get("JOBCARDNO");
            //参数非空校验
            resstr = Help.return5003Describe(ACTYPE, CARDSOURCE, JOBCARDNO);
            if(resstr!=null){
                return resstr;
            }
            resstr =interfaceServiceI.syncJobCard(ACTYPE, CARDSOURCE, JOBCARDNO);
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
     * 获取所在磁盘信息
     * @author 子火
     * @Date 2019年5月5日12:12:59
     * @return 实体类
     * @throws  Exception 接口若扔出了异常要有此
     */
    @RequestMapping(value="/getDiskInfo")
    public List<Map> getDiskInfo() throws Exception{
        List<Map> list=new ArrayList<>();
        File[] roots = File.listRoots();//获取磁盘分区列表
        for (File file : roots) {
            double totalSize=file.getTotalSpace()/1024/1024/1024;//总空间
            double freeSize=file.getFreeSpace()/1024/1024/1024;//空闲空间
            double usableSize=file.getUsableSpace()/1024/1024/1024;//可用空间
            double usedSize=totalSize-freeSize;//已使用容量
            Map map=new HashMap();
            map.put("fpath",file.getPath());
            map.put("total",totalSize+"G");//总容量
            map.put("free",freeSize+"G");//空闲空间
            map.put("usable",usableSize+"G");//可用空间
            map.put("used",usedSize+"G");//已使用容量-总减空闲
            list.add(map);
        }
        return list;
    }


}
