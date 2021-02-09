package cn.nzxxx.predict.config.pdftable;

import cn.nzxxx.predict.toolitem.tool.Helper;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.jboss.logging.Logger;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.length;

//后期可直接指定表头列，如第一列是啥，进行优化(称为定制版(人为自定义表头),现在写的叫标准版(自动解析表头))
//解析pdf,table数据
public class TablePdf {
    private Map<String,Map<String,Object>> mapp=new HashMap<String,Map<String,Object>>();
    private List<Map<String,String>> matchList=new ArrayList<Map<String,String>>();
    //上一次的 colXY 值,当此次表头不对,colXY值直接取此,有值就不覆盖了(即就赋一次)
    private Map<String,List<Map>> colXYS=new HashMap<String,List<Map>>();
    //根据文件名称(全小写)获取对应的表名
    //若此返回"" 则不会覆盖,初始的配置 mapHi.put("tabnam","BOEING_HI");//表名的
    //如上是为了防止一个paf 文件,需要存不同表
    public TablePdf() {
        //页面会有未完待续,然后下个页面继续录入的情况,所以提取数据时要注意此情况

        //解析 SLOC.pdf -线
        Map mapSloc=new HashMap();
        String typeSloc="sloc";
        mapSloc.put("type",typeSloc);
        List<String> listSloc=new ArrayList<String>();//列定义
        listSloc.add("MPD_TASK_NUMBER");
        listSloc.add("CODE");
        listSloc.add("DATA_ELEMENT");
        listSloc.add("PREVIOUS_DATA");
        listSloc.add("NEW_DATA");
        listSloc.add("REMARKS");
        mapSloc.put("tabcols",listSloc);
        //当根据线去(即原生)获取初始数据,判断这个表是否就是想要的
        //校验第一行的第几列(从0开始)
        mapSloc.put("fCloN",0);
        //被校验列的值
        mapSloc.put("fCloV","MPDTASKNUMBER");
        //mapSloc.put("tabnam","CRJ_SLOC");//表名(此是旧写法,现根据文件名找表 getTN 方法)
        mapp.put(typeSloc,mapSloc);

        //解析 section1.pdf,section2.pdf,section3.pdf -流 (此被舍弃)
        Map mapCrjS=new HashMap();
        String typeCrjS="crjs";
        mapCrjS.put("titn",4);//表头所占行数,从1开始
        mapCrjS.put("intervalMinX",10.0);
        mapCrjS.put("delrow",5);//表头前垃圾行数,从1开始
        mapCrjS.put("type",typeCrjS);
        List<String> listCrjS=new ArrayList<String>();//列定义
        listCrjS.add("TASK_NUMBER");
        listCrjS.add("TASK_EFFECTIVITY");
        listCrjS.add("TASK_TYPE_DESCRIPTION");
        listCrjS.add("INTERVAL");
        listCrjS.add("SSTGS");
        mapCrjS.put("tabcols",listCrjS);
        //mapCrjS.put("tabnam","CRJ_S1");//表名(此是旧写法,现根据文件名找表 getTN 方法)
        //当表头列结束的位置后还有字符串属于此列,需如下配置
        //表头列的结束位置修正(值是double 会 加长此列的结束位置,字符串left 表:当字符串处于列A列B的夹角,值放列A)
        Map<Integer,Object> matcollrmap=new HashMap<Integer,Object>();
        matcollrmap.put(1,30.0);//key从0开始
        matcollrmap.put(2,46.0);
        matcollrmap.put(3,"left");//不写默认是右
        mapCrjS.put("endcols",matcollrmap);
        //赋colXY时,其不被后覆盖(即下表头值不会被后影响)  List<String>
        //解决如 section2.pdf 表头,表头列往右跑(即"原数据输出"应该在第三列,却和第四列内容在一起)
        List<String> offsetcol=new ArrayList<String>();
        offsetcol.add("DESCRIPTION");
        mapCrjS.put("offsetcol",offsetcol);
        mapp.put(typeCrjS,mapCrjS);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        //适用 section1.pdf section3.pdf
        Map<String,String> matchmapS13= new HashMap<String,String>();
        matchmapS13.put("tag",typeCrjS);
        matchmapS13.put("val","MPDMPDSOURCETASKAPPLICABILITY");
        matchList.add(matchmapS13);
        //适用 section2.pdf
        Map<String,String> matchmapS2= new HashMap<String,String>();
        matchmapS2.put("tag",typeCrjS);
        matchmapS2.put("val","MPDMPDINTERVALSOURCETASKAPPLICABILITY");
        matchList.add(matchmapS2);

        //解析 HI___100.pdf  -流
        Map mapHi=new HashMap();
        String typeHi="hi";
        mapHi.put("titn",1);//表头所占行数,从1开始
        mapHi.put("intervalMinX",10.0);
        mapHi.put("delrow",2);//表头前垃圾行数,从1开始
        mapHi.put("type",typeHi);
        List<String> listHi=new ArrayList<String>();//列定义
        listHi.add("LOCATION_OF_CHANGE");
        listHi.add("DESCRIPTION_OF_CHANGE");
        mapHi.put("tabcols",listHi);
        //mapHi.put("tabnam","BOEING_HI");//表名(此是旧写法,现根据文件名找表 getTN 方法)
        mapp.put(typeHi,mapHi);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchmapHi= new HashMap<String,String>();
        matchmapHi.put("tag",typeHi);
        matchmapHi.put("val","LOCATIONOFCHANGEDESCRIPTIONOFCHANGE");
        matchList.add(matchmapHi);

        //解析 BOEING/01___100.pdf  -流
        Map mapb1=new HashMap();
        String typeB1="b1";
        mapb1.put("titn",4);//表头所占行数,从1开始
        mapb1.put("intervalMinX",10.0);
        mapb1.put("delrow",4);//表头前垃圾行数,从1开始
        mapb1.put("type",typeB1);//对应下 mapp.put(typea,mapa);
        List<String> listB1=new ArrayList<String>();//列定义
        listB1.add("MPD_ITEM_NUMBER");
        listB1.add("AMM_REFERENCE");
        listB1.add("CAT");
        listB1.add("TASK");
        listB1.add("THRESH");
        listB1.add("REPEAT");
        listB1.add("ZONE");
        listB1.add("ACCESS");
        listB1.add("APL");
        listB1.add("ENG");
        listB1.add("MAN_HOURS");
        listB1.add("TASK_DESCRIPTION");
        mapb1.put("tabcols",listB1);
        //解析的表头中,此值应该在第几个(从1开始)
        //下表头值直接赋值到某(从1开始)
        Map<String,Integer> colIB1=new HashMap<String,Integer>();
        colIB1.put("ZONE",7);
        colIB1.put("ACCESS",8);
        colIB1.put("APL",9);
        colIB1.put("ENG",10);
        colIB1.put("HOURS",11);
        colIB1.put("TASKDESCRIPTION",12);
        mapb1.put("colI",colIB1);
        mapp.put(typeB1,mapb1);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchmapb1= new HashMap<String,String>();
        matchmapb1.put("tag",typeB1);
        matchmapb1.put("val","MPDCAITEMAMMASMAN");
        matchList.add(matchmapb1);

        //解析 BOEING/02___100.pdf  -流
        Map mapb2=new HashMap();
        String typeB2="b2";
        mapb2.put("titn",4);//表头所占行数,从1开始
        mapb2.put("intervalMinX",10.0);
        mapb2.put("delrow",2);//表头前垃圾行数,从1开始
        mapb2.put("type",typeB2);//对应下 mapp.put(typea,mapa);
        List<String> listb2=new ArrayList<String>();//表列定义
        listb2.add("MPD_ITEM_NUMBER");
        listb2.add("AMM_REFERENCE");
        listb2.add("PGM");
        listb2.add("ZONE");
        listb2.add("ACCESS");
        listb2.add("THRESH");
        listb2.add("REPEAT");
        listb2.add("APL");
        listb2.add("ENG");
        listb2.add("MAN_HOURS");
        listb2.add("TASK_DESCRIPTION");
        mapb2.put("tabcols",listb2);
        //mapa.put("tabnam","BOEING_02");//表名(此是旧写法,现根据文件名找表 getTN 方法)
        mapp.put(typeB2,mapb2);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchmapb2= new HashMap<String,String>();
        matchmapb2.put("tag",typeB2);
        matchmapb2.put("val","MPDPINTERVALAPPLICABILITY");
        matchList.add(matchmapb2);


        //解析 BOEING/03___100.pdf  -流
        Map mapb3=new HashMap();
        String typeB3="b3";
        mapb3.put("titn",4);//表头所占行数,从1开始
        mapb3.put("intervalMinX",10.0);
        mapb3.put("delrow",2);//表头前垃圾行数,从1开始
        mapb3.put("type",typeB3);//对应下 mapp.put(typea,mapa);
        List<String> listB3=new ArrayList<String>();//表列定义
        listB3.add("MPD_ITEM_NUMBER");
        listB3.add("AMM_REFERENCE");
        listB3.add("ZONE");
        listB3.add("ACCESS");
        listB3.add("THRESH");
        listB3.add("REPEAT");
        listB3.add("APL");
        listB3.add("ENG");
        listB3.add("MAN_HOURS");
        listB3.add("TASK_DESCRIPTION");
        mapb3.put("tabcols",listB3);
        //mapa.put("tabnam","BOEING_03");//表名(此是旧写法,现根据文件名找表 getTN 方法)
        mapp.put(typeB3,mapb3);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchmapb3= new HashMap<String,String>();
        matchmapb3.put("tag",typeB3);
        matchmapb3.put("val","MPDINTERVALAPPLICABILITY");
        matchList.add(matchmapb3);

        //解析 CRJ -section3.pdf(同7,9,11,12)  -流
        String typeST3="CRJ_ST3";
        Map mapST3=new HashMap();
        mapST3.put("titn",2);//表头所占行数,从1开始
        mapST3.put("intervalMinX",10.0);
        mapST3.put("delrow",4);//表头前垃圾行数,从1开始
        mapST3.put("type",typeST3);//对应下 mapp.put(typea,mapa);
        List<String> listST3=new ArrayList<String>();//表列定义
        listST3.add("task_card_number");
        listST3.add("task_number");
        listST3.add("amm_amtoss_reference");
        listST3.add("task_card_title");
        listST3.add("interval_crj");
        listST3.add("access");
        listST3.add("task_type");
        listST3.add("skill");
        listST3.add("mhr");
        mapST3.put("tabcols",listST3);
        //新增特殊列的处理
        mapST3.put("uuid","unique_identifier");
        mapST3.put("havaFileNa","file_name");
        Map<String,Integer> colIST3=new HashMap<String,Integer>();
        colIST3.put("TASKCARDTITLE",4);
        colIST3.put("INTERVAL",5);
        colIST3.put("ACCESS",6);
        colIST3.put("TYPE",7);
        colIST3.put("SKILL",8);
        colIST3.put("MHR",9);
        mapST3.put("colI",colIST3);
        Map<Integer,Object> matcolST3=new HashMap<Integer,Object>();
        matcolST3.put(3,"left");//key从0开始
        mapST3.put("endcols",matcolST3);
        mapp.put(typeST3,mapST3);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchMapST3= new HashMap<String,String>();
        matchMapST3.put("tag",typeST3);
        matchMapST3.put("val","TASKCARDTASKAMMAMTOSSTASK");
        matchList.add(matchMapST3);

        //解析 CRJ -section4.pdf(同6) section5.pdf-流
        String typeST4="CRJ_ST4";
        Map mapST4=new HashMap();
        mapST4.put("titn",2);//表头所占行数,从1开始
        mapST4.put("intervalMinX",10.0);
        mapST4.put("delrow",4);//表头前垃圾行数,从1开始
        mapST4.put("type",typeST4);//对应下 mapp.put(typea,mapa);
        List<String> listST4=new ArrayList<String>();//表列定义
        listST4.add("task_card_number");
        listST4.add("task_number");
        listST4.add("task_card_title");
        listST4.add("interval_crj");
        listST4.add("access");
        listST4.add("task_type");
        listST4.add("work_area");
        listST4.add("skill");
        listST4.add("mhr");
        mapST4.put("tabcols",listST4);
        //新增特殊列的处理
        mapST4.put("uuid","unique_identifier");
        mapST4.put("havaFileNa","file_name");
        Map<String,Integer> colIST4=new HashMap<String,Integer>(); //(从1开始)
        colIST4.put("TASKCARDTITLE",3);
        colIST4.put("INTERVAL",4);
        colIST4.put("ACCESS",5);
        colIST4.put("TYPE",6);
        colIST4.put("AREA",7);
        colIST4.put("SKILL",8);
        colIST4.put("MHRS",9); //section4的是此
        colIST4.put("MNHRS",9);//5的是此,两者差异仅如此
        mapST4.put("colI",colIST4);
        Map<Integer,Object> matcolST4=new HashMap<Integer,Object>();
        matcolST4.put(2,30.0);//key从0开始
        mapST4.put("endcols",matcolST4);
        mapp.put(typeST4,mapST4);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchMapST4= new HashMap<String,String>();
        matchMapST4.put("tag",typeST4);
        matchMapST4.put("val","TASKCARDTASKTASKWORK");
        matchList.add(matchMapST4);

        //解析 CRJ -section10.pdf -流
        String typeST10="CRJ_ST10";
        Map mapST10=new HashMap();
        mapST10.put("titn",2);//表头所占行数,从1开始
        mapST10.put("intervalMinX",10.0);
        mapST10.put("delrow",5);//表头前垃圾行数,从1开始
        mapST10.put("type",typeST10);//对应下 mapp.put(typea,mapa);
        List<String> listST10=new ArrayList<String>();//表列定义
        listST10.add("task_number");
        listST10.add("task_description");
        listST10.add("task_number_p");
        listST10.add("task_card_number_p");
        listST10.add("task_interval_p");
        mapST10.put("tabcols",listST10);
        //新增特殊列的处理
        mapST10.put("uuid","unique_identifier");
        mapST10.put("havaFileNa","file_name");
        Map<Integer,Object> matcolST10=new HashMap<Integer,Object>();
        matcolST10.put(1,"left");//key从0开始
        mapST10.put("endcols",matcolST10);
        mapp.put(typeST10,mapST10);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchMapST10= new HashMap<String,String>();
        matchMapST10.put("tag",typeST10);
        matchMapST10.put("val","PRECLUDEDBYZONALINSPECTIONPROGRAM");
        matchList.add(matchMapST10);

        //解析 CRJ -section8.pdf-流
        String typeST8="CRJ_ST8";
        Map mapST8=new HashMap();
        mapST8.put("titn",4);//表头所占行数,从1开始
        mapST8.put("intervalMinX",10.0);
        mapST8.put("delrow",5);//表头前垃圾行数,从1开始
        mapST8.put("delrowOffset", Arrays.asList(0,-1));
        mapST8.put("type",typeST8);//对应下 mapp.put(typea,mapa);
        List<String> listST8=new ArrayList<String>();//表列定义
        listST8.add("task_card_number");
        listST8.add("task_number");
        listST8.add("task_card_title");
        listST8.add("task_type");
        listST8.add("skill");
        listST8.add("threshold");
        listST8.add("repeat");
        listST8.add("manhrs");
        mapST8.put("tabcols",listST8);
        //新增特殊列的处理
        mapST8.put("uuid","unique_identifier");
        mapST8.put("havaFileNa","file_name");
        Map<String,Object> colIST8=new HashMap<String,Object>(); //(从1开始)
        colIST8.put("TASKCARDTITLE",3);
        colIST8.put("TYPE",4);
        colIST8.put("SKILL",5);
        List list_F_ST8=new ArrayList();//当列名相同如下设置;个数要和匹配时对应,不做处理可设置为0
        list_F_ST8.add(6);list_F_ST8.add(7);
        colIST8.put("(FLTCYCLES)",list_F_ST8);
        colIST8.put("HRS",8);
        mapST8.put("colI",colIST8);
        Map<Integer,Object> matcolST8=new HashMap<Integer,Object>();
        matcolST8.put(2,"left");//key从0开始
        mapST8.put("endcols",matcolST8);
        List<String> offsetcolST8=new ArrayList<String>();
        offsetcolST8.add("TYPE");
        offsetcolST8.add("SKILL");
        mapST8.put("offsetcol",offsetcolST8);
        mapp.put(typeST8,mapST8);
        //根据流解析获取数据才用到,用于判断是否是要解析的表
        Map<String,String> matchMapST8= new HashMap<String,String>();
        matchMapST8.put("tag",typeST8);
        matchMapST8.put("val","TASKCARDTITLESKILL51000lbMTOWMAN,TASKCARDTASKTASK51000lbMTOWMAN");
        matchList.add(matchMapST8);



        /*流解析
            titn    //表头所占行数,从1开始
            intervalMinX //列最小间距
            delrow  //表头前垃圾行数,从1开始
            delrowOffset
                当 val 设多匹配如 "A,B,C"
                匹配A和B时,delrow值不同,此时需要偏移纠正时,设此
                mapST8.put("delrowOffset", Arrays.asList(0,-1,1));
                值是List,如左:B的delrow值是:原值-1;C的delrow值是:原值+1
            type    //匹配标记
            tabcols //表列
            uuid 若表中列是uuid,其列名叫什么
            offsetcol
                //赋colXY时,其不被后覆盖(即下表头值不会被后影响)  List<String>
                //解决如 section2.pdf 表头,表头列往右跑(即"原数据输出"应该在第三列,却和第四列内容在一起)
                值示例
                    List<String> offsetcol=new ArrayList<String>();
                    offsetcol.add("DESCRIPTION");
            colI
                //解析的表头中,此值应该在第几个(从1开始),下表头值直接赋值到某(从1开始)
                //一般设置如第七列是某8,9,10...也去设置
                //猜 后会覆盖前,用 offsetcol 解决(一般情况下如8位置的单词本来要覆盖7,但其直接被指定到8了就没有覆盖了)
                注意若是多个单词做表头,设置时值不能有空格!!!
                值示例
                    Map<String,Integer> colIB1=new HashMap<String,Integer>();
                    colIB1.put("ZONE",7);
                    colIB1.put("ACCESS",8);
                    List list_F_ST8=new ArrayList();//当列名相同如下设置;个数要和匹配时对应,不做处理可设置为0
                    list_F_ST8.add(6);list_F_ST8.add(7);
                    colIB1.put("(FLTCYCLES)",list_F_ST8);
            endcols
                //当表头列结束的位置后还有字符串属于此列,需如下配置
                //表头列的结束位置修正(值是double 会 加长此列的结束位置,字符串left 表:当字符串处于列A列B的夹角,值放列A)
                值示例
                    Map<Integer,Object> matcolST4=new HashMap<Integer,Object>();
                    matcolST4.put(1,30.0);//key从0开始
                    matcolST4.put(2,46.0);
                    matcolST4.put(3,"left");//不写默认是右

            匹配,用于确定 匹配标记
                val 匹配值(多匹配写为 "A,B";匹配是根据 indexOf)     tag 值同 上 type
          线
			type
			tabcols
			fCloN,fCloV用于校验功能
			fCloN
				//当根据线去(即原生)获取初始数据,判断这个表是否就是想要的
				//校验第一行的第几列(从0开始)
			fCloV
				//被校验列的值

		  现根据文件名去找表名 getTN 方法
		  结束匹配 参考 isEndrow 方法
         */

    }

