package cn.nzxxx.predict.webinterface;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller 类
 * @author 子火
 * @Date 2019年5月5日12:12:59
 */
@Controller
public class WebInterfaceController {
    private final Logger logger=Logger.getLogger(this.getClass());

    /**
     * 获取所在磁盘信息
     * @author 子火
     * @Date 2019年5月5日12:12:59
     * @return 实体类
     * @throws  Exception 接口若扔出了异常要有此
     */
    @RequestMapping(value="/getDiskInfo")
    @ResponseBody
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
