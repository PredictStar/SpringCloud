//package com.tiandy.easy7.core.vo;;//实际地址
${(suggestion?html?replace('\r\n','<w:br/>') )!}
import javax.persistence.*;

/**
 * Entity 类
 * @author -自动生成
 * @Date ${cur_time?datetime}
 */
/**${table_comments!}*/
public class ${entity_name}{
<#list col_list as colmap>
	<#if (colmap.DATA_TYPE=="int")>
		<#assign dataType="Integer"/>
	<#elseif (colmap.DATA_TYPE=="double")>
		<#assign dataType="Double"/>
	<#elseif (colmap.DATA_TYPE=="varchar")>
		<#assign dataType="String"/>
	<#elseif (colmap.DATA_TYPE=="char")>
		<#assign dataType="String"/>
	<#elseif (colmap.DATA_TYPE=="datetime")>
		<#assign dataType="Date"/>
	<#elseif (colmap.DATA_TYPE=="timestamp")>
		<#assign dataType="Date"/>
	</#if>
	<#if (colmap.NULLABLE=="NO")>
		<#assign nullAble="false"/>
	<#else>
		<#assign nullAble="true"/>
	</#if>
	/**${colmap.COMMENTS!}*/
	private ${dataType} ${colmap.UNCAPITALIZE};

</#list>
<#list col_list as colmap>
	<#if (colmap.DATA_TYPE=="int")>
		<#assign dataType="Integer"/>
	<#elseif (colmap.DATA_TYPE=="double")>
		<#assign dataType="Double"/>
	<#elseif (colmap.DATA_TYPE=="varchar")>
		<#assign dataType="String"/>
	<#elseif (colmap.DATA_TYPE=="char")>
		<#assign dataType="String"/>
	<#elseif (colmap.DATA_TYPE=="datetime")>
		<#assign dataType="Date"/>
	<#elseif (colmap.DATA_TYPE=="timestamp")>
		<#assign dataType="Date"/>
	</#if>
	<#if (colmap.NULLABLE=="NO")>
		<#assign nullAble="false"/>
	<#else>
		<#assign nullAble="true"/>
	</#if>
	public ${dataType!模板未定义} get${colmap.INITCAP}(){
		return this.${colmap.UNCAPITALIZE};
	}
	public void set${colmap.INITCAP}(${dataType} ${colmap.UNCAPITALIZE}){
		this.${colmap.UNCAPITALIZE} = ${colmap.UNCAPITALIZE};
	}
</#list>

}