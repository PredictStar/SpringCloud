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
     *  同步数据到 job_card job_card_tool job_card_materials job_card_reference --翻译数据会存在 job_card_body
     *  localhost:8081/interface/syncJobCard?param=%7BID:%22957%22,CREATEDBY:%22ligeng%22,CARDSOURCE:%22CRJ%22%7D
     *  CARDSOURCE :CRJ;BOEING;AIRBUS (类型)
     *  ID :主键
     *  CREATEDBY :创建人
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
            //主键
            String ID=(String)map.get("ID");
            //类型
            String CARDSOURCE=(String)map.get("CARDSOURCE");
            //创建人
            String CREATEDBY=(String)map.get("CREATEDBY");
            //参数非空校验
            resstr = Help.return5002Describe(ID, CARDSOURCE);
            if(resstr!=null){
                return resstr;
            }
            resstr =interfaceServiceI.syncJobCard(ID, CARDSOURCE, CREATEDBY);
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
     * localhost:8081/interface/airbusTWord?idd=126629494
     * from amms_job_cardbody b where b.cardid ="+idd
     *  生成翻译的airbus word,地址在方法返回值能看到,这个翻译word的地址不会存在数据库
     * @throws Exception
     */
    @RequestMapping(value="/airbusTWord")
    public String airbusTWord(String idd){
        String resstr=Help.returnClass(200,"生成完成","");
        try{
            String translateTaskCard="" ;
            if(StringUtils.isBlank(idd)){
                resstr=Help.returnClass(200,"idd值为空","");
                return resstr;
            }
            if(!Helper.isInt(idd,false)){
                resstr=Help.returnClass(200,"idd不为数字","");
                return resstr;
            }
            int i=Integer.parseInt(idd);
            translateTaskCard= interfaceServiceI.translateAirbusRC(i);
            resstr=Help.returnClass(200,"生成完成",translateTaskCard);

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