    protected static final Logger logger = Logger.getLogger(TablePdf.class);
    //根据文件名获取表名
    private String getTN(String fileName){
        //表名-根据文件名去定义(全小写校验)
        // BOEING_01 BOEING_02 BOEING_03 BOEING_HI
        // CRJ_S1 CRJ_S2 CRJ_S3 CRJ_SLOC
        String tabN="";
        if("01___100.pdf".equals(fileName)){
            tabN="BOEING_01";
        }else if("02___100.pdf".equals(fileName)){
            tabN="BOEING_02";
        }else if("03___100.pdf".equals(fileName)){
            tabN="BOEING_03";
        }else if("hi___100.pdf".equals(fileName)){
            tabN="BOEING_HI";
        }else if("section1.pdf".equals(fileName)){
            tabN="CRJ_S1";
        }else if("section2.pdf".equals(fileName)){
            tabN="CRJ_S2";
        }/*else if("section3.pdf".equals(fileName)){ tabN="CRJ_S3";}*/
        else if("sloc.pdf".equals(fileName)){
            tabN="CRJ_SLOC";
        }else if("section3.pdf".equals(fileName)||"section7.pdf".equals(fileName)||"section9.pdf".equals(fileName)||"section11.pdf".equals(fileName)||"section12.pdf".equals(fileName)){
            tabN="crj_st1";
        }else if("section4.pdf".equals(fileName)||"section5.pdf".equals(fileName)||"section6.pdf".equals(fileName)){
            tabN="crj_st2";
        }else if("section10.pdf".equals(fileName)){
            tabN="crj_st3";
        }else if("section8.pdf".equals(fileName)){
            tabN="crj_st4";
        }
        return tabN;
    }

