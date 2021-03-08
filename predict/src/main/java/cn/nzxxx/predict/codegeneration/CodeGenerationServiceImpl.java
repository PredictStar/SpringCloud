package cn.nzxxx.predict.codegeneration;



import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.sun.deploy.config.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

/**
 * @author 子火
 * @Date 2020年4月20日
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class CodeGenerationServiceImpl{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	/**
	 * 代码生成的默认地址
	 */
	private String codePath="";
	/**
	 * 模板路径
	 */
	private String ftlUrl="";
	//过滤查询列
	public String addFilter(String str) {
		List<String> filtercol=new ArrayList<String>();
		filtercol.add("WRITTEN");
		filtercol.add("WRITTENDATE");
		filtercol.add("APPROVED");
		filtercol.add("APPROVEDATE");
		filtercol.add("REVIEWED");
		filtercol.add("REVIEWEDATE");
		filtercol.add("CREATEDBY");
		filtercol.add("CREATEDWHEN");
		filtercol.add("UPDATEDBY");
		filtercol.add("UPDATEDWHEN");
		filtercol.add("DELETED");
		filtercol.add("VERSIONNO");
		filtercol.add("SORTBY");
		
		filtercol.add("USERNAME");
		filtercol.add("CREATEWHEN");
		filtercol.add("UPDATEWHEN");
		filtercol.add("UPDATEBY");
		String strin="''";
		for(int i=0;i<filtercol.size();i++){
			if(i==0) {
				strin="'"+filtercol.get(0)+"'";
			}else {
				strin=strin+",'"+filtercol.get(i)+"'";
			}
		}
		if(StringUtils.isNotBlank(str)) {
			strin+=str;			
		}
		return strin;
	}
	String packageN;//骆驼命名法
    String packAgeN;//首大写的骆驼命名
    String packagen;//全小写
    String packAGENL;//全大写(会包含下划线)
	public ReturnClass autoCode(String schema, String tableName, int modeNum, String idcol, String pkcol, HttpServletRequest request)throws Exception{
		String notin=",'"+idcol+"'";
		if(StringUtils.isNotBlank(pkcol)) {
			notin+=",'"+pkcol+"'";
		}
		packageN=Helper.transformPrelude(tableName);//骆驼命名法
        packAgeN=StringUtils.capitalize(packageN);//首大写的骆驼命名
        packagen= packageN.toLowerCase();//全小写
        packAGENL=tableName.toUpperCase();//全大写(会包含下划线)
		String sql="select  col.TABLE_NAME,col.COLUMN_NAME, LCASE(col.column_name) as LCASE,col.column_comment as COMMENTS,tab.TABLE_COMMENT AS TABLE_COMMENTS, " + 
				"	col.DATA_TYPE,col.CHARACTER_MAXIMUM_LENGTH AS CHAR_LENGTH,col.IS_NULLABLE AS NULLABLE " + 
				"	from information_schema.columns as col " + 
				"	INNER JOIN information_schema.tables as tab ON tab.table_name=col.table_name " + 
				"	where col.table_schema ='"+schema+"'  " + 
				"	and col.table_name = '"+tableName+"' 	 " +
				"   and col.COLUMN_NAME not in("+addFilter(notin)+") ";
		//较oracle少了		DATA_PRECISION DATA_SCALE
		List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
		for(Map<String, Object> map: list){
			String uncapitalize=Helper.transformPrelude((String) map.get("LCASE"));
			map.put("INITCAP",StringUtils.capitalize(uncapitalize));//首字母大写的骆驼命名法
			map.put("UNCAPITALIZE",uncapitalize);//首字母小写的骆驼命名法
		}
		if(list.size()>0){
			//根据实际情况看下两个用哪个
			//ftlUrl=request.getSession().getServletContext().getRealPath("/")+"WEB-INF/classes/com/me/rj/codegeneration/ftl";
			//ftlUrl=System.getProperty("user.dir")+"/ftl";
			ftlUrl = ResourceUtils.getURL("classpath:").getPath();//D:/SpringCloud/predict/target/classes/
			ftlUrl=ftlUrl+"META-INF/resources/ftl/";
			//System.out.println(ftlUrl);
			codePath="D:/codeGeneration/"+packagen+"/";
			boolean bolent=createXmlRJ(list, modeNum,idcol,pkcol);
			if(!bolent){
				return Help.returnClassT(300,"生成xml失败","");
			}
			boolean boljs=createJsRJ(modeNum);
			if(!boljs){
				return Help.returnClassT(300,"生成js失败,xml成功","");				
			}			
			boolean bolController=createControllerRJ(modeNum);
			if(!bolController){
				return Help.returnClassT(300,"生成Controller失败,js成功,xml成功","");				
			}
			boolean bolServiceimpl=createServiceimplRJ(modeNum);
			if(!bolServiceimpl){
				return Help.returnClassT(300,"生成Serviceimpl失败,Controller成功,js成功,xml成功","");				
			}
			return Help.returnClassT(200,"生成"+packAGENL+"全部成功","");
		}else{
			return Help.returnClassT(300,"无查询结果,注意表名大小写","无查询结果");
		}

	}
	/**
     * 生成 RJ xml页面
     * @author 子火
     * @Date 2020年4月20日
     * @param tablelist 表的说明
     * @return false 说明异常
     */
	public boolean createXmlRJ(List<Map<String, Object>> tablelist,int modeNum,String idcol,String pkcol){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("table_key",idcol);
        map.put("table_pkcol",pkcol);
        map.put("form_br",5); //form表单一行几个
        map.put("cur_time",new Date());
        map.put("xml_pageid",packagen);
        map.put("table_name",packAGENL);
        map.put("table_comments",tablelist.get(0).get("TABLE_COMMENTS"));
        map.put("col_list",tablelist);
        List<Integer> inxmlftl=new ArrayList<Integer>();
        inxmlftl.add(1);//editablelist2 
        inxmlftl.add(2);//form 
        inxmlftl.add(3);//editablelist2  子表
        if(!inxmlftl.contains(modeNum)) {
        	modeNum=1;
        }        
        String tempName="rjxml"+modeNum+".ftl";//模板名称
        String fileName=packAGENL.toLowerCase()+".xml";
        String pageName="xml";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }

	/**
     * 生成 RJ js页面
     * @author 子火
     * @Date 2020年4月21日
     * @return false 说明异常
     */
	public boolean createJsRJ(int modeNum){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("cur_time",new Date());
        map.put("class_jaVa",packageN);
        List<Integer> inxmlftl=new ArrayList<Integer>();
        inxmlftl.add(1);
        if(!inxmlftl.contains(modeNum)) {
        	modeNum=1;
        }        
        String tempName="rjjs"+modeNum+".ftl";//模板名称
        String fileName=packagen+".js";
        String pageName="js";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }
	/**
     * 生成 RJ controller 页面
     * @author 子火
     * @Date 2020年4月21日
     * @return false 说明异常
     */
	public boolean createControllerRJ(int modeNum){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("cur_time",new Date());
        map.put("class_JaVe",packAgeN);//首大写的骆驼命名
        map.put("class_java",packagen);
        map.put("class_jaVa",packageN);
        List<Integer> inxmlftl=new ArrayList<Integer>();
        inxmlftl.add(1);
        if(!inxmlftl.contains(modeNum)) {
        	modeNum=1;
        }        
        String tempName="controller"+modeNum+".ftl";//模板名称
        String fileName=packAgeN+"Controller.java";
        String pageName="java";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }
	/**
     * 生成 RJ serviceimpl 页面
     * @author 子火
     * @Date 2020年4月21日
     * @return false 说明异常
     */
	public boolean createServiceimplRJ(int modeNum){
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("cur_time",new Date());
        map.put("class_JaVe",packAgeN);//首大写的骆驼命名
        map.put("class_java",packagen);
        map.put("class_jaVa",packageN);
        List<Integer> inxmlftl=new ArrayList<Integer>();
        inxmlftl.add(1);
        if(!inxmlftl.contains(modeNum)) {
        	modeNum=1;
        }        
        String tempName="serviceimpl"+modeNum+".ftl";//模板名称
        String fileName=packAgeN+"Serviceimpl.java";
        String pageName="java";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }
	/**
     * 流程SQL生成
     * @author 子火
     * @Date 2020年4月20日
     * @return false 说明异常
     */
	public ReturnClass autoFlowsql(String tableName,String wfname,String idcol,Integer definitionid,HttpServletRequest request){
		packageN=Helper.transformPrelude(tableName);//骆驼命名法
        packAgeN=StringUtils.capitalize(packageN);//首大写的骆驼命名
        packagen= packageN.toLowerCase();//全小写
        packAGENL=tableName.toUpperCase();//全大写(会包含下划线)
		ftlUrl=request.getSession().getServletContext().getRealPath("/")+"WEB-INF/classes/com/me/rj/codegeneration/ftl";
		codePath="D:/codeGeneration/"+packagen+"/";
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("xml_pageid",packagen);
        map.put("idcol",idcol);
        map.put("wfname",wfname);
        map.put("tableName",tableName);
        map.put("definitionid",definitionid);
        map.put("xmlname",packAGENL.toLowerCase());
        map.put("PROAGENL",packageN.toUpperCase());
             
        String tempName="flowsql.ftl";//模板名称
        String fileName=packAGENL.toLowerCase()+".sql";
        String pageName="flowsql";
        boolean reabol=template(map, tempName,fileName,pageName);
        if(!reabol){
			return Help.returnClassT(300,"流程SQL生成失败","");				
		}
        return Help.returnClassT(200,"流程SQL生成成功","");
    }
	/**
     * 生成实体类页面
     * @author 子火
     * @Date 2019年5月9日14:45:34
     * @param tablelist 表的说明
     * @return false 说明异常
     *//*
    public boolean createEntity(List<Map<String, Object>> tablelist,String tabN,int modeNum,String packAgeN){
        Map<String,Object> map=new HashMap();
        map.put("cur_time",new Date());
        map.put("table_name",tabN);
        map.put("entity_name",packAgeN+"Entity");
        map.put("table_comments",tablelist.get(0).get("TABLE_COMMENTS"));
        map.put("col_list",tablelist);
        String tempName="entity1.ftl";//模板名称
        if(modeNum==2){
            tempName="entity2.ftl";
        }
        String fileName=packAgeN+"Entity.java";
        String pageName="entity";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }
    *//**
     * 生成实体类对应的.hbm.xml
     * @author 子火
     * @Date 2019年11月01日
     * @param tablelist 表的说明
     * @return false 说明异常
     *//*
    public boolean createHbmXml(List<Map<String, Object>> tablelist,String tabN,int modeNum,String packAgeN){
        Map<String,Object> map=new HashMap();
        map.put("cur_time",new Date());
        map.put("table_name",tabN);
        map.put("entity_name",packAgeN+"Entity");
        map.put("table_comments",tablelist.get(0).get("TABLE_COMMENTS"));
        map.put("col_list",tablelist);
        String tempName="hbmXml1.ftl";//模板名称
        String fileName=packAgeN+"hbm.xml";
        String pageName="po";
        boolean reabol=template(map, tempName,fileName,pageName);
        return reabol;
    }*/

	/**
	 * 模板生成通用方法
	 * @author 子火
	 * @Date 2020年4月20日
	 * @param map 动态参
	 * @param tempName 模板名称
     * @param fileName 生成文件名称
	 * @param pageName 文件所在文件夹名称
	 * @return false 说明异常
	 */
	public boolean template(Map map,String tempName,String fileName,String pageName){
		boolean reabol=true;
		try{
			Configuration cfg = new Configuration();
			cfg.setDirectoryForTemplateLoading(new File(ftlUrl));  //在这下找模版
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			Template temp = cfg.getTemplate(tempName);  // 模版名称
			File dir = new File(codePath+pageName);//保存路径
			if(!dir.exists()){
				dir.mkdirs();
			}
			OutputStream fos = new FileOutputStream( new File(dir, fileName)); //文件的生成名称
			Writer out = new OutputStreamWriter(fos, "UTF-8");	//无此生成中文乱码		
			temp.process(map, out);
			fos.flush();
			fos.close();

		}catch(Exception e){
			String strE=Helper.exceptionToString(e);
	        String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
			System.out.println(strEInfo);
			reabol=false;
			return reabol;
		}
		return reabol;
	}
	
}