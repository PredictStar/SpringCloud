package cn.nzxxx.predict.amms.controller;

import cn.nzxxx.predict.amms.service.TaskParserFileService;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/TaskParserAction")
public class TaskParserAction {
    @Autowired
    TaskParserFileService taskParserFileService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * http://localhost:8081/TaskParserAction/createTask?type=aa&ammfileid=1
     * @Title: createTask
     * @Description: 执行解析的方法
     * @param request
     */
    @RequestMapping("/createTask")
    public String createTask(HttpServletRequest request, HttpServletResponse response)throws Exception{
        String ammfileId = request.getParameter("ammfileid");
        String type = request.getParameter("type");
        String result = taskParserFileService.createTask(new Long(ammfileId),type);

        Map<String,String> params = new HashMap<String,String>();
        params.put("RESULT", result);

        String s = Helper.mapToStringJSON(params);
        return s;
    }
    /**
     * http://localhost:8081/TaskParserAction/createWord?idd=126629494
     * 根据 amms_job_card 主键生成 word
     * @param request
     */
    @RequestMapping("/createWord")
    public String createWord(HttpServletRequest request, HttpServletResponse response)throws Exception{
        String res;
        try{
            String idd = request.getParameter("idd");
            res = taskParserFileService.createWord(idd);
        }catch (Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            res=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(res);
        }
        return res;
    }

}
