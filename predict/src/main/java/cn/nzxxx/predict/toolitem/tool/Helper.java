package cn.nzxxx.predict.toolitem.tool;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.nzxxx.predict.toolitem.entity.ReturnClass;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.net.URI;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
/*import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;*/

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.alibaba.fastjson.JSONObject;
//jackson包
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Helper {

	/**
	 * @param 定义jackson对象
	 * @return
	 */
	private static final ObjectMapper MAPPER=new ObjectMapper();


	/**
	 * 简单的测试可写这,否则报类未定义(当未启动项目时)
	 * 否则还是用 @Test 最佳
	 */
	public static void main(String[] args){


	}




//-----------------------------------协助-----------------------------------------------------------------------------------

	/**
	 * 反射类-判断是否是此类的属性
	 * 返回 true -是此类的属性;否则false
	 */
	public static boolean fsIsProperty(Object cla,String str){
		Class class1=cla.getClass();
		boolean bol=false;
		try {
			Field[] fields=class1.getDeclaredFields();
			for(Field field:fields){
				String paraName=field.getName();
				if(paraName.equals(str)){
					bol=true;
					return  bol;
				}
			}
		}catch (Exception e){
			String exceptionToString = Helper.exceptionToString(e);
			System.out.println("Helper页面fsIsProperty方法报错:"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
			return  bol;
		}
		return  bol;
	}
	/**
	 * 反射类-传方法所在对象,方法名,方法参值集合(最多支持3个),方法参类型的Class,
	 * 运行方法
	 */
	public static Object fsOperationMethod(Object cla,String str,Object[] objs,Class... parameterClass) {
		if(cla==null||objs==null){
			System.out.println("Helper页面fsOperationMethod方法参为null");
			return null;
		}
		Class class1=cla.getClass();
		Object obj=null;
		try {
			Method method = class1.getMethod(str, parameterClass);
			if(objs.length==0){
				obj=method.invoke(cla);
			}else if(objs.length==1){
				//System.out.println("执行方法名"+str);
				//System.out.println("测试:所传实体类"+objs[0].getClass().getName());
				obj=method.invoke(cla,objs[0]);
			}else if(objs.length==2){
				obj=method.invoke(cla,objs[0],objs[1]);
			}else if(objs.length==3){
				obj=method.invoke(cla,objs[0],objs[1],objs[2]);
			}

		} catch (Exception e) {
			String exceptionToString = Helper.exceptionToString(e);
			System.out.println("Helper页面fsOperationMethod方法报错-会继续往后执行"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
		}
		return obj;
	}
	/**
	 * 反射类-传对象,及属性名(注意大小写)
	 * 返回属性值
	 */
	public static Object fsGetProperty(Object cla,String str) {
		Class class1=cla.getClass();
		Object obj=null;
		try {
			Field field = class1.getDeclaredField(str);
			field.setAccessible(true);
			obj=field.get(cla);
		} catch (Exception e) {
			String exceptionToString = Helper.exceptionToString(e);
			System.out.println("Help页面fsGetProperty方法报错"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
		}
		return obj;
	}
	/**
	 * 反射类--重新给类属性值
	 * @param obj Object  类对象
	 * @param map{"类的属性-String":值-Object}
	 * @return  obj对象
	 */
	public static Object fsClass(Object obj,Map<String,Object> map) {
		Class class1=obj.getClass();
		for(String str:map.keySet()){
			//System.out.println(str+"||"+map.get(str)+"||");
			try {
				Field field = class1.getDeclaredField(str);
				field.setAccessible(true);
				try {
					field.set(obj, map.get(str));
				} catch (Exception e) {
					String exceptionToString = Helper.exceptionToString(e);
					System.out.println("Helper页面fsClass方法报错2"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
				}
			} catch (Exception e) {
				String exceptionToString = Helper.exceptionToString(e);
				System.out.println("Helper页面fsClass方法报错1"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
			}

		}

		return obj;

	}


	/**
	 * 获取数字集合的均值
	 * valueList集合无数据返回null
	 */
	public static Double numListMeanValue(List<Double> valueList){
		Double douu=null;
		if(valueList.size()!=0){//valueList集合无数据下会报错
			double total = 0;
			for(int i=0;i<valueList.size();i++) {
				total += valueList.get(i);
			}
			douu= total/valueList.size();//分母不能为0?
		}

		return douu;
	}

	/**
	 * 获取数字集合的最大值
	 * valueList集合无数据返回null
	 */
	public static Double numListMaxValue(List<Double> valueList){
		Double douu=null;
		if(valueList.size()!=0){//valueList集合无数据下会报错
			douu=Collections.max(valueList);//最大值
		}

		return douu;
	}
	/**
	 * 获取数字集合的最小值
	 * valueList集合无数据返回null
	 */
	public static Double numListMinValue(List<Double> valueList){
		Double douu=null;
		if(valueList.size()!=0){//valueList集合无数据下会报错
			douu=Collections.min(valueList);//最小值
		}

		return douu;
	}
	/**
	 * 获取数字集合的中值
	 * valueList集合无数据返回null
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Double numListMidValue(List<Double> valueList){
		Double douu=null;
		if(valueList.size()!=0){//valueList集合无数据下会报错

			Collections.sort(valueList, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return new Double(o1.toString()).compareTo(new Double(o2.toString()));
				}
			});
			if(valueList.size()%2==0) {
				douu = (valueList.get(valueList.size()/2) + valueList.get(valueList.size()/2-1)) / 2;
			} else {
				douu = valueList.get((valueList.size()-1) / 2);
			}

		}

		return douu;
	}
	/**
	 * 获取数字集合的均方差
	 * valueList集合无数据返回null
	 */
	public static Double numListStandardDeviation(List<Double> valueList){
		Double douu=null;
		if(valueList.size()!=0){//valueList集合无数据下会报错
			double total = 0;
			for(int i=0;i<valueList.size();i++) {
				total += valueList.get(i);
			}
			double avg = total/valueList.size();
			double dVar=0;
			for(int i=0;i<valueList.size();i++) {
				dVar+=(valueList.get(i)-avg) * (valueList.get(i)-avg);
			}
			douu=Math.sqrt(dVar/valueList.size());
		}

		return douu;
	}
	/**
	 * 判断字符串是否为数字(包括整数或浮点型数字)
	 * @param bol true:可为负数;false:不可为负数
	 * @return 是:返回true,否则返回false
	 */
	public static boolean isNumber(String str,boolean bol){
		if(str==null){return false;}
		String reg=null;
		if(bol){
			reg = "^-?[0-9]+(\\.[0-9]+)?$";
		}else{
			reg = "^[0-9]+(\\.[0-9]+)?$";
		}

		return str.matches(reg);
	}
	/**
	 * 判断字符串是否为整数
	 * @param bol true:可为负数;false:不可为负数
	 * @return 是:返回true,否则返回false
	 */
	public static boolean isInt(String str,boolean bol){
		if(str==null){return false;}
		String reg=null;
		if(bol){
			reg = "^-?[0-9]+$";
		}else{
			reg = "^[0-9]+$";
		}

		return str.matches(reg);
	}
	/**
	 * 对List<泛型无所谓>值判断,值会转为字符串进行对比,并返回第一个不为数字元素的下标,
	 * 否则返回null;(此方法只识别整数)	 *
	 */
	public static Integer processingListInteger(List list) {
		for(int i=0;i<list.size();i++){
			boolean bel=false;
			Object object = list.get(i);
			if(object!=null){
				bel=StringUtils.isNumeric(String.valueOf(object));

			}
			if(!bel){
				return i;
			}
		}
		return null;
	}
	/**
	 * 返回大数值,为null认为0.0做处理
	 * 一般用在两个参都不为null时必须大数是大数,小数是小数
	 * Double数值要带点的形式,参必须为Double类型
	 * @return
	 */

	public static Double getBigDouble(Double d1,Double d2) {
		if(d1==null){d1=0.0;}
		if(d2==null){d2=0.0;}
		if(d1>d2){
			return d1;
		}
		return d2;
	}
	/**
	 * 返回小数值,为null认为0.0做处理
	 * 一般用在两个参都不为null时必须大数是大数,小数是小数
	 * Double数值要带点的形式,参必须为Double类型
	 * @return
	 */

	public static Double getLittleDouble(Double d1,Double d2) {
		if(d1==null){d1=0.0;}
		if(d2==null){d2=0.0;}
		if(d1>d2){
			return d2;
		}
		return d1;
	}

	/**
	 * List元素个数为0返回null,否则正常返回
	 * @return
	 */

	public static List<?> listReturn(List<?> list) {
		if(list.size()==0){
			return null;
		}
		return list;
	}



	/**
	 * String为null赋为"",否则去两边空格
	 * @return
	 */

	public static String nvlString(Object obj) {
		return obj == null ? "" : String.valueOf(obj).trim();
	}
	/**
	 * String为""或NULL,赋为NULL,否则去两边空格
	 * 补充:"   "会为""而不是null
	 * @param str String
	 * @return  String
	 */

	public static String eToNULL(String str) {
		return str==null||str.equals("") ? null : str.trim();
	}



	/**
	 * byte[]转换为BASE64位的字符串
	 * @return String
	 * NULL->NULL
	 */
	public static String byteToBase64(byte[] bt) {
		if(bt==null){return null;}//不做null处理会异常
		//return Base64.encodeBase64String(bt);//import org.apache.commons.codec.binary.Base64;
		return new BASE64Encoder().encode(bt);//import sun.misc.BASE64Decoder;是jdk1.8的包
	}
	/**
	 * BASE64位的字符串转换为byte[]
	 * @return byte[]
	 * NULL|异常->NULL
	 */
	public static byte[] base64ToByte(String str) {
		if(str==null){return null;}//不做null处理会异常
		//return Base64.decodeBase64(str);//此不用写try-catch
		try{
			return new BASE64Decoder().decodeBuffer(str);
		}catch (Exception e){
			return null;
		}
	}

	/**
	 * SHA1加密方式
	 * @return 40位的16进制-小写形式
	 * ""|NULL->NULL
	 */
	public static String SHA1(String str){
		if (null == str || 0 == str.length()){
			return null;
		}
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f'};
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes("UTF-8"));

			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] buf = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}




	/**
	 * MD5加密方式;为32位的MD5加密方式,全小写返回
	 * 16位的方法里有注释解决
	 * @return
	 */
	public static String stringMD5(String pw) {

		String resultString = null;
		if(pw==null){return "";};
		resultString = new String(pw);
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			//使用指定的字节数组更新摘要。
			md.update(pw.getBytes());
			//通过执行诸如填充之类的最终操作完成哈希计算。
			byte b[] = md.digest();
			//生成具体的md5密码到buf数组
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}

			resultString = buf.toString();// 32位的加密
			//resultString = buf.toString().substring(8, 24);// 16位的加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return resultString;

	}

	/**
	 * 订单id由日期+五位随机数组成
	 * @return
	 */

	public static String dingdanId() {
		String dingdan=new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+(int) (Math.random() * (99999 - 10000) + 10000);
		return dingdan;
	}
	/**
	 * 返回从1970至今的一个毫秒数+两位随机数,-且必须是两位数字;例05
	 * @param
	 * @return	Long类型的
	 */

	public static Long getLong() {
		Long longs=System.currentTimeMillis();
		int ends=new Random().nextInt(99);
		String idString=longs+String.format("%02d", ends);
		Long id=new Long(idString);
		return id;
	}
	/**
	 * in('a','b','c') 使用占位符
	 * @author 子火
	 * @param	str 格式:a,b,c|'a','b','c'|"a","b","c"
	 * @param list 是对应给占位符赋值的;
	 * @param col 列名
	 * @return	字符串 (col in(?,?,?)) str为空,返回"(col in (?))",list会赋一个元素""
	 * 超出400个,会返回 (col in(?,?,?) or col in(?,?,?))
	 * 注意,使用时例 where 1=1 and "+Helper.inPlaceholder(str,list,col)+" order by createtime
	 */

	public static String inPlaceholder(String str,List list,String col) {
		if(StringUtils.isBlank(str)){
			str="("+col+" in (?)";
			list.add("");
			return str;
		}
		str=str.replaceAll("'","").replaceAll("\"","");
		String[] strr=str.split(",");
		StringBuffer restr=new StringBuffer();
		//一个in里放多少个
		int inNums=400;
		//数组里有多少个元素
		int strrLength=strr.length;
		//需要几个in,最少一个
		double isInNum=Math.ceil((double)strrLength/inNums);
		restr.append(" (");
		for(int i=1;i<=isInNum;i++){
			if(i!=1){
				restr.append(" or ");
			}
			restr.append(col+" in (");
			for(int ii=(i-1)*inNums,j=0;j<inNums&&ii<strrLength;ii++,j++){
				if(j==0){
					restr.append("?");
				}else{
					restr.append(",?");
				}
				list.add(strr[ii]);
			}
			restr.append(") ");
		}
		restr.append(") ");
		return restr.toString();
	}

	/**
	 * 将 a_bB 转为 aBb
	 * @param str 用正则写也可
	 * @return
	 */
	public static String transformPrelude(String str) {
		String strT="";
		if(StringUtils.isBlank(str)){
			return strT;
		}
		strT=str.trim().toLowerCase();
		StringBuffer sub=new StringBuffer();
		boolean up=false;
		for(int i=0;i<strT.length();i++){
			char item=strT.charAt(i);
			if('_'==item){
				up=true;
				continue;
			}
			if(up){
				sub.append(String.valueOf(item).toUpperCase());
			}else{
				sub.append(item);
			}
			up=false;
		}
		strT=sub.toString();
		return strT;
	}
// ----------------------------------------------------------------------------------------------------------------
//--------------------------------------数据类型转换--------------------------------------------------------------------


	/**
	 *List<Map<String,Object>>-->List<实体类>
	 *@param listMap 参1的key要同属性值;value是null则为默认值(逻辑见下continue处);
	 *@param cla    参2是实体类的class对象,两个参其中一方为null返回null
	 *@return 接收:List<实体类>对象=此方法;
	 *实体类是Float|Long|float|long|List|Map类型数据我没做处理,因此如果需要后期再添
	 *此方法会对map的值eToNULL;猜方法里是遍历属性,根据属性提map值,依据'@exception'规则赋值
	 *现在只是对属性String|Double(不是数字为0.0)|Integer(不是数字为0)|Date值(此类型仅限"yyyy-MM-dd",不是日期为null)提取,其它类型以待后期补充
	 *发生异常返回null,注无法转换的会赋值为默认值
	 */
	public static List listMapToListObj(List<Map<String,Object>> listMap,Class cla) {
		if(cla==null||listMap==null){return null;}//否则空指针异常
		List listObj=new ArrayList();
		for(Map<String,Object> map: listMap){
			try {
				//Object
				Class cl=cla;
				Object lei=cl.newInstance();
				Field[] declaredFields = cl.getDeclaredFields();
				for(int i=0;i<declaredFields.length;i++){
					Field field = declaredFields[i];
					field.setAccessible(true);
					String name = field.getName();//属性值
					boolean isStrType=true;//Map的value是字符串类型
					Object valObject=map.get(name);
					if(valObject==null){
						continue;
					}
					String string=null;
					if(valObject.getClass()!=String.class){//注意valObject.getClass()不会是基本数据类型
						isStrType=false;
					}else{
						string = Helper.eToNULL((String)map.get(name));//map值
					}
					Class classs=field.getType();
					Object objvalue=null;
					if(classs==String.class){
						objvalue=string;
					}else if(classs==Double.class){
						Double dou=0.0;
						if(isStrType){
							if(Helper.isNumber(string, true)){
								dou=Double.parseDouble(string);
							}
						}else{
							if(valObject.getClass()==Double.class){
								dou=(Double) valObject;
							}
						}
						objvalue=dou;
					}else if(classs==Integer.class){
						Integer intt=0;
						if(isStrType){
							if(Helper.isInt(string, true)){
								intt=Integer.parseInt(string);
							}
						}else{
							if(valObject.getClass()==Integer.class){
								intt=(Integer)valObject;
							}
						}
						objvalue=intt;
					}else if(classs==Date.class){
						Date da=null;
						if(isStrType){
							if(Helper.isDate(string)){
								da=Helper.stringToDateSuper(string);
							}else if(Helper.isDateTime(string)){
								da=Helper.stringToDate(string,"yyyy-MM-dd HH:mm:ss");
							}
						}else{
							if(valObject.getClass()==Date.class){
								da=(Date) valObject;
							}else if(valObject.getClass()==Long.class){
								da=new Date((Long) valObject);
							}
						}
						objvalue=da;
					}else if(classs==int.class){
						int intt=0;
						if(isStrType){
							if(Helper.isInt(string, true)){
								intt=Integer.parseInt(string);
							}
						}else{
							if(valObject.getClass()==Integer.class){
								intt=(Integer)valObject;
							}
						}
						objvalue=intt;
					}else if(classs==double.class){
						double dou=0.0;
						if(isStrType){
							if(Helper.isNumber(string, true)){
								dou=Double.parseDouble(string);
							}
						}else{
							if(valObject.getClass()==Double.class){
								dou=(Double) valObject;
							}
						}
						objvalue=dou;
					}
					field.set(lei,objvalue);
				}
				listObj.add(lei);
			} catch (Exception e) {
				String exceptionToString = Helper.exceptionToString(e);
				System.out.println("Helper页面 listMapToListObj 方法报错"+exceptionToString.substring(0,500>exceptionToString.length()?exceptionToString.length():500));
				return null;
			}
		}

		return listObj;
	}
	/**
	 * String(json)->Map//注意事项可参考stringJSONToList()方法注释
	 * 参json(map格式)value值为[]自动转换为list;只要带""就会为字符串,例"[]";5自动转为Integer,5.0自动转为Double;若获取值时,值类型与接收时的泛型冲突报错!!!
	 * 对于转换后的字符串进行判断详见stringJSONToList()方法注释
	 * String str="{'tabconfig':[{'id':'ys_ry','title':'人员'},{'id':'ys_asj','title':'案（事）件'}],'merginattr':'isdetail','app_name':'组合模糊查询','deleteflag':'0'}";('也可为\")
	 * 前台的json->str,后台转换时不会报错,若自己写或拼一个传后台执行此方法(value是[]格式的字符串,即{'':'[]'}会报错,只能是\"['a']\"或'[\"a\"]'会报错,若就想"[""]"可用多个转义字符解决
	 * 后台接收此方法返回值时:若写泛型要注意类型冲突,不写其泛型会默认为Object
	 * String(json)
	 * 旧写法发现数据里有 \r\n 或 \r 或 \n 转换时报错,下是新写法,所以没这问题
	 * 但如果字符串输出内容带"\" 如 {"colMatch":["[A-Z0-9\-]} 执行下会报错(旧写法没这问题)
	 * 	可 a()方法 暂时去掉"\" 转换Map后 b()方法,再转回来
	 * 		.replaceAll("\\\\","反斜杠暂时去掉");
	 * @return Map
	 */
	public static Map stringJSONToMap(final String json)
	{
		Map<String,Object> map = JSON.parseObject(json, Map.class);
		return map;
	}


	/**
	 * "[]"->[]	输出此字符串应该是['pm25']而不是"['pm25']"在显示台		  *
	 * "['null']"	判断用equals("\"null\"")返回true;
	 * "['NULL']"	判断用equals("NULL")
	 * "['']"		判断用equals("")		  *
	 * "[NULL]"|""|"null"              转换List报错
	 * "['sg5gg','10']"-->接收为List<Integer> list1不会处理也不会报错,输出为[sg5gg, 10];当提取元素[0]值时,值与泛型冲突报错,解决:看下总结
	 * "['sg5gg',10]"-->接收为List<Integer>,赋值给Integer不会报错;注意list元素1类型为String,元素2类型为Internet
	 * 上两行总结--泛型不会起转换的作用,'10'会自动转换为String 10 会自动转换为Integer,当提取时(例list1.get(0))的值与接收的list泛型冲突会报错;1-只要一提取就把它赋值给Object变量,否则即使只是输出也报错|2-接收就List<Object>
	 * 若list必须为数字,拦截处理可用processingListInteger()方法,方法里元素会转为字符串对比哪个不为数字
	 * 在mybatis的xml中jdbcType值与list类型不对应,不会报错,同sql列为数字类型,查询时列='字符串'不会异常只是无查询数据一样
	 * 当元素是[]格式的字符串,例"['[]','[]']";
	 *		通过前台的json->str进行转换,在后台获取执行此方法是可以的.
	 * 	若后台执行此方法,参是自己定义的字符串(只能是"[\"['a']\",\"['a']\"]"或"['[\"a\"]','[\"a\"]']",若就想"[""]"可用多个转义字符解决
	 * 当元素是[]格式,例"[['e','3'],[\"e\",\"r\"]]";->list<List>;当元素是{}格式->list<Map>
	 * 2.4版本json-lib- "[null]"	判断用equals("null")返回true;参为null list里会有一个元素,判断用equals("null")//转为integer报错
	 * 2.1版本- "[null]"	| null返回[{}],获取的是个Map
	 * 为什么规则变了,因为jar包2.1?
	 */
	public static List stringJSONToList(final String json)
	{
		final List list = JSONObject.parseArray(json);
		return  list;
	}

	/**
	 * Integer->String
	 * 当integer为null;布尔参为true返回"",否则返回null
	 * @return String
	 */
	public static  String integerToString(Integer intt,boolean pd){
		if(intt==null){
			if(pd){
				return "";
			}
			return null;
		}else{
			return String.valueOf(intt);
		}
	}


	/**
	 * pojo实体类->String(json)
	 * 返回字符串例 "{\"str\":\"xx\",\"intt\":4,\"douu\":9.99,\"datt\":1568787915759}" 这四个属性分别是String Integer Double Date 类型
	 * 注意上datt在实体类中是日期类型;值转为日期可:Long l=1568787915759L; Date Date =new Date(l);
	 * 上字符串如果转为Map datt会是Long类型;
	 * 上字符串可直接使用 stringJSONToPojo 转为pojo实体类
	 * @return String
	 */
	public static String pojoToStringJSON(final Object object){
		try {
			String writeValueAsString = MAPPER.writeValueAsString(object);
			return writeValueAsString;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * String(json)->类(对象)
	 * 使用例Login login = Helper.stringJSONToPojo(str, Login.class) ;
	 * 参1为null会异常返回null
	 * @return 返回类----异常会返回null-----{}的key不为类属性名,值无法转换为对应属性的(如在类里没有此或不对应的),产生异常,返回null
	 * 若json里没写的,属性赋为默认,例参为{}返回类,属性值都是默认值
	 * 实体类里面最好只有属性及其set&get方法,否则在转为字符串时会多些东西,再转为实体类时异常,如{}字符串里会多些key值
	 * "{\"receivetime\":\"\"}"变量为Date或Integer值为NULL;为String值为""
	 * "{\"storageroom\":\"null\"}"//变量为Date或Integer值为NULL;为String值为"null"
	 * 前台的json->str,后台转换时不会报错,若自己写或拼一个传后台执行此方法,属性是字符串,只是[]格式的json只能是\"['a']\",否则报错返回null;若就想"[""]"可用多个转义字符解决
	 */
	public static <T>T  stringJSONToPojo(String string,Class<T> class1){
		try {
			T t =MAPPER.readValue(string, class1);
			return t;
		} catch (Exception e) {

			return null;
		}


	}
	/**
	 * String(json)->List<类>
	 * @param {}的key不为类属性名,json值无法转换为对应属性的,产生异常(猜不是标准的json字符串(要为\")也会异常)
	 * 实体类 属性类型为Date-json为""(猜或null)值会为null	;json里无此属性,类属性值为默认值
	 * 前台的json->str,后台转换时不会报错,若自己写或拼一个传后台执行此方法,属性是字符串,[]格式的json只能是\"['a']\",否则报错返回null;若就想"[""]"可用多个转义字符解决
	 * @return List<类>
	 */

	public static <T>List<T>  stringJSONToListPojo(String string,Class<T> class1){
		JavaType constructParametricType = MAPPER.getTypeFactory().constructParametricType(List.class, class1);
		try {
			List<T> list = MAPPER.readValue(string, constructParametricType);
			return list;
		} catch (Exception e) {
			return null;
		}


	}


	/**
	 *  String-->集合,字符串元素是- / 空格 年月日  四种日期格式的字符串
	 *  日期范围例(0-9999)-(1-12)-(1-31)
	 *  转不了返回空集合.size()==0
	 */
	public static List<String> stringToArrayDate(String str){
		List<String> date=new ArrayList<String>();
		String pp="(\\d{1,4}(-|/| )(([0]?[1-9])|([1][0-2]))(-|/| )([12][0-9]|3[01]|0?[1-9]))|(\\d{1,4}年(([0]?[1-9])|([1][0-2]))月([12][0-9]|3[01]|0?[1-9])日)";
		Pattern pattern = Pattern.compile(pp);
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			date.add(matcher.group(0));
		}
		return date;
	}
	/**
	 *  String-->Date 支持- / 空格 年月日  四种格式
	 *  日期范围例(0-9999)-(1-12)-(1-31)
	 *  转不了返回null
	 */
	public static Date stringToDateSuper(String str){
		if(str==null||str.equals("")){
			//System.out.println("helper-stringToDate-str-null");
			return null;
		}

		String reg1 = "\\d{1,4}-(([0]?[1-9])|([1][0-2]))-([12][0-9]|3[01]|0?[1-9])";
		String reg2 = "\\d{1,4}/(([0]?[1-9])|([1][0-2]))/([12][0-9]|3[01]|0?[1-9])";
		String reg3 = "\\d{1,4}年(([0]?[1-9])|([1][0-2]))月([12][0-9]|3[01]|0?[1-9])日";
		String reg4 = "\\d{1,4} (([0]?[1-9])|([1][0-2])) ([12][0-9]|3[01]|0?[1-9])";
		String ge="";
		if(str.matches(reg1)){
			ge="yyyy-MM-dd";
		}else if(str.matches(reg2)){
			ge="yyyy/MM/dd";
		}else if(str.matches(reg3)){
			ge="yyyy年MM月dd日";
		}else if(str.matches(reg4)){
			ge="yyyy MM dd";
		}else{
			return null;
		}

		SimpleDateFormat x=new SimpleDateFormat(ge);
		//格式(可换为其它格式)例:"yyyy-MM-dd"里可再加汉字,不能为字母
		Date da=null;
		try {
			da = x.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//猜str格式也要为yyyy-MM-dd格式
		return da;

	}
	/**
	 *  String-->Date
	 *  给数据库中类型为Date的列所传的值也要为Date类型
	 * @param str String -日期--为""|null会返回null
	 * @param ge String ge-格式如"yyyy-MM-dd"//yyyy只是表年份并不是一定要四位
	 * //猜str格式也要为yyyy-MM-dd格式
	 * //格式(可换为其它格式)例:"yyyy-MM-dd"里可再加汉字,不能为字母
	 * 参不对会出现异常,其它情况参看JAVA\Date\使用--46行
	 * 又例"2017年8月"会转为2017-8-1
	 * yyyy-MM-dd HH:mm:ss
	 * Helper.stringToDate("2019-1-51 25:3:3","yyyy-MM-dd HH:mm:ss") 也是可以的
	 * 这个月或这天的最后时刻,思路如下
	 * Date endTime=Helper.stringToDate(endtime+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
	 */

	public static Date stringToDate(String str,String ge) throws ParseException{
		if(str==null){
			//System.out.println("helper-stringToDate-str-null");
			return null;
		}
		if(str.equals("")){
			//System.out.println("helper-stringToDate-str-空");
			return null;
		}

		SimpleDateFormat x=new SimpleDateFormat(ge);
		//格式(可换为其它格式)例:"yyyy-MM-dd"里可再加汉字,不能为字母
		Date da=x.parse(str);
		//猜str格式也要为yyyy-MM-dd格式
		return da;

	}
	/**
	 * List<>->String(json)
	 * <单元素>转换后例：[1,2]		.js获取例：data[0]
	 * <Map>转换后例：[{"b":2,"a":1},{"A":"1","B":"2"}]	.js获取例：data[int].b
	 * @param list
	 * @return String;为null返回[null]
	 * 注意:在使用fromObject的时候会遇到一种情况就是当对象的某一个Double型或Integer型的属性为空的时候，转JSON的时候会变成0.0
	 * 当一个布尔型的属性为空的时候，转JSON的时候会变成FALSE,(猜字符串为null转为"",日期为null转为null)
	 *
	 */
	public static String listToStringJSON(final List list){
		return JSONArray.toJSONString(list);
	}
	/**
	 * Map->String(json);猜:原旧写法key必须为String类型,如下新写法,没这问题
	 * 转换后例：{"a":1,"b":2}		.js获取例：data.b
	 * 返回值key顺序是按照哈希表排序
	 * @param map Map
	 * @return String
	 */
	public static String mapToStringJSON(final Map map){
		return JSONObject.toJSONString(map);
	}
	/**
	 *获取堆栈异常信息Exception e->String
	 *然后输出sys("页面位置"+str),可替代!!e.print...
	 * 作用1:是转为字符串后截取一定的长度进行输出
	 * 	因为,当需要在后台输出看时e.print...太长会造成资源浪费(一般不会这样用,后台看哈,前台看去)
	 * 	例 String strE=Helper.exceptionToString(e);
	 *     String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
	 * 	   System.out.println(strEInfo);
	 * 作用2,方便捕获到异常后返回给前台
	 * e.getMessage():非常简洁的异常信息;e.toString()简洁的异常信息;Helper.exceptionToString(e)-全
	 * 		详见 JAVA\其它\小知识点\try+return
	 * @param t
	 * @return String
	 */
	public static String exceptionToString(Throwable t){
		StringWriter sw=new StringWriter();
		PrintWriter pw =new PrintWriter(sw);
		try{
			t.printStackTrace(pw);
			return sw.toString();

		}finally{
			pw.close();
		}

	}

//----------------------------------------------------------------------------------------------------------------------
//--------------------------------------------HttpClient--------------------------------------------------------------------------

	/**
	 *HttpClient的Get请求--请求是静态资源,获取返回的资源,是方法获取返回值
	 * @param url String url(例www.xxx.cn/content/81)
	 * @param param Map(所传递的"表单"值)--如果有map注意其value值不能为NULL
	 * @param bol 在方法外声明个bol,若值被更改为true表是异常直接返回前台即可
	 * @return String
	 */
	public static String doGet(String url,Map<String, String> param,boolean bol){
		//param为url赋上变量(即占位符?),如果没有可用null表示
		CloseableHttpClient client=HttpClients.createDefault();
		String resultString="";
		CloseableHttpResponse closeableHttpResponse=null;
		try {
			URIBuilder builder=new URIBuilder(url);
			if(param!=null){
				for(String key:param.keySet()){
					builder.addParameter(key, param.get(key));
				}

			}
			URI uri=builder.build();
			HttpGet httpGet=new HttpGet(uri);
			closeableHttpResponse =client.execute(httpGet);
			if(closeableHttpResponse.getStatusLine().getStatusCode()==200){
				resultString=EntityUtils.toString(closeableHttpResponse.getEntity(),"UTF-8");

			}
		} catch (Exception e) {
			bol=true;
			return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
		}finally{
			try {
				if(closeableHttpResponse!=null){
					closeableHttpResponse.close();
				}
				client.close();
			} catch (IOException e) {
				bol=true;
				return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
			}

		}
		return resultString;

	}
	/**
	 *HttpClient的Post请求--扩展见笔记 \JAVA\类\HttpClient扩展.txt(可配置请求协议和配置)
	 * @param url String (例www.xxx.cn/posttest)
	 * @param param Map(所传递的"表单"值)--如果有map注意其value值不能为NULL
	 * @param bol 在方法外声明个bol,若值被更改为true表是返回的是异常
	 * @return String
	 * controller处,例[]--post表只接收post请求,不用如下,别的也成
	 * [@RequestMapping(value="/posttest",method=RequestMapping.POST")]
	 * 发现:地址处?a=xx;map里有"a":"oo";输出a(controller方法参a)--xx,oo;
	 * controller方法里执行request.getParameter("a");若两处都赋值了,则输出?处的值;若只一处赋值,都能获取且输出
	 * 注意用?传json格式的字符串,后台报错,用map传可以后台获取(用参及request都可以)
	 */
	public static String doPost(String url,Map<String, String> param,boolean bol){
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse closeableHttpResponse=null;
		String resultString="";
		try {
			HttpPost post = new HttpPost(url);
			if(param!=null){
				List<NameValuePair> formList = new ArrayList<NameValuePair>();
				for(String key:param.keySet()){
					formList.add(new BasicNameValuePair(key, param.get(key)));
					//formList.add(new BasicNameValuePair("name", "张三"));
				}
				UrlEncodedFormEntity entity=new UrlEncodedFormEntity(formList, "utf-8");
				post.setEntity(entity);
			}
			closeableHttpResponse=httpClient.execute(post);
			resultString= EntityUtils.toString(closeableHttpResponse.getEntity(),"utf-8");

		}  catch (Exception e) {
			bol=true;
			return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
		}finally{

			try {

				closeableHttpResponse.close();

			} catch (IOException e) {
				bol=true;
				return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
			}
		}
		return resultString;
	}




	/**
	 *HttpClient的传值为json的Post请求--录屏-第六天-09//00:14:56
	 * @param url String
	 * @param json String json(如{"a":"aa","b":"bb"})--猜值不能为NULL
	 * @param bol 在方法外声明个bol,若值被更改为true表是异常直接返回前台即可
	 * @return String
	 *  注意controller处,例[]
	 * [@RequestMapping(value="/posttest",method=RequestMapping.POST")
	 *	@ResponseBody 方法(@RequestBody Map map)]					  *
	 */
	public static String doPostJSON(String url,String json,boolean bol){
		CloseableHttpClient httpClient=HttpClients.createDefault();
		CloseableHttpResponse response=null;
		String resultString="";
		try {
			HttpPost httpPost=new HttpPost(url);
			StringEntity entity=new StringEntity(json,ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			response=httpClient.execute(httpPost);
			resultString=EntityUtils.toString(response.getEntity(),"utf-8");

		} catch (Exception e) {
			bol=true;
			return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
		}finally{
			try {
				response.close();
			} catch (IOException e) {
				bol=true;
				return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
			}
		}
		return resultString;
	}

	/**
	 * 请求一卡易接口--示例代码在SSM(MAVEN)\技术汇总\跨域+接口\请求一卡易接口
	 * 和HttpClient看着就不一样
	 * @param urls 参urls例http://openapi.1card1.cn/OpenApi/Get_MembersPagedV2?openId=E1FC5F553C2D48188000D386E86AC663&signature="+urlString+"&timestamp="+time
	 * @param data 参data例"{\"cardId\":\"desc\",\"orderBy\":\"\",\"pageSize\":\"10\",\"userAccount\":\"10000\",\"pageIndex\":\"0\"}"
	 * @param bol 在方法外声明个bol,若值被更改为true表是异常直接返回前台即可
	 */
	public static String doPostYKY(String urls,String data,boolean bol){
		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		// BufferedReader bufferedReader = null;
		StringBuffer responseResult = new StringBuffer();
		StringBuffer params = new StringBuffer();
		HttpURLConnection httpURLConnection = null;
		// 组织请求参数
		params = params.append("&data=" + data);

		try {
			URL realUrl = new URL(urls);
			// 打开和URL之间的连接
			httpURLConnection = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			//如果不设置超时,在网络异常的情况下,可能会导致程序僵死而不继续往下执行;默认0-不会超时;此设置为30s
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);

			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Content-Length", String
					.valueOf(params.length()));
			httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
			//	httpURLConnection.setRequestProperty("Content-type", "text/html");
			httpURLConnection.setRequestProperty("contentType", "utf-8");
			httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			String dataStr = new String(params.toString().getBytes(), "utf-8");
			printWriter.write(dataStr);
			// flush输出流的缓冲
			printWriter.flush();
			// 根据ResponseCode判断连接是否成功
			int responseCode = httpURLConnection.getResponseCode();

			bufferedReader = new BufferedReader(new InputStreamReader(
					httpURLConnection.getInputStream(),"Utf-8"));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				responseResult.append(line);
			}

			if (responseCode == 200) {

				return responseResult.toString();

			} else {
				bol=true;
				return "{statusCode: \"300\", stateDescribe: \"查询接口发生错误\", valueDescribe: \"!?\"}";
			}
		} catch (Exception e) {
			bol=true;
			return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";

		} finally {
			httpURLConnection.disconnect();
			try {
				if (printWriter != null) {
					printWriter.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				bol=true;
				return "{statusCode: \"300\", stateDescribe: \"请求接口异常\", valueDescribe: \""+Helper.exceptionToString(e)+"\"}";
			}

		}


	}
//----------------------------------------------------------------------------------------------------------------------

//-----------------------------------Date-----------------------------------------------------------------------------------


	/**
	 * 北京时转为世界时
	 */
	public static Date dateBTOdateS(Date da) {
		Calendar calendar=Calendar.getInstance(); //获取calendar实例;
		calendar.setTime(da);
		calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)-8);
		Date dataDS=calendar.getTime();
		return dataDS;
	}
	/**
	 * 世界时转为北京时
	 */
	public static Date dateSTOdateB(Date da) {
		Calendar calendar=Calendar.getInstance(); //获取calendar实例;
		calendar.setTime(da);
		calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)+8);
		Date dataDB=calendar.getTime();
		return dataDB;
	}
	/**
	 * Date-->String("yyyy-MM-dd HH:mm:ss")
	 * @param da-Date 日期
	 * @param ge-String 格式如 "yyyy年MM月dd日"即转换str后的格式
	 * @return Date为null返回"",Oracle 的to_date('','YYYY-mm-dd')会返回null,所以需要考虑null的情况
	 */
	public static String dateToString(Date da,String ge) {
		if(da==null){return "";}
		SimpleDateFormat x=new SimpleDateFormat(ge);
		String str =x.format(da);
		return str;

	}
	/**
	 * 判断字符串是否符合年格式
	 * @param str 字符串要为yyyy格式
	 * @return 不符合返回当前年份
	 */
	public static String isYear(String str){
		String reg=	"^[0-9]{1,4}$";
		if(str==null||!str.matches(reg)){
			Calendar calendar=Calendar.getInstance();
			str=String.valueOf(calendar.get(Calendar.YEAR));
		}
		return str;
	}
	/**
	 * 判断字符串是否为日期
	 * @param str 字符串要为yyyy-MM-dd格式
	 * @return 不符合返回false
	 */
	public static boolean isDate(String str){
		if(str==null){return false;}
		//从网上找的校验(张浩磊)
		String reg="^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
		//原自写校验只能是2010-2029期间的日期(一些日期也会返回true如2000-2-29|2004-2-29|2068-2-29等)
		//reg = "^(((20[1-2][0-9]-)(((0{0,1}[13578]|1[02])-(0{0,1}[1-9]|[12][0-9]|3[01]))|((0{0,1}[469]|11)-(0{0,1}[1-9]|[12][0-9]|30))|(0{0,1}2-(0{0,1}[1-9]|1[0-9]|2[0-8]))))|(2000-0{0,1}2-29)|(20(0[48]|[246][048]|[135][26])-0{0,1}2-29))$";
		//reg = "^[0-9]{1,4}-(0?[1-9]|1[012])-([12][0-9]|3[01]|0?[1-9])$";
		return str.matches(reg);
	}
	/**
	 * 判断字符串是否为日期
	 * @param str 字符串要为yyyy-MM-dd HH:mm:ss格式
	 * @return 不符合返回false
	 */
	public static boolean isDateTime(String str){
		if(str==null){return false;}
		String reg = "^[0-9]{1,4}-(0?[1-9]|1[012])-([12][0-9]|3[01]|0?[1-9]) [0-9]{2}:[0-9]{2}:[0-9]{2}$";
		return str.matches(reg);
	}
	/**
	 * 根据开始时间和结束时间返回时间段内的时间集合 (包括xi与da!!!)即使日期一样,不处理,返回这两个日期
	 * 若想返回就一个可java判断是否是同一个(是则add只赋那一个值)否则执行此方法
	 * @param beginDate //开始时间
	 * @param endDate 	//结束时间
	 * @param ge --例为"yyyy-MM-dd HH:mm:ss",返回List泛型String;为null返回List泛型Date
	 * @param symbol --日期间隔多久放到集合里;默认为每次加一天从beginDate开始,若此值小于结束时间放集合里;+1年-y;+1月-m;+1日-d;+1小时-h;+1分钟-mm
	 * @param bool 最后一个(当元素个数大于1个时,因为要(包括xi与da))与结束时间是相同的年|月|日或时(此依据symbol而言)以哪个为准,true以结束时间为准否则以最后一个为准
	 * @param shu 增加量例1
	 * @return List
	 */
	public static List getDatesBetweenTwoDate(Date beginDate, Date endDate,String ge,String symbol,boolean bool,int shu) {
		List lDate = new ArrayList();

		if(ge==null){
			lDate.add(beginDate);// 把开始时间加入集合
		}else {
			lDate.add(dateToString(beginDate,ge));

		}
		Date lastDate=null;
		Calendar cal = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		cal.setTime(beginDate);
		boolean bContinue = true;
		while (bContinue) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			if("y".equals(symbol)){
				cal.add(Calendar.YEAR, shu);
			}else if("m".equals(symbol)){
				cal.add(Calendar.MONTH, shu);
			}else if("d".equals(symbol)){
				cal.add(Calendar.DAY_OF_MONTH, shu);
			}else if("h".equals(symbol)){
				cal.add(Calendar.HOUR_OF_DAY, shu);
			}else if("mm".equals(symbol)){
				cal.add(Calendar.MINUTE, shu);
			}else{
				cal.add(Calendar.DAY_OF_MONTH, shu);
			}

			// 测试此日期是否在指定日期之后
			if (endDate.after(cal.getTime())) {
				lastDate=cal.getTime();
				if(ge==null){
					lDate.add(cal.getTime());
				}else {
					lDate.add(dateToString(cal.getTime(),ge));
				}
			} else {
				break;
			}
		}

		if(lDate.size()>1){
			boolean pd=false;//是否重复
			if("y".equals(symbol)){
				if(lastDate.getYear()==endDate.getYear()){
					pd=true;
				}
			}else if("m".equals(symbol)){
				if(lastDate.getMonth()==endDate.getMonth()){
					pd=true;
				}
			}else if("d".equals(symbol)){
				if(lastDate.getDate()==endDate.getDate()){
					pd=true;
				}
			}else if("h".equals(symbol)){
				if(lastDate.getHours()==endDate.getHours()){
					pd=true;
				}
			}else if("mm".equals(symbol)){
				if(lastDate.getMinutes()==endDate.getMinutes()){
					pd=true;
				}
			}else{
				if(lastDate.getDate()==endDate.getDate()){
					pd=true;
				}
			}
			if(pd){
				if(bool){//true:以endDate为准
					if(ge==null){
						lDate.set(lDate.size()-1, endDate);
					}else{
						lDate.set(lDate.size()-1,dateToString(endDate,ge));
					}
				}
			}else{
				if(ge==null){
					lDate.add(endDate);// 把结束时间加入集合
				}else{
					lDate.add(dateToString(endDate,ge));
				}
			}
		}else{
			if(ge==null){
				lDate.add(endDate);// 把结束时间加入集合
			}else{
				lDate.add(dateToString(endDate,ge));
			}
		}

		return lDate;
	}

	/**
	 * 传日期返回,返回此日期所在月的最后一天的那个日期
	 */
	public static Date getBigMonthDate(Date date) {
		if(date==null){
			return null;
		}
		Calendar calendar=Calendar.getInstance(); //获取calendar实例;
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date dateReturn=calendar.getTime();
		return dateReturn;
	}

	/**
	 * 两个date日期比大小
	 * 返回大日期(会更改大日期的时间,为23:59:59的形式返回)
	 * @return 如果方法参有一个为null,直接返回null
	 */
	public static Date getChangeBigDate(Date date1, Date date2) {
		if(date1==null||date2==null){
			System.out.println("getChangeBigDate方法参为null,异常");
			return null;
		}
		if(date1.getTime()>date2.getTime()){
			Calendar calendar=Calendar.getInstance(); //获取calendar实例;
			calendar.setTime(date1);
			calendar.set(Calendar.HOUR_OF_DAY,23);//设置为最大时间
			calendar.set(Calendar.MINUTE,59);//设置为最大分钟
			calendar.set(Calendar.SECOND,59);//设置为最大秒数
			date1=calendar.getTime();
			return date1;
		}else {

			Calendar calendar=Calendar.getInstance(); //获取calendar实例;
			calendar.setTime(date2);
			calendar.set(Calendar.HOUR_OF_DAY,23);//设置为最大时间
			calendar.set(Calendar.MINUTE,59);//设置为最大分钟
			calendar.set(Calendar.SECOND,59);//设置为最大秒数
			date2=calendar.getTime();
			return date2;
		}
	}


	/**
	 * 两个date日期比大小
	 * 返回大日期(不对大日期做更改)
	 * 为null返回null
	 */
	public static Date getOnlyBigDate(Date date1, Date date2) {
		if(date1==null||date2==null){
			//System.out.println("getOnlyBigDate方法参为null");
			return null;
		}
		if(date1.getTime()>date2.getTime()){
			return date1;
		}else {
			return date2;
		}
	}
	/**
	 * 两个date日期比大小
	 * 返回小日期,参若有null返回null
	 */
	public static Date getLittleDate(Date date1, Date date2) {
		if(date1==null||date2==null){
			//System.out.println("helper--getLittleDate --null");
			return null;
		}
		if(date1.getTime()>date2.getTime()){
			return date2;
		}else {
			return date1;
		}
	}
	/**
	 * 两个date日期-返回月差(最好参1是大日期,不是也没关心有处理)
	 * 例'2017-2-1'与'2017-1-20'返回1
	 */
	public static int getMonthLess(Date bigDate, Date littleDate) {
		int yearnum=bigDate.getYear()-littleDate.getYear();
		int lengthM=0;
		if(yearnum==0){
			lengthM=bigDate.getMonth()-littleDate.getMonth();
		}else {
			if (littleDate.after(bigDate)){ //如果	bigDate在littleDate之前
				Date datee=bigDate;
				bigDate=littleDate;
				littleDate=datee;
			}

			lengthM=(yearnum-1)*12+(11-littleDate.getMonth())+(bigDate.getMonth()+1);
		}
		return lengthM;
	}
	/**
	 * 两个date日期-返回年差				     *
	 * 例'2017-12-1'与'2018-1-20'返回1
	 */
	public static int getYearLess(Date dateA, Date dateB) {
		int yearnum=dateA.getYear()-dateB.getYear();
		return Math.abs(yearnum);
	}
	/**
	 * 两个date日期-返回日差
	 * 例'2016-12-1'与'2017-1-20'返回50,返回日期中间的空格数,这样理解比较好
	 * 注意若是时分秒不一致所带来的影响
	 */
	public static int getDayLess(Date dateA, Date dateB) {
		long  yy=(dateA.getTime()-dateB.getTime())/(24*60*60*1000);
		return Math.abs((int)yy);
	}
	/**
	 * 两个date日期-返回时差
	 * 例'2017-1-1'与'2017-1-2'返回24,返回日期中间的空格数,这样理解比较好
	 * 注意若是时分秒不一致所带来的影响
	 */
	public static int getHourLess(Date dateA, Date dateB) {
		long  yy=(dateA.getTime()-dateB.getTime())/(60*60*1000);
		return Math.abs((int)yy);
	}
	/**
	 * 两个date日期-返回分差
	 * 返回日期中间的空格数,这样理解比较好
	 * 注意若是时分秒不一致所带来的影响
	 * 5/2;返回2.0--即同天时23:58:59同23:58:00返回值
	 */
	public static int getMinuteLess(Date dateA, Date dateB) {
		long  yy=(dateA.getTime()-dateB.getTime())/(60*1000);
		return Math.abs((int)yy);
	}
	/**
	 * 求分,时,日,月,年差				     *
	 */
	public static int getAllLess(Date dateA, Date dateB,String ge) {
		if("h".equals(ge)){
			return getHourLess(dateA,dateB);
		}else if("d".equals(ge)){
			return getDayLess(dateA,dateB);
		}else if("m".equals(ge)){
			return getMonthLess(dateA,dateB);
		}else if("y".equals(ge)){
			return getYearLess(dateA,dateB);
		}else if("mm".equals(ge)){
			return getMinuteLess(dateA,dateB);
		}else{
			return 0;
		}
	}
