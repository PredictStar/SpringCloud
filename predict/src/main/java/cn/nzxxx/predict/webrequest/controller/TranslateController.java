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
     * 英文转中文  http://localhost:8081/translate/etoc?vall=wordd
     * @param professional 专业类型(单词匹配后:相同优先级,优先取其对应专业;句子匹配:相同相似度,优先取其对应专业)
     * @param vall  英文内容
     * @param type	翻译类型(单词翻译word(结合语法推导单词词性获取单个单词含义并汇总)和句子翻译sentence(匹配句子表,获取最相近句子及其翻译内容))
     * @throws Exception
     */
    @RequestMapping(value="/etoc")
    public String testBB(String professional,String vall,String type) throws Exception{
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
            }else if("sentence".equals(type)){
                resStr = translate.sentenceTranslate(vall,professional);
            }
        }catch (Exception e){
            String strE=Helper.exceptionToString(e);
            logger.error(strE);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo);
        }
        return resStr;
    }
}
