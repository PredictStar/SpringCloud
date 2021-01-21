package cn.nzxxx.predict.toolitem.entity;


import javax.servlet.http.HttpServletRequest;

import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import cn.nzxxx.predict.toolitem.tool.CookieUtils;
import cn.nzxxx.predict.toolitem.tool.Helper;

public class Help {
	/**
	 * 返回状态码类
	 * Integer intt--serverImpl返回数字,最好别返回null,此方法里已规定intt为null返回510
	 * 状态描述不为null值描述才会赋,需要赋值查询结果给valueDescribe传List,方法返回的字符串会为[]而不是"[]"
	 * @return  ReturnClass类json格式
	 * 状态码
	 * 200-正常;300-正常错误返回;400-跳转至首页;500-异常错误;510-不可能异常错误
	 * 210-冲突
	 * 0  -受影响数据为0行
	 */
	public static String returnClass(Integer intt,String stateDescribe,Object valueDescribe) {
		String valueOf;
		if(intt==null){
			valueOf="510";
			stateDescribe="不可能异常错误,returnClass方法的参1为null";
		}else{
			valueOf= String.valueOf(intt);
		}
		ReturnClass returnClass=new ReturnClass();
		returnClass.setStatusCode(valueOf);
		if(stateDescribe!=null||valueDescribe!=null){
			returnClass.setStateDescribe(stateDescribe);
			returnClass.setValueDescribe(valueDescribe);
		}

		return Helper.pojoToStringJSON(returnClass);

	}

	/**
	 * 返回状态码类
	 * @param intt
	 * @param stateDescribe
	 * @param valueDescribe
	 * @return ReturnClass类
	 */
	public static ReturnClass returnClassT(Integer intt,String stateDescribe,Object valueDescribe) {
		String valueOf;
		if(intt==null){
			valueOf="510";
			stateDescribe="不可能异常错误,returnClass方法的参1为null";
		}else{
			valueOf= String.valueOf(intt);
		}
		ReturnClass returnClass=new ReturnClass();
		returnClass.setStatusCode(valueOf);
		returnClass.setStateDescribe(stateDescribe);
		returnClass.setValueDescribe(valueDescribe);

		return returnClass;

	}

	/**
	 * 根据参返回状态码类描述
	 * 参1,参2为NULL或'',返回已经定义好的状态码描述
	 * @return 返回null表正常 ;否则返回,Help.returnClass(500, "异常,传递的参为NULL或''!");//理论上前台应该已经拦截验证.
	 */
	public static String return5002Describe(String str1,String str2) {
		str1=Helper.eToNULL(str1);
		str2=Helper.eToNULL(str2);
		if(str1==null||str2==null){
			return Help.returnClass(500, "异常,传递的参为NULL或''!","参1为"+str1+"参2为"+str2+".");
		}else{
			return null;
		}

	}
	/**
	 * 根据参返回状态码类描述
	 * 参1,参2,参3为NULL或'',返回已经定义好的状态码描述
	 * @return 返回null表正常 ;否则返回,Help.returnClass(500, "异常,传递的参为NULL或''!");//理论上前台应该已经拦截验证.
	 */
	public static String return5003Describe(String str1,String str2,String str3) {
		str1=Helper.eToNULL(str1);
		str2=Helper.eToNULL(str2);
		str3=Helper.eToNULL(str3);
		if(str1==null||str2==null|str3==null){
			return Help.returnClass(500, "异常,传递的参为NULL或''!","参1为"+str1+"参2为"+str2+".");
		}else{
			return null;
		}

	}
	/**
	 * 根据参返回状态码类描述
	 * 参1,参2,参3为NULL或'',返回已经定义好的状态码描述
	 * @return 返回null表正常 ;否则返回,Help.returnClassT(500, "异常,传递的参为NULL或''!");//理论上前台应该已经拦截验证.
	 */
	public static ReturnClass return5003DescribeT(String str1,String str2,String str3) {
		str1=Helper.eToNULL(str1);
		str2=Helper.eToNULL(str2);
		str3=Helper.eToNULL(str3);
		if(str1==null||str2==null|str3==null){
			return Help.returnClassT(500, "异常,传递的参为NULL或''!","参1为"+str1+"参2为"+str2+".");
		}else{
			return null;
		}

	}
	/**
	 * 根据参返回状态码类描述
	 * @param str 参为NULL或'',返回已经定义好的状态码描述
	 * @return 返回null表正常 ;否则返回,Help.returnClass(500, "异常,传递的参为NULL或''!");//理论上前台应该已经拦截验证.
	 */
	public static String return5001Describe(String str) {
		str=Helper.eToNULL(str);
		if(str==null){
			return Help.returnClass(500, "异常,传递的参为NULL或''!",null);//理论上前台应该已经拦截验证.
		}else{
			return null;
		}

	}
	/**
	 * 根据参返回状态码类描述-前台值为空Internet默认为null
	 * @param  str 参1(Integer)为NULL
	 * @param bol 参2为true表参1为0也返回已经定义好的状态码描述
	 * @return 返回null表正常 ;否则返回,Help.returnClass(500, "异常,传递的数字参有误!");
	 */
	public static String return5001IDescribe(Integer str,boolean bol) {

		boolean b=str!=null?bol&&str==0?true:false:true;
		if(b){
			return Help.returnClass(500, "异常,传递的数字参有误!","数字参为"+str+".");
		}else{
			return null;
		}

	}
	/**
	 * 传进来的加密好的token值与Cookie的token做对比
	 * cookie不存在返回值为null
	 */
	public static String tokenMD5Verify(HttpServletRequest request,String tokenn) {
		String token = CookieUtils.getCookieValue(request, "TT_TOKEN",true);
		//System.out.println(token);
		//System.out.println(Helper.stringMD5(token));
		if(tokenn==null||!tokenn.equals(Helper.stringMD5(token))){
			return Help.returnClass(510, "异常,传递的tokenn参有误!","请重新登录!");
		};
		return null;
	}


}