    /**
     * 返回 PDDocument
     * @author 子火
     * @Date 2020-12-23
     * @return  PDDocument
     */
    public PDDocument returnPDDocument(InputStream input)throws Exception{
        //根据页面page类获取
        PDDocument document = PDDocument.load(input);
        return document;
    }
    /**
     * 返回页面总数
     * @author 子火
     * @Date 2020-12-23
     * @return  页面总数
     */
    public int retPagenum(PDDocument document)throws Exception{
        //pdf页数(第1页到第200页,此返回200)
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int pagenum=pages.getCount();
        return pagenum;
    }

    /**
     * 此是解析结构数据返回sq,过滤页面数据写此处
     * 返回此页的插入sql
     * @author 子火
     * @Date 2020-12-23
     * @return  页面插入sql(返回空表拦截了,直接 continue;)
     */
    public String retInSql(List<List<String>> newrows,Map conditionsMap,String uuid,String fileName)throws Exception{
        //表头list获取,用于校验那个列缺失
        List<String> tabTList = newrows.get(0);

        String sql="";
        StringBuilder zdB=new StringBuilder("");
        String type=(String)conditionsMap.get("type");
        String tabnam=(String)conditionsMap.get("tabnam");
        String uuidC=(String)conditionsMap.get("uuid");
        String havaFileNa=(String)conditionsMap.get("havaFileNa");
        List<String> tabcols=(List<String>)conditionsMap.get("tabcols");
        for(int i=0;i<tabcols.size();i++){
            String tabcol= "`"+tabcols.get(i)+"`";
            if(i==0){
                zdB.append(tabcol);
            }else{
                zdB.append(","+tabcol);
            }
        }
        if(StringUtils.isNotBlank(uuidC)){
            zdB.append(",`"+uuidC+"`");
        }
        if(StringUtils.isNotBlank(havaFileNa)){
            zdB.append(",`"+havaFileNa+"`");
        }
        //sql要的list
        List<List<String>> sqllist=new ArrayList<List<String>>();
        //第一列有值,才认为是一条需要插入的数据,其它列有值但第一列无值则认为和上是同一条数据
        //"b".equals(type)||"hi".equals(type) 现模板都需如下,所以直接写true
        if(true){
            for(int i=1;i<newrows.size();i++){ //循环行(正式数据从1开始,0是表头)
                List<String> strings = newrows.get(i);
                for(int ii=0;ii<strings.size();ii++){//循环列
                    String s = strings.get(ii);
                    if(StringUtils.isBlank(s)){ //此列无值continue
                        continue;
                    }else{
                        s=s.replaceAll("'","‘");//单引号转为中文的单引号
                    }
                    //插入数据过滤
                    if(("hi".equals(type))&&(ii==0)&&(s.indexOf("SECTION")!=-1)){
                        break;//退出二重循环
                    }if(("sloc".equals(type))&&(ii==0)&&(s.indexOf("(Continued)")!=-1)){
                        s=s.replaceAll("\\(Continued\\)","");
                    }
                    if(ii==0){ //第一列有值,才认为是一条需要插入的数据
                        List<String> col=new ArrayList<String>();
                        //赋初始值
                        for(int j=0;j<strings.size();j++){
                            col.add("");
                        }
                        col.set(ii,s);
                        sqllist.add(col);
                    }else {
                        int size = sqllist.size();
                        if(size>0){
                            //获取最后一个
                            List<String> strings1 = sqllist.get(size - 1);
                            //原有值,拼一起,换行符 表示换行
                            String s1 = strings1.get(ii);
                            if(StringUtils.isNotBlank(s1)){
                                s1=s1+"\\r\\n"+s;
                                strings1.set(ii,s1);
                            }else{
                                s1=s;
                            }
                            strings1.set(ii,s1);
                        }
                    }
                }
            }
        }
        for(int i=0;i<sqllist.size();i++){ //每一行
            List<String> sqlrow = sqllist.get(i);
            int sdiff=tabcols.size()-sqlrow.size();
            if(sdiff>0){
                //列补全计划
                for(int c=0;c<tabcols.size();c++){
                    String t = tabcols.get(c);
                    if(c>=sqlrow.size()){
                        sqlrow.add("");
                        continue;
                    }
                    String s = tabTList.get(c).replaceAll(" ","_");
                    if(t.indexOf(s)==-1){
                        tabTList.add(c,"");
                        sqlrow.add(c,"");
                        sdiff=sdiff-1;
                        if(sdiff==0){
                            break;
                        }
                    }
                }
            }
            String val="(";//(2,值)  此行对应的插入内容
            for(int c=0;c<tabcols.size();c++){
                String sval = sqlrow.get(c);
                //特殊符修改防止入库为?
                sval=sval.replaceAll("−","-");
                String s = "'"+sval+"'";
                if(c==0){
                    val=val+s;
                }else{
                    val=val+","+s;
                }
            }
            if(StringUtils.isNotBlank(uuidC)){
                val=val+",'"+uuid+"'";
            }
            if(StringUtils.isNotBlank(havaFileNa)){
                val=val+",'"+fileName+"'";
            }
            val=val+")";
            if(i==0){
                sql="insert into "+tabnam+"("+zdB.toString()+") values "+val;
            }else{
                sql=sql+","+val;
            }
        }
        return sql;
    }

