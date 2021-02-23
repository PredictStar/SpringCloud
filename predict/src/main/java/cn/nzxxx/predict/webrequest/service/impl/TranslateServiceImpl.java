package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import org.apache.commons.lang3.StringUtils;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;
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
                    resStr.append(sentence+"    ");
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
            List<Map<String, Object>> getword= getword(word);
            if(getword.size()>0){
                Map<String,Object> paraMap=new HashMap();
                paraMap.put("index",i);
                paraMap.put("words",words);
                paraMap.put("wordsList",getword);
                Integer wordBookIdd = getwordPP(paraMap);
                i=(Integer)paraMap.get("index");
                Map wordItem = getWordItem(wordBookIdd,professional,null);
                String word_chinese=(String) wordItem.get("word_chinese");
                if(StringUtils.isBlank(word_chinese)){
                    continue;
                }
                resStr.append(word_chinese);
            }else {
                resStr.append(word+" ");
            }
        }
        String s = Helper.nvlString(resStr.toString());
        return s;
    }
    /**
     * 根据单词查询匹配的单词集合
     */
    public List<Map<String, Object>> getword(String word){
        List<Map<String, Object>> resList=new ArrayList<>();
        if(StringUtils.isBlank(word)){
            return resList;
        }
        String sql="SELECT\n" +
                "idd,\n" +
                "word_val\n" +
                "FROM\n" +
                "translate_word_book\n" +
                "where word_val like LCASE('"+word+"%')\n"+
                "ORDER BY  length(word_val) desc";
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        for(Map<String, Object> map: re){
            Integer idd=(Integer)map.get("idd");
            String word_val=(String)map.get("word_val");
            Map<String, Object> resMap=new HashMap<>();
            resMap.put("idd",idd);
            word_val=Helper.nvlString(word_val).replaceAll("\\s+"," ");
            resMap.put("word_val",word_val);
            resList.add(resMap);
        }
        return resList;
    }
    /**
     * 获取实际匹配值的主键
     */
    public Integer getwordPP(Map<String,Object> paraMap){
        Integer reInt=null;
        //以当前单词开头的所有符合数据
        List<Map<String, Object>> wordsList=(List)paraMap.get("wordsList");
        //当前单词的下标
        int index=(int)paraMap.get("index");
        //把整句拆分成数组
        String[] words=(String[])paraMap.get("words");
        int wordsLeg=words.length;
        for(Map<String, Object> map: wordsList){
            Integer idd=(Integer)map.get("idd");
            String word_val=(String)map.get("word_val");
            String[] split_word_val = word_val.split(" ");
            int spLeg=split_word_val.length-1;
            if(spLeg<0){
                spLeg=0;
            }
            int wleg=index+spLeg;
            if(wleg>wordsLeg){
                continue;
            }
            StringBuilder sb=new StringBuilder();
            for(int i=index;i<=wleg;i++){
                String word = words[i];
                //转为小写与数据库里的比对
                word=word.toLowerCase();
                sb.append(word+" ");
            }
            //去除两边空格,并转为小写与数据库存值比较
            String s = Helper.nvlString(sb.toString()).toLowerCase();
            if(s.equals(word_val)){
                paraMap.put("index",wleg);
                reInt=idd;
                break;
            }
        }

        return reInt;
    }
    /**
     * 根据translate_word_book主键获取相关单词信息
     * word_class_V 值如 n
     */
    public Map getWordItem(Integer word_book_idd,String professional,String word_class_V){
        Map<String, Object> resMap=new HashMap();
        if(word_book_idd==null){
            return resMap;
        }
        String tj=" ) ";
        if(StringUtils.isNotBlank(professional)){
            tj=" or word_professional='"+professional+"') ";
        }
        String sql="SELECT\n" +
                "idd,\n" +
                "word_book_idd,\n" +
                "word_class,"+
                "word_chinese,\n" +
                "word_order,\n" +
                "word_professional\n" +
                "FROM\n" +
                "translate_word_book_item\n" +
                "where word_book_idd=" +word_book_idd+
                " and (word_professional='all' "+tj+
                "ORDER BY word_professional desc,word_order desc";
        List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
        boolean bol=true;
        for(Map<String, Object> map: re){
            if(bol){
                setMapp(map,resMap);
            }
            bol=false;
            if(StringUtils.isNotBlank(word_class_V)){ //词性优先
                String word_class=(String)map.get("word_class");
                if(word_class_V.equals(word_class)){
                    setMapp(map,resMap);
                    break;
                }
            }else{
                break;
            }

        }
        return resMap;
    }
    //map 赋值
    public void setMapp(Map<String, Object> map,Map<String, Object> resMap){
        Integer idd=(Integer)map.get("idd");
        resMap.put("idd",idd);
        String word_chinese=(String)map.get("word_chinese");
        resMap.put("word_chinese",word_chinese);
    }

}