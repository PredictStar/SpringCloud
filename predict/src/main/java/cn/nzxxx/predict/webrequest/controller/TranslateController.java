package cn.nzxxx.predict.webrequest.controller;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translate")
public class TranslateController {
    private final Logger logger=Logger.getLogger(this.getClass());
    @Autowired
    private TranslateServiceI translate;
    @RequestMapping(value="/test")
    public String test(String idd){
        System.out.println(idd);
        return "ooo";
    }
    /**
     * 英文转中文  http://localhost:8081/translate/etoc?vall=I have to learn to take care of myself
     * @param professional 专业类型,可以为空(表不查专业类型单词)(查询条件有此则优先选专业类型是其的)
     * @param vall  英文内容
     * @param type	翻译类型(单词翻译word(结合语法推导单词词性获取单个单词含义并汇总)和句子翻译sentence(匹配句子表,获取最相近句子及其翻译内容))
     * @throws Exception
     */
    @RequestMapping(value="/etoc")
    public String etoc(String professional,String vall,String type) throws Exception{
        String resStr="";
        try{
            if(StringUtils.isBlank(type)){
                type="word";    //word 单词翻译 //sentence 句子翻译
            }
            if(StringUtils.isBlank(vall)){
                return "";
            }
            //professional 可以为空
            if("word".equals(type)){
                resStr = translate.wordTranslate(vall,professional);
            }/*else if("sentence".equals(type)){
                resStr = translate.sentenceTranslate(vall,professional);
            }*/
        }catch (Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
        }
        return resStr;
    }
}
