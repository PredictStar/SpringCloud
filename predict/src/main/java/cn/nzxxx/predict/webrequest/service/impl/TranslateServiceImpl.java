package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("translateService")
@Transactional
public class TranslateServiceImpl implements TranslateServiceI {
    private final Logger logger=Logger.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

    @Override
    /**
     * 单词翻译word(结合语法推导单词词性获取单个单词含义并汇总)
     */
    public String wordTranslate(String vall,String professional) {
        StringBuilder resStr=new StringBuilder();
        if(StringUtils.isNotBlank(vall)){
            //段落拆分为句子数组
            String[] paragraphs = vall.split("\\.");
            for(String strP:paragraphs){
                //句子的获取
                strP=Helper.nvlString(strP).replaceAll("\\s+"," ");
                //翻译后的句子
                String sentence = wholeSentence(strP,professional);
                resStr.append(sentence+"。");
            }
        }
        return resStr.toString();
    }

    @Override
    public String sentenceTranslate(String vall, String professional) {
        return null;
    }

    /**
     * 整句的翻译
     */
    public String wholeSentence(String sentence,String professional){
        StringBuilder resStr=new StringBuilder();
        String[] words = sentence.split(" ");
        for(int i=0;i<words.length;i++){
            String word = words[i];
            //根据单词查询匹配的单词
            List<Map> getword= getword(word);
            if(getword.size()>0){
                Integer wordBookIdd = getwordPP(getword, words, i);
                Map wordItem = getWordItem(wordBookIdd);
                String word_chinese=(String) wordItem.get("word_chinese");

            }
        }

        return resStr.toString();
    }
    /**
     * 根据单词查询匹配的单词
     */
    public List<Map> getword(String word){
        List<Map> resList=new ArrayList<>();


        return resList;
    }
    /**
     * 根据translate_word_book主键获取相关单词信息
     */
    public Map getWordItem(int word_book_idd){
        Map resMap=new HashMap();


        return resMap;
    }
    /**
     * 获取实际匹配值的主键
     */
    public Integer getwordPP(List<Map> getword,String[] words,int i){
        Integer reInt=null;


        return reInt;
    }
}