//----------------------------------------------------------------------------------------------------------------------
//------------------Excel文件-------------------------------------------------------------------------------------------------
	/**
	 *
	 * @param inp-InputStream流类型
	 * @return 返回和流对应的HSSFWorkbook 还是 XSSFWorkbook
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	@SuppressWarnings("deprecation")
	public static Workbook getWorkbook(InputStream inp) throws IOException, InvalidFormatException{
		// If clearly doesn't do mark/reset, wrap up

		return WorkbookFactory.create(inp);//poi 版本4.1.0
		/*
		if(! inp.markSupported()) {//poi 版本 3.16-beta1
			inp = new PushbackInputStream(inp, 8);
		}
		if(POIFSFileSystem.hasPOIFSHeader(inp)) {
			return new HSSFWorkbook(inp);
		}
		if(POIXMLDocument.hasOOXMLHeader(inp)) {
			return new XSSFWorkbook(OPCPackage.open(inp));
		} */ //此处若报错猜因为jar版本不同
		//throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
	}
	/**
	 * 导出Excel
	 * 参1是list,泛型是实体类
	 * 参4做表头(赋值顺序要为实体类属性的书写顺序否则对应不上),为null第一行为空
	 */
	public static void exportExcel(List list,HttpServletRequest request,HttpServletResponse response,List<String> listt){

		//System.out.println(cla==String.class);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet=wb.createSheet("sheet");


		//表头
		if(listt!=null&&listt.size()!=0){
			HSSFRow row0=sheet.createRow(0);
			for(int i=0;i<listt.size();i++){
				Object obj=listt.get(i);
				String str=null;
				if(obj!=null){
					str=String.valueOf(obj);
				}
				HSSFCell cell = row0.createCell(i);
				cell.setCellValue(str);
			}
		}


		for(int i=1;i<=list.size();i++){
			Object obj=list.get(i-1);
			if(obj==null){continue;}
			HSSFRow row=sheet.createRow(i);
			Class  class1 = obj.getClass();
			Field[] declaredFields = class1.getDeclaredFields();
			for(int j=0;j<declaredFields.length;j++){
				HSSFCell cell = row.createCell(j);
				Field fie=declaredFields[j];
				fie.setAccessible(true);
				Object object=null;
				try {
					object = fie.get(obj);
				} catch (Exception e) { System.out.println("报错A:Helper的exportExcel方法");}
				if(object==null){
					String st=null;
					cell.setCellValue(st);
				}else{
					if(object.getClass()==Date.class){
						cell.setCellValue(Helper.dateToString((Date)object, "yyyy-MM-dd HH:mm:ss"));
						//System.out.println(Helper.dateToString((Date)object, "yyyy-MM-dd HH:mm:ss"));
					}else{
						cell.setCellValue(String.valueOf(object));
					}
				}
			}
		}
		try {
			OutputStream output=null;
			output = response.getOutputStream();
			response.reset();
			response.setHeader("Content-disposition", "attachment; filename=export.xls");//名字不能为中文?
			response.setContentType("application/msexcel");
			wb.write(output);
			output.close();
		} catch (Exception e) {
			System.out.println("报错B:Helper的exportExcel方法");
		}

	}

	/**
	 * 导出Excel
	 * 参1是list,泛型是Map
	 * 参4为表头中文(),为null第一行为空
	 * 参5为表头英文()
	 * 参6为导出表名
	 */
	public static void exportExcelMapN(List<Map<String, Object>> list,HttpServletRequest request,HttpServletResponse response,List<String> listt,List<String> listy,String tabName){

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet=wb.createSheet("sheet");
		//表头
		if(listt!=null&&listt.size()!=0){
			HSSFRow row0=sheet.createRow(0);
			for(int i=0;i<listt.size();i++){
				Object obj=listt.get(i);
				String str=null;
				if(obj!=null){
					str=String.valueOf(obj);
				}
				HSSFCell cell = row0.createCell(i);
				cell.setCellValue(str);
			}
		}
		for(int i=1;i<=list.size();i++){
			Map<String, Object> map=list.get(i-1);
			if(map==null||map.size()==0){continue;}
			HSSFRow row=sheet.createRow(i);
			for(int j=0;j<listy.size();j++){
				HSSFCell cell = row.createCell(j);
				String keyy=listy.get(j);
				Object object=null;
				try {
					object =map.get(keyy);
				} catch (Exception e) { System.out.println("报错A:Helper的exportExcel方法");}
				if(object==null){
					String st=null;
					cell.setCellValue(st);
				}else{
					if(object.getClass()==Date.class){
						cell.setCellValue(Helper.dateToString((Date)object, "yyyy-MM-dd HH:mm:ss"));
						//System.out.println(Helper.dateToString((Date)object, "yyyy-MM-dd HH:mm:ss"));
					}else{
						cell.setCellValue(String.valueOf(object));
					}
				}
			}
		}
		try {
			OutputStream output=null;
			output = response.getOutputStream();
			response.reset();
			tabName=new String(tabName.getBytes("GB2312"), "ISO_8859_1"); //中文名
			response.setHeader("Content-disposition", "attachment; filename="+tabName+".xls");
			response.setContentType("application/msexcel");
			wb.write(output);
			output.close();
		} catch (Exception e) {
			System.out.println("报错B:Helper的exportExcel方法");
		}

	}
	/**
	 * 根据excle的cell,返回值,若Cell为null返回""
	 * 提取excle值时用上了
	 */
	public static String getCellValue(Cell cell) {
		String value = null;
		if(cell==null){
			//System.out.println("检测到cell为null");
			value="";
		}else{
			switch (cell.getCellTypeEnum()) {
				case FORMULA:
					value =cell.getCellFormula();
					break;

				case NUMERIC:
					value =formatNumericCell(cell.getNumericCellValue(), cell);
					//System.out.println("值:"+cell.getNumericCellValue()+"--对比--"+formatNumericCell(cell.getNumericCellValue(), cell));
					break;

				case STRING:
					value =cell.getStringCellValue();
					break;

				default:
			}


		}
		return nvlString(value);
	}
	/**
	 * 原样返回数值(日期)单元格的内容--被上方法调用
	 */
	public static String formatNumericCell(Double value, Cell cell) {
		try {
			//System.out.println("xx"+value+"xx");
			if(cell.getCellTypeEnum() != CellType.NUMERIC && cell.getCellTypeEnum() != CellType.FORMULA) {
				return null;
			}
			if(DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				DataFormatter dataFormatter = new DataFormatter();
				Format format = dataFormatter.createFormat(cell);
				return format.format(date);
			} else {
				DataFormatter dataFormatter = new DataFormatter();
				Format format = dataFormatter.createFormat(cell);
				return format.format(value);
			}
		} catch (Exception e) {
			System.out.println("报错:Helper-formatNumericCell方法");
		}
		return null;//无法解析,里面异常时,返回null
	}
}
