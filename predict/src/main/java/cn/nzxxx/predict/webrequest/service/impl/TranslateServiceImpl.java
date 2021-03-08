package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import cn.nzxxx.predict.webrequest.service.TranslateServiceI;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("translateService")
@Transactional
public class TranslateServiceImpl implements TranslateServiceI {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
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
                //翻译后的句子
                String sentence = wholeSentence(strP,professional);
                resStr.append(sentence+".");
            }
        }
        String s = resStr.toString();
        s=Helper.trimStringChar(s,'.');
        return s;
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
            word_val=Helper.nvlString(word_val).replaceAll(","," ").replaceAll("\\s+"," ");
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
        String tj="";
        if(StringUtils.isNotBlank(professional)){
            tj=" or word_professional='"+professional+"' ";
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
                " and (word_professional='all'"+tj+")"+
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

    @Override
    /**
     * 句柄翻译sentence(匹配句子表,获取最相近句子及其翻译内容))
     * 后期的优化:使数字不影响匹配,且匹配后能替换翻译结果里的数字
     *  六个单词以下,要100%才表示匹配上
     *  匹配时去掉非必要特征值,如the
     *  professional 专业类型,可以为空(表不查专业类型单词)(查询条件有此则优先选专业类型是其的)
     */
    public String sentenceTranslate(String vall, String professional,Map<Integer, List<Map<String, Object>>> splitSentenceL) {
        if(splitSentenceL==null||splitSentenceL.size()==0){
            //获取所有句柄
            List<Map<String, Object>> allSentence = getAllSentence(professional);
            //句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
            splitSentenceL=splitSentenceL(allSentence);
        }
        StringBuilder resStr=new StringBuilder();
        if(StringUtils.isNotBlank(vall)){
            //段落拆分为句子数组
            String[] paragraphs = vall.split("\\.");
            int paragraphsL=paragraphs.length;
            //先整体翻译
            List<Map<String, Object>> sentenceA=splitSentenceL.get(paragraphs.length-1);
            Integer indexx = sentenceMatch(vall,sentenceA);
            String sentence = getIddV(indexx);
            //当整段就是一句话,就不用逐句翻译了,前已经匹配执行过了
            if(StringUtils.isBlank(sentence)&&paragraphsL>1){
                List<Map<String, Object>> sentence0=splitSentenceL.get(0);
                for(String strP:paragraphs){
                    //句子的获取
                    strP=Helper.nvlString(strP);
                    //翻译后的句子
                    indexx = sentenceMatch(strP,sentence0);
                    sentence = getIddV(indexx);
                    if(StringUtils.isBlank(sentence)){
                        sentence="无此句柄,需自行查询";
                    }
                    if(resStr.length()==0){
                        resStr.append(sentence);
                    }else {
                        resStr.append("。"+sentence);
                    }
                }
            }else{
                resStr.append(sentence);
            }
            if(StringUtils.isBlank(sentence)){
                resStr.append("无此句柄,需自行查询");
            }
        }
        return resStr.toString();
    }
    //匹配查询到的所有句柄,获取匹配最高的那个
    public Integer sentenceMatch(String strP,List<Map<String, Object>> allSentence){
        Integer idd=null;
        //最大匹配度时对应下标,方便提取其它关键数据
        Integer indexx=null;
        //最大匹配度
        double  maxMatchRate=0.0;
        //允许匹配度//如匹配值要大于60%
        ResourceBundle re = java.util.ResourceBundle.getBundle("application");//application.properties里值
        String pro = re.getString("translate.allow.match.rate");
        double allowMatchRate=Double.parseDouble(pro);
        for(int i=0;i<allSentence.size();i++){
            Map<String, Object> stringObjectMap = allSentence.get(i);
            String sentence_val =(String) stringObjectMap.get("sentence_val");

            double reMatchRate = reMatchRate(sentence_val, strP);
            //测试
            /*if(strP.equals("Refer to Figure 1")){
                System.out.println("匹配度:"+reMatchRate);
            }*/
            if(reMatchRate>allowMatchRate){
                if(reMatchRate>maxMatchRate){
                    indexx=i;
                    maxMatchRate=reMatchRate;
                }
            }
        }
        if(indexx!=null){
            Map<String, Object> stringObjectMap = allSentence.get(indexx);
            idd=(Integer)stringObjectMap.get("idd");
        }
        return idd;
    }
    /**
     * 返回俩字符串匹配度,b和a的相似度
     */
    public double reMatchRate(String tempS,String valS){
        double re=0;
        String[] splitTempS = Helper.nvlString(tempS).split(" ");
        String[] splitValS = Helper.nvlString(valS).split(" ");
        //上一行存储数据
        List<Map> oldRecord=new ArrayList();
        //当前行存储数据
        List<Map> nowRecord=new ArrayList();
        for(int i=0;i<splitValS.length;i++){
            //获取目标句的单词并转为小写
            String val=splitValS[i].toLowerCase();
            oldRecord.clear();
            oldRecord.addAll(nowRecord);
            nowRecord.clear();
            for(int j=0;j<splitTempS.length;j++){  //j 是列数
                //获取源句的单词并转为小写
                String temp=splitTempS[j].toLowerCase();
                int le=0;
                String maV="";
                if(temp.equals(val)){
                    //相同单词个数
                    le=1;
                    //最大匹配字符串内容
                    maV=temp;
                    //两个字母相同,值是左上角的值+1,最终值就取末尾值即可
                    if(i!=0&&j!=0){
                        Map map = oldRecord.get(j - 1);
                        le= (int)map.get("le")+1;
                        String m=(String) map.get("maV");
                        if(StringUtils.isBlank(m)){
                            maV= temp;
                        }else{
                            maV= m+" "+temp;
                        }
                    }
                }else{
                    //不同的两个字母,就选择上方和左方单元格值大的那个
                    Map topM=new HashMap();
                    Map leftM=new HashMap();
                    if(i!=0){
                        topM=oldRecord.get(j);
                    }
                    if(j!=0){
                        leftM=nowRecord.get(j-1);
                    }
                    if(topM.size()>0&&leftM.size()>0){
                        int leT= (int)topM.get("le");
                        int leL= (int)leftM.get("le");
                        le= leT;
                        maV=(String) topM.get("maV");
                        if(leL>leT){
                            le= leL;
                            maV=(String) leftM.get("maV");
                        }
                    }else if(topM.size()>0&&leftM.size()==0){
                        le= (int)topM.get("le");
                        maV=(String) topM.get("maV");
                    }else if(topM.size()==0&&leftM.size()>0){
                        le= (int)leftM.get("le");
                        maV=(String) leftM.get("maV");
                    }
                }
                Map newMap=new HashMap();
                newMap.put("le",le);
                newMap.put("maV",maV);
                nowRecord.add(newMap);
            }
        }
        if(nowRecord.size()>0){
            Map dataM=nowRecord.get(nowRecord.size()-1);
            //最长公共子序列的单词个数
            int le= (int)dataM.get("le");
            // of the as 等常用单词 会导致匹配度虚高(现没做任何处理);
            // 考虑到大小写会导致匹配度降低,所以现写法:被改为小写后才去匹配的
            // ,空格 会导致匹配降低,代码里已去掉此影响因素
            int a=splitTempS.length;
            int b=splitValS.length;
            int c=a;
            // 防止因为包含导致的100%匹配
            if(b>a){
              c=b;
            }
            re=(double)le/c;
            /*System.out.println(dataM);
            System.out.println(re);*/
        }
        return re;
    }
    /**
     * 获取所有句柄
     */
    public List<Map<String, Object>> getAllSentence(String professional){
        String tj="";
        if(StringUtils.isNotBlank(professional)){
            tj=" or sentence_professional='"+professional+"'";
        }
        String sql="SELECT\n" +
                "tb.idd,\n" +
                "tb.sentence_val\n" +
                "FROM\n" +
                "translate_sentence_book AS tb\n" +
                "where (tb.sentence_professional='all'"+tj+")";
        List<Map<String, Object>> resList=jdbcTemplate.queryForList(sql);
        return resList;
    }
    /**
     * 句柄根据.拆分(从0开始,即a.b会放在1里,abc放在0里)
     */
    public Map<Integer, List<Map<String, Object>>> splitSentenceL(List<Map<String, Object>> allSentence){
        Map<Integer, List<Map<String, Object>>> reMap=new HashMap<>();
        for(Map<String, Object> ele: allSentence){
            String sentence_val=(String) ele.get("sentence_val");
            sentence_val=Helper.nvlString(sentence_val);
            sentence_val=Helper.trimStringChar(sentence_val,'.');
            sentence_val=sentence_val.replaceAll(","," ").replaceAll("\\s+"," ");
            ele.put("sentence_val",sentence_val);
            //获取.的个数
            int bLeg=sentence_val.length()-sentence_val.replaceAll("\\.","").length();
            List<Map<String, Object>> maps = reMap.get(bLeg);
            if(maps==null){
                maps=new ArrayList<>();
                reMap.put(bLeg,maps);
            }
            maps.add(ele);
        }
        return reMap;
    }
    /**
     * 根据idd获取sentence_chinese
     */
    public String getIddV(Integer idd){
        String re="";
        if(idd!=null){
            String sql="SELECT\n" +
                    "tb.sentence_chinese\n" +
                    "FROM\n" +
                    "translate_sentence_book AS tb\n" +
                    "where tb.idd="+idd;
            List<Map<String, Object>> resList=jdbcTemplate.queryForList(sql);
            if(resList.size()>0){
                Map<String, Object> stringObjectMap = resList.get(0);
                re=(String) stringObjectMap.get("sentence_chinese");
            }
        }
        return re;
    }

}