    public List<List<String>> reTaData(ObjectExtractor oe,int pageN,Map conditionsMap,String fileName){
        String tag="";
        if(fileName.equals("sloc.pdf")){
            tag="sloc";
        }
        Map<String,Object> m=mapp.get(tag);
        for(String k:m.keySet()){
            Object v=m.get(k);
            conditionsMap.put(k,v);
        }
        if(conditionsMap.size()!=0){
            String tabnam=getTN(fileName);
            conditionsMap.put("tabnam",tabnam);
        }
        if(conditionsMap.size()==0){
            return new ArrayList<List<String>>();
        }
        int fCloN=(Integer) conditionsMap.get("fCloN");
        String fCloV=(String) conditionsMap.get("fCloV");
        //根据table分割线,获取数据
        //表是有横线竖线(没横线会认为是同一个单元格内容,多个仅仅空格隔开())
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Page page = oe.extract(pageN);
        List<Table> tabs=sea.extract(page);
        //System.out.println("获取table数:"+tabs.size());
        List<List<String>> returnrows=new ArrayList<List<String>>();
        //此page当多个表时
        for (int t = 0; t < tabs.size(); t++) {
            //当前table
            Table table=tabs.get(t);
            //转为可输出使用的数据
            List<List<String>> rows=new ArrayList<List<String>>();
            //初始解析出来的数据
            List<List<RectangularTextContainer>> tableRows = table.getRows();
            for (int j = 0; j < tableRows.size(); j++) {
                List<RectangularTextContainer> row = tableRows.get(j);
                List<String> rowscol=new ArrayList<String>();
                for (int jj = 0; jj < row.size(); jj++) {
                    String cellT=Helper.nvlString(table.getCell(j, jj).getText());
                    cellT=cellT.replaceAll("\r(?!\n)","\r\n");
                    rowscol.add(cellT);
                }
                rows.add(rowscol);
            }
            if(rows.size()>0){
                List<String> strings = rows.get(0);
                if(strings.size()>0){
                    String s = strings.get(fCloN);
                    if(StringUtils.isNotBlank(s)){
                        s=s.replaceAll("\\s", "");
                        if(s.equals(fCloV)){
                            //表格数据方式获取的提取数据输出
                            /*for(int ii=0;ii<rows.size();ii++){
                                List<String> rowscol=rows.get(ii);
                                for(int iii=0;iii<rowscol.size();iii++){
                                    String str=rowscol.get(iii);
                                    //如下输出,会输出展示全,如 System.out.println("sa\rdd"); 输出 dd
                                    System.out.print(str.replaceAll("\r\n?","<换行>"));
                                    // \t方便复制到xls时有格式,后期注释掉
                                    System.out.print("	");
                                }
                                //当前行结尾,后期注释掉
                                System.out.println("*");
                            }*/
                            returnrows=rows;
                            return returnrows;
                        }
                    }

                }
            }
        }
        return returnrows;
    }
    /**
     * 获取表类型,返回适合这个表的动态参数,对页面的拦截最好写此处
     * 返回map.size()==0 表此页过滤掉
     * @author 子火
     * @Date 2020-12-23
     * @return  Map
     */
    public Map retCondMap(ObjectExtractor oe,String type,int pageN,String fileName)throws Exception{
        Map conditionsMap=new HashMap();
        Page page = oe.extract(pageN);
        List<TextElement> text2 = page.getText();
        //过滤空
        text2 =text2.stream().filter(n -> {
            String text = n.getText();
            if(StringUtils.isNotBlank(text)){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        //如果是x 表,当前页面需要动态解析去确定走什么标准
        if("x".equals(type)){
            String str="";
            //取前260个字
            for(int i=0;i<text2.size()&&i<260;i++){
                TextElement textElement = text2.get(i);
                str+=textElement.getText();
            }
            //System.out.println(str);
            //(值是XX-XXX-XX此页需过滤掉 ,那是演示样例)
            if(str.indexOf("XX-XXX-XX")!=-1){
                //System.out.println("拦截XX-XXX-XX");
                return conditionsMap;
            }else if(str.indexOf("ABCDE")!=-1){//(section10,12.pdf里的演示样例)
                return conditionsMap;
            }
            boolean bol=true;
            String tag="";
            for(Map<String,String> mapp: matchList){
                String strMatchO=mapp.get("val");
                String[] split = strMatchO.split(",");
                for(int i=0;i<split.length;i++){
                    String strMatch=split[i];
                    //int i="青春无悔".indexOf("春无");返回1;
                    int ind = str.indexOf(strMatch);
                    if(ind!=-1){
                        bol=false;
                        tag=mapp.get("tag");
                        conditionsMap.put("delrowOffsetIndex",i);
                        break;//停止二重循环
                    }
                }
                if(!bol){
                    break;//停止一重循环
                }
            }
            if(!bol){
                //数值赋给新map,防止值传递
                Map<String,Object> m=mapp.get(tag);
                for(String k:m.keySet()){
                    Object v=m.get(k);
                    conditionsMap.put(k,v);
                }
                conditionsMap.put("textList",text2);
                conditionsMap.put("page",page);
            }
        }else{
            //已知此页就是符合标准的可type值直接指定标准,一般方便测试用
            Map<String,Object> m=mapp.get(type);
            for(String k:m.keySet()){
                Object v=m.get(k);
                conditionsMap.put(k,v);
            }
            conditionsMap.put("textList",text2);
            conditionsMap.put("page",page);
        }
        String tabnam=getTN(fileName);
        if(conditionsMap.size()!=0){
            conditionsMap.put("tabnam",tabnam);
        }
        return conditionsMap;
    }


    /**
     * 解析pdf当前页数据,并返回
     * @author 子火
     * @Date 2020-12-23
     * @param conditionsMap 各种可变参数
     * @param--Page 页面对象
     * @return  此页面数据解析后的list集合
     * @throws  Exception 接口若扔出了异常要有此
     */
    public List<List<String>> parsePdf(Map conditionsMap) throws Exception{
        //实际功能注意事项
        //  (表的线比较全,用 JAVA\字符,字节流+上传下载\操作PDF文件 A处 ,否则用此 )
        //isEndrow(是否是最后一行) 方法,需要根据需求看一下是否需要更改

        //数据记录(若要改为相同类型表复用此map,需要通过addRe扩展页面初始数据列,然后才能解析主体数据)
        Map colmap=new HashMap();
        colmap.put("fontindex", 0);//当前字位置下标
        //表头的同一单元格内容,空格隔开的两个字,其实际x位置超过此,认为需要拆分单元格(要double类型)
        colmap.put("intervalMinX", conditionsMap.get("intervalMinX"));
        colmap.put("titn", conditionsMap.get("titn"));//表头所占行数,从1开始
        colmap.put("colXY", null);//表头的定位说明 List<Map>
        colmap.put("tableTit", null);//第一行表头的数值暂定,做展示用 List<String>
        colmap.put("tableNum", 0);//表头字符串长度
        colmap.put("addRe", null);////页面初始数据需要扩展列时,要用到此
        colmap.put("tabcols", conditionsMap.get("tabcols"));//数据库需要存的列数(从1开始)
        colmap.put("type", conditionsMap.get("type"));//此为表的类型
        //作用见上 mapCrjS.put("offsetcol",offsetcol);
        List<String> offsetcol=(List<String>)conditionsMap.get("offsetcol");
        if(offsetcol==null){
            offsetcol=new ArrayList<String>();
        }
        colmap.put("offsetcol", offsetcol);
        //作用见上  mapb1.put("colI",colIB1);
        Map<String,Object> colI=(Map<String,Object>)conditionsMap.get("colI");
        if(colI==null){
            colI=new HashMap<String,Object>();
        }
        colmap.put("colI", colI);
        //表头列的结束位置修正(即加长了此列的位置)
        colmap.put("endcols", conditionsMap.get("endcols"));
        List<TextElement> textList=(List<TextElement>)conditionsMap.get("textList");
        Page page=(Page)conditionsMap.get("page");
        Integer delrow=(Integer)conditionsMap.get("delrow");//表头前(垃圾数据)所占行数
        if(delrow==null){
            delrow=0;
        }
        List<Integer> delrowOffset=(List)conditionsMap.get("delrowOffset");
        if(delrowOffset!=null&&delrowOffset.size()>0){
            Integer delrowOffsetIndex=(Integer)conditionsMap.get("delrowOffsetIndex");
            if(delrowOffsetIndex!=null){
                Integer integer = delrowOffset.get(delrowOffsetIndex);
                //纠正 delrow 值
                delrow=delrow+integer;
            }
        }
        //根据流的方式去获取数据
        BasicExtractionAlgorithm sea = new BasicExtractionAlgorithm();
        List<Table> talist=sea.extract(page);
        //循环table(用流方式获取,此一般就一个)
        Table table=talist.get(0);
        //获取页面数据结构,元素是行数据
        List<List<String>> rows=new ArrayList<List<String>>();
        List<List<RectangularTextContainer>> tableRows = table.getRows();
        int indexx=0;
        //被排除的字体个数
        for(int i=0;i<delrow;i++){
            List<RectangularTextContainer> rectangularTextContainers = tableRows.get(i);
            for (int rec = 0; rec < rectangularTextContainers.size(); rec++) {
                String text = rectangularTextContainers.get(rec).getText().replaceAll("\\s", "");
                indexx+=text.length();
            }
        }
        for(int i=0;i<indexx;i++){
            textList.remove(0);
        }
        for(int i=0;i<delrow;i++){
            tableRows.remove(0);
        }
        for (int j = 0; j < tableRows.size(); j++) {
            List<RectangularTextContainer> row = tableRows.get(j);
            List<String> rowscol=new ArrayList<String>();
            for (int jj = 0; jj < row.size(); jj++) {
                rowscol.add(row.get(jj).getText());
            }
            rows.add(rowscol);
        }
        //对 rows 纠正 使和 List<TextElement> textList 字顺序统一


        //原数据输出
        /*for(int ii=0;ii<rows.size();ii++){
            List<String> rowscol=rows.get(ii);
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                System.out.print(str);
                // \t方便复制到xls时有格式,后期注释掉
                System.out.print("	");
            }
            //当前行结尾,后期注释掉
            System.out.println("*");
        } System.out.println("-------------------------------");*/
        //解析后的数据结构声明
        List<List<String>> newrows=new ArrayList<List<String>>();
        //扩展表数据获取(即添加多少列给原数据)
        extendTable(rows,textList,colmap,newrows);
        //解析后的数据输出
        for(int ii=0;ii<newrows.size();ii++){
            List<String> rowscol=newrows.get(ii);
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                System.out.print(str);
                // \t方便复制到xls时有格式,后期注释掉
                System.out.print("	");
            }
            //当前行结尾,后期注释掉
            System.out.println("*");
        }
        //主动清数据
        textList.clear();
        return newrows;

    }
    /** 扩展表数据获取(即添加多少列给原数据)
     * list 需要解析的数据
     * colmap 一些关联数据
     * extendTable(11,3)//表前三行是表头,共11列
     */
    public void extendTable(List<List<String>> rows,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
        //原理:当检测到单元格里内容间距大于intervalMinX就认为需要拆分单元格
        int titn=(int)colmap.get("titn"); // titn 表头所占行数,从1开始
        double intervalMinX=(double)colmap.get("intervalMinX");
        int fontindex=(int)colmap.get("fontindex");
        LinkedHashMap<String,Integer> addRe=new LinkedHashMap<String,Integer>();  //新增列记录
        //循环表头
        for(int i=0;i<titn;i++){ //表头行循环
            List<String> rowscol=rows.get(i);//此是行数据
            for(int ii=0;ii<rowscol.size();ii++){//此是行列循环
                String str=rowscol.get(ii);
                String strNext=null;
                if(ii+1<rowscol.size()) {
                    strNext=rowscol.get(ii+1);
                }
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");
                }
                String[] arrayStr=str.split(" ");
                //新增列数
                int addcol=0;
                //循环数组
                for(int array=0;array<arrayStr.length;array++){
                    //当前字的描述
                    TextElement textElementF =textList.get(fontindex);
                    String bstr=arrayStr[array];
                    int strleg=bstr.length();
                    if(array>0) {
                        TextElement upF=textList.get(fontindex-1);//上次的尾字的描述
                        double x = upF.getX();
                        double w = upF.getWidth();
                        double xw=x+w;
                        double xx = textElementF.getX();
                        double diff=xx-xw;
                        //System.out.println(i+upF.getText()+textElementF.getText()+ii+";diff:"+diff);
                        if(diff>intervalMinX) {
                            addcol++;
                        }
                    }
                    fontindex=fontindex+strleg;//下一个循环字的坐标
                }
                //此列后面要新增列
                if(addcol>0){
                    if(StringUtils.isBlank(strNext)) {
                        addcol=addcol-1;//其后有空格列则就不新增列,而是把值移过去
                    }
                    Integer integer=addRe.get(String.valueOf(ii));
                    if(integer==null) {
                        integer=0;
                    }
                    if(addcol>integer) {
                        addRe.put(String.valueOf(ii), addcol);
                    }
                }
            }
        }
        colmap.put("addRe",addRe);
        //扩展表实现
        extendTableT(rows,textList,colmap, newrows);
    }

    //重新新增列
    public void addTableC(List<List<String>> rows,Map colmap) {
        int titn=(int)colmap.get("titn");
        LinkedHashMap<String,Integer> addRe=(LinkedHashMap<String,Integer>)colmap.get("addRe");
        //表头行循环,扩展表头即可(后面需要对 rows 的表头部分.set(下标,值),主体就不需要了,是直接赋值进 newrows 里了)
        for(int i=0;i<titn;i++){
            List<String> rowscol=rows.get(i);//此是行数据
            int addA=0;//新增总数
            //给addRe新增列
            for(String key:addRe.keySet()){
                Integer integer = addRe.get(key);
                int keyn=Integer.parseInt(key)+1;
                keyn+=addA;
                for(int k=0;k<integer;k++) {
                    rowscol.add(keyn, "");
                }
                addA+=integer;
            }
        }
    }
    /**
     * 表新增列实现
     */
    public void extendTableT(List<List<String>> rows,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
        //重新新增列(此意义在于,扩展表头后,表头拆分后直接赋值)
        addTableC(rows,colmap);
        //表头重新定位,并获取每列的定位
        int titn=(int)colmap.get("titn"); // titn 表头所在行数,从1开始
        double intervalMinX=(double)colmap.get("intervalMinX");
        List<String> offsetcol=(List<String>)colmap.get("offsetcol");
        Map<String,Object> colI=( Map<String,Object>)colmap.get("colI");
        //每一列表头的具体说明,如定位等
        List<Map> colXY=new ArrayList<Map>();
        int fontindex=0;
        for(int i=0;i<titn;i++){ //表头-行的循环
            List<String> rowscol=rows.get(i);//此是行数据
            for(int ii=0;ii<rowscol.size();ii++){ //此是列循环
                String str=rowscol.get(ii);
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");
                }
                //赋初始定位
                Map colXYEle=new HashMap();
                //开始节点
                TextElement FTE= textList.get(fontindex);
                colXYEle.put("F",FTE.getX());
                colXYEle.put("FT",FTE.getText());
                //结束节点
                TextElement ETE=textList.get(fontindex+(str.replaceAll(" ","").length()-1));
                colXYEle.put("E",ETE.getX()+ETE.getWidth());
                colXYEle.put("ET",ETE.getText());
                //表头节点内容
                colXYEle.put("T",str);
                int intI=(ii+1)-colXY.size();
                //扩展colXY 用于set
                for (int k =intI; k >0; k--){
                    colXY.add(null);
                }
                String[] arrayStr=str.split(" ");
                boolean bol=false;
                String valla="";
                String vallb="";
                //循环数组
                for(int array=0;array<arrayStr.length;array++){
                    String bstr=arrayStr[array];
                    //第一个就不对比了
                    if((!bol)&&(array>0)) {
                        //当前字的描述
                        TextElement textElementF =textList.get(fontindex);
                        //不会有-1的情况(上if里的array>0就已经排除此了)
                        //上次的尾字的描述
                        TextElement upF=textList.get(fontindex-1);
                        double x = upF.getX();
                        double w = upF.getWidth();
                        double xw=x+w;
                        double xx = textElementF.getX();
                        double diff=xx-xw;
                        //System.out.println(upF.getText()+":"+(fontindex-1)+";"+textElementF.getText()+":"+(fontindex));
                        if(diff>intervalMinX) {
                            //System.out.println(i+upF.getText()+textElementF.getText()+ii+";diff:"+diff);
                            //System.out.println("valla:"+valla);
                            bol=true;
                        }
                    }
                    //不需要拆分
                    if(!bol) {//要获取后面的字符串,所以不能跳出当前循环
                        valla+=bstr+" ";
                        int strleg=Helper.nvlString(bstr).length();
                        //System.out.println("当前字符串:"+Helper.nvlString(bstr)+"下标改变:"+fontindex+"("+textList.get(fontindex).getText()+")"+"->"+(fontindex+strleg)+"("+textList.get(fontindex+strleg).getText()+")");
                        fontindex=fontindex+strleg;//下一个循环字的坐标
                    }else {
                        vallb+=bstr+" ";
                    }
                }
                //需要拆分单元格值
                Map colXYE;
                if(bol) {
                    valla=Helper.nvlString(valla);
                    rowscol.set(ii,valla);
                    int in=ii+1;
                    if(in>=rowscol.size()){//防止set超出下标报错
                        rowscol.add("");
                    }
                    rowscol.set(in,Helper.nvlString(vallb));
                    //修订定位
                    Map colXYEleT=new HashMap();
                    int endf=fontindex-1;
                    //开始节点
                    TextElement FTET=textList.get(endf-(valla.replaceAll(" ","").length()-1));
                    colXYEleT.put("F",FTET.getX());
                    colXYEleT.put("FT",FTET.getText());
                    //结束节点
                    TextElement ETET=textList.get(endf);
                    colXYEleT.put("E",ETET.getX()+ETET.getWidth());
                    colXYEleT.put("ET",ETET.getText());
                    //表头节点内容
                    colXYEleT.put("T",valla);
                    colXYE=colXYEleT;
                }else{
                    colXYE=colXYEle;
                    //int length = str.replaceAll(" ", "").length();
                    //fontindex=fontindex+length;//下一个循环字的坐标
                }
                //表头值直接赋值到某(从1开始)
                boolean toIndex = isToIndex(colI, colXY,colXYE);//此需赋到指定处返回false
                if(toIndex){ //colXYEleT已指定赋到某就不参合下了
                    //colXY原ii有值,且设置不能被后覆盖.下返回false
                    boolean overflow = isOverflow(offsetcol, colXY, ii);//注意还有一个此方法
                    if(overflow){
                        colXY.set(ii,colXYE);
                    }
                }
            }
            //tableTit.add(rowscol);
        }
        //表头的数值描述
        List<String> tableTit=new ArrayList<String>();
        //中间有空着的,后面的移过来(即[有,null,有]->[有,有]})
        Iterator it=colXY.iterator();
        while(it.hasNext()){//返回true或false
            Map map=(Map)it.next();
            if(map==null){
                it.remove();
            }else{
                String t=(String)map.get("T");
                tableTit.add(t);
            }
        }
        //每一列表头的具体说明,如定位等
        colmap.put("colXY", colXY);
        //表头的数值描述
        colmap.put("tableNum", fontindex);
        //TextElement tt=textList.get(fontindex);
        //System.out.println(tt.getText());
        colmap.put("tableTit", tableTit);
        //表实体数据纠正
        extendTableB(rows,textList,colmap,newrows);
    }
    public boolean isOverflow(List<String> offsetcol,List<Map> colXY,int ii){
        boolean boloffset=true;
        if(offsetcol.size()>0){
            //上次值获取
            Map map= colXY.get(ii);
            //左侧未定义colXY值
            if(map!=null&&map.size()>0){
                String t=(String)map.get("T");
                if(offsetcol.contains(t)){
                    boloffset=false;
                }
            }
        }
        return boloffset;
    }
    public boolean isToIndex(Map<String,Object> colI,List<Map> colXY,Map colXYEle){
        boolean toIndex=true;
        String str=((String)colXYEle.get("T")).replaceAll("\\s+","");
        //测试
        /*if(str.equals("(FLTCYCLES)")){
            System.out.println(str);
        }*/
        if(colI.size()>0&&colI.containsKey(str)){
            Object o = colI.get(str);
            Integer intt=0;
            if(o instanceof List){
                List<Integer> list=(List)o;
                if(list.size()>0){
                    intt=list.get(0);
                    list.remove(0);//每次只取一次,取后就失效
                }
            }else if(o instanceof Integer){
                intt=(Integer) o;
            }else {
                return toIndex;
            }
            if(intt==0){
                return toIndex;
            }
            int intI=intt-colXY.size();
            //扩展colXY 用于set
            for (int k =intI; k >0; k--){
                colXY.add(null);
            }
            colXY.set((intt-1),colXYEle);
            toIndex=false;
        }
        return toIndex;
    }


