package cn.nzxxx.predict.webrequest.controller;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HelloController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public int a=2;
    /**
     * http://localhost:8081/test/aa
     */
    @RequestMapping(value="/test/aa")
    @ResponseBody
    public String testBB(String str) throws Exception{
        FormPdf fpdf=new FormPdf();
        int abc = 0;
        System.out.println(a);
        a++;
        /*List list=new ArrayList();
        Map map=new HashMap();
        map.put("value","12");
        map.put("text","ggg");
        Map map2=new HashMap();
        map2.put("value","2222");
        map2.put("text","uuuuu");
        list.add(map);
        list.add(map2);*/
        return "110";
    }
    @RequestMapping("/hello")
    public String hello(ModelMap map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("name", "li");
        map.addAttribute("bookTitle", "近代简史");
        logger.error(System.getProperty("user.dir")+"近代简史");
        return "/hello";
    }
}
