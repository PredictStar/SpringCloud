package cn.nzxxx.predict.codegeneration;



import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.Helper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * Controller 类
 * @author 子火
 * @Date 2019年4月23日
 */
@Controller
@RequestMapping("/codeGenerationController")
public class CodeGenerationController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	private CodeGenerationServiceImpl bbkzVersionServiceI;


	/**
	 * xml代码生成
	 * @author 子火
	 * @Date 2020年4月20日
	 * @param schema 数据库名
	 * @param tableName 表名 (即Navicat下点开表所示名称,区分大小写(一般大写))
     * @param modeNum 模板样式
     * @param idcol 主键列
     * @param pkcol 若是从表,其对于主表的列
	 * @return ReturnClass
	 * localhost:8080/misbj/codeGenerationController/autoCode?schema=asr&tableName=表名&modeNum=1&idcol=主键
	 * 生成从表xml  localhost:8080/misbj/codeGenerationController/autoCode?schema=asr&tableName=从表名&modeNum=3&idcol=从表主键&pkcol=从表上的从键
	 */
	@RequestMapping(value="/autoCode")
	@ResponseBody
	public ReturnClass autoCode(String schema, String tableName, Integer modeNum, String idcol, String pkcol, HttpServletRequest request) {
		ReturnClass res=null;
		try {
			tableName=Helper.nvlString(tableName);
			idcol=Helper.nvlString(idcol).toUpperCase();
			if(modeNum==null) {
				modeNum=1;
			}
			if (StringUtils.isBlank(pkcol)){
				pkcol="";
            }
            if (StringUtils.isBlank(tableName)){
            	res=Help.returnClassT(300,"表名参数为空","表名参数为空");
                return res;
            }
			res=bbkzVersionServiceI.autoCode(schema,tableName,modeNum,idcol,pkcol,request);
		}catch (Exception e){
			log.error(Helper.exceptionToString(e));           
            String strE=Helper.exceptionToString(e);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo); 
			res=Help.returnClassT(500,"autoCode方法异常",strEInfo);
		}
		return res;
	}
	
	/**
	 * 流程SQL生成
	 * @author 子火
	 * @Date 2020年4月22日
	 * @return ReturnClass
	 * localhost:8080/misbj/codeGenerationController/flowSql?idcol=唯一列&tableName=表名&wfname=流程名(例 TOOLSTOCKIN)&definitionid=(例 1001)
	 */
	@RequestMapping(value="/flowSql")
	@ResponseBody
	public ReturnClass flowSql(String tableName,String wfname,String idcol,Integer definitionid,HttpServletRequest request) {
		ReturnClass res=null;
		try {
			tableName=Helper.nvlString(tableName);
			idcol=Helper.nvlString(idcol).toUpperCase();
			if (definitionid==null){
            	res=Help.returnClassT(300,"definitionid参数为空","");
                return res;
            }
            if (StringUtils.isBlank(tableName)){
            	res=Help.returnClassT(300,"表名参数为空","");
                return res;
            }
            if (StringUtils.isBlank(wfname)){
            	res=Help.returnClassT(300,"wfname参数为空","");
                return res;
            }
            if (StringUtils.isBlank(idcol)){
            	res=Help.returnClassT(300,"idcol参数为空","");
                return res;
            }
			res=bbkzVersionServiceI.autoFlowsql(tableName,wfname,idcol,definitionid,request);
		}catch (Exception e){
			log.error(Helper.exceptionToString(e));           
            String strE=Helper.exceptionToString(e);
            String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
            System.out.println(strEInfo); 
			res=Help.returnClassT(500,"autoCode方法异常",strEInfo);
		}
		return res;
	}

}