    //表主体数据纠正
    public void extendTableB(List<List<String>> rows,List<TextElement> textList,Map colmap,List<List<String>> newrows) {
        //主体纠正原理:此内容的开始坐标在上个列的结尾后面,在下个列的开头前面,则此内容在这列
        int titn=(int)colmap.get("titn"); // titn 表头所在行数,从1开始
        List<String> tabcols=( List<String>)colmap.get("tabcols");//数据库就需要存的列数(从1开始)
        String type=(String)colmap.get("type");//此为表的类型
        //备份的colXY
        List<Map> colXYSlist = colXYS.get(type);
        //每一列表头的具体说明,如定位等
        List<Map> colXY=(List<Map> )colmap.get("colXY");
        //表头列的结束位置修正(即加长了此列的位置)
        Map<Integer,Object> endcols=(Map<Integer,Object>)colmap.get("endcols");
        if(endcols==null){
            endcols=new HashMap<Integer,Object>();
        }
        boolean bol=false;
        if((colXY.size()<tabcols.size())&&colXYSlist!=null){
            //从备份提取colXY,并使用,解决如:此页表头有缺失
            colXY=colXYSlist;
            bol=true;
        }else{
            if(colXYSlist==null){
                colXYS.put(type,colXY);
            }
        }
        //第一行表头的展示(同列以后为准)
        List<String> tableTit=(List<String>)colmap.get("tableTit");
        int tableNum=(int)colmap.get("tableNum");//表头字符串长度
        //赋表头数据(此仅作展示用,实际表头处理时依据上colXY)
        newrows.add(tableTit);
        //System.out.println(Helper.listToStringJSON(newrows));
        //遍历输出
        for(int ii=titn;ii<rows.size();ii++){
            List<String> rowscol=rows.get(ii);
            //是否是最后一行
            boolean isEndrow=false;
            if("crjs".equals(type)){
                isEndrow=isEndrowCrjS(rowscol);
            }else if(type.indexOf("CRJ_ST")!=-1){
                isEndrow=isEndrowCrjST(rowscol);
            }else {
                isEndrow=isEndrow(rowscol);
            }
            if(isEndrow) {
                break;
            }
            List<String> newrow=new ArrayList();
            //赋行初始数据
            for(int n=0;n<colXY.size();n++){
                newrow.add("");
            }
            for(int iii=0;iii<rowscol.size();iii++){
                String str=rowscol.get(iii);
                if(StringUtils.isBlank(str)) {
                    str="";
                    continue;
                }else {
                    str=str.replaceAll("\\s+"," ");//任何空白字符(包括空格、制表符、换页符等等)替换为空格
                }
                String[] b=str.split(" ");
                //单元格里内容根据空格分隔
                for(int array=0;array<b.length;array++){
                    String s=b[array];
                    int strleg=s.length();
                    //当前内容第一个一个字位置下标
                    TextElement f=textList.get(tableNum);
                    //当前内容最后一个字位置下标
                    TextElement ef=textList.get(tableNum+(strleg-1));
                    double fx=f.getX();
                    double ex=ef.getX();
                    //double fxw=f.getX()+f.getWidth();

                    //方便测试断点进入
                    /*if("T:".equals(s)){
                        TextElement testT=textList.get(tableNum+strleg+1);
                        System.out.println("进入断点");
                    }*/

                    //根据x轴坐标获取放在哪列(从0开始)
                    int index= getIndex(colXY,ex,fx,endcols);
                    String val=newrow.get(index);
                    if(StringUtils.isNotBlank(val)){
                        val+=" ";
                    }
                    newrow.set(index,val+s);
                    //下一个循环字的坐标
                    tableNum+=strleg;
                }
            }
            newrows.add(newrow);
        }
    }
    //根据x轴坐标获取放在哪列(从0开始),参数x是内容的最后一个字的x(不是xw)
    public int getIndex(List<Map> colXY,double ex,double fx,Map<Integer,Object> endcols) {
        //此内容最后一个字位置下标在上个列的结尾后面,在下个列的开头前面,则此内容在这列
        Map endmap=null;
        int size = colXY.size();
        if(size==1){
            return 0;
        }
        int res=0;
        //获取倒数第二列下标
        if(size>2){
            res=size-2;
        }
        //倒数第二列的信息
        endmap=colXY.get(res);
        double ee=(double)endmap.get("E");

        //修正结束位置
        Object doue=endcols.get(res);
        if(doue!=null&&(doue instanceof Double)){
            Double douee=(Double)doue;
            ee+=douee;
        }
        //当前字的 ex直接大于倒数第二列的结束位置,直接放最后一位
        if(fx>ee){
            if(doue instanceof String){
                //继续往后走,让程序判断是赋给最后一列还是倒数第二列
            }else{
                //ex直接大于倒数第二列的结束位置,直接放最后一位
                return size-1;
            }
        }
        for(int i=0;i<colXY.size();i++){
            //上个节点的结束位置
            double beforeE=0;
            //下个节点的开始位置
            double afterF=0;
            if(i>0){
                Map map=colXY.get(i-1);
                beforeE=(double)map.get("E");
                //修正结束位置
                Object doub=endcols.get(i-1);
                if(doub!=null&&(doub instanceof Double)){
                    Double doubb=(Double) doub;
                    beforeE+=doubb;
                }
            }
            //已经是最后一个了,直接赋为最后一个
            if ((i+1)>=size){
                return size-1;
            }
            Map mape=colXY.get(i+1);
            afterF=(double)mape.get("F");
            //此内容最后一个字位置下标在上个列的结尾后面,在下个列的开头前面,则此内容在这列
            if(ex>beforeE&&ex<afterF){
                //获取当前列数据
                Map map=colXY.get(i);
                double indexE=(double)map.get("E");
                //修正结束位置
                Object doui=endcols.get(i);
                if(doui!=null&&(doui instanceof Double)){
                    Double douii=(Double) doui;
                    indexE+=douii;
                }
                //满足上的同时,当前字首位置大于当前列尾,则默认认为应放在下一列
                if(fx>indexE){
                    String doustr="right";//(默认)
                    if(doui!=null&&(doui instanceof String)){
                        doustr=(String) doui;
                    }
                    if("left".equals(doustr)){
                        return i;//分给左侧
                    }else{
                        return i+1;//分给右侧(默认)
                    }
                }
                return i;
            }
        }
        return size-1;
    }
    //是否是最后一行
    public boolean isEndrow(List<String> list) {
        boolean bol=false;
        String s=list.get(0);//匹配值例 "Feb 15/2020"
        String reg = "^[A-Za-z]{3} [0-9]{1,2}\\/2[0-9]{3}$";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean rs =s.matches(reg);
        if(rs){
            bol=true;
        }
        return bol;
    }
    //是否是最后一行
    public boolean isEndrowCrjS(List<String> list) {
        boolean bol=false;
        String s=list.get(0);//匹配值例 "CSP B−136 −  Maintenance Planning Document"
        String reg = "^CSP .+ Maintenance Planning Document$";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean rs =s.matches(reg);
        if(rs){
            bol=true;
        }
        return bol;
    }
    //是否是最后一行
    public boolean isEndrowCrjST(List<String> list) {
        boolean bol=false;
        String s=list.get(0);//匹配值例 "Maintenance Planning Manual"
        String reg = "^Maintenance Planning Manual$|^Mainitenance Planning Manual$";  //带不带^ $一样?都是严格按照正则,不能为例"/^[0-9]$/"
        boolean rs =s.matches(reg);
        if(rs){
            bol=true;
        }
        return bol;
    }
    /**
     * 关
     * @author 子火
     * @Date 2020-12-23
     */
    public void closed(ObjectExtractor oe,PDDocument document,InputStream input)throws Exception{
        //根据页面page类获取
        oe.close();
        document.close();
        input.close();
    }




}
