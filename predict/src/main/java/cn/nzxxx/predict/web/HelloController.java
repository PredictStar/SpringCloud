package cn.nzxxx.predict.web;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {
    private final Logger logger=Logger.getLogger(this.getClass());

    @Autowired
    private DiscoveryClient client;

    @RequestMapping("/hello")
    public String index(){
        return "Hello World!!";
    }


    @RequestMapping(value="/test/aa")
    @ResponseBody
    public List testBB(String str) throws Exception{
        System.out.println(str+"xxxx");
        List list=new ArrayList();
        Map map=new HashMap();
        map.put("value","12");
        map.put("text","ggg");
        Map map2=new HashMap();
        map2.put("value","2222");
        map2.put("text","uuuuu");
        list.add(map);
        list.add(map2);
        return list;
    }
}
