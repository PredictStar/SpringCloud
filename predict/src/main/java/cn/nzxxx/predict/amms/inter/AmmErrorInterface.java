package cn.nzxxx.predict.amms.inter;

/**
* @ClassName: AmmErrorInterface  
* @Description: 记录错误代码的类  
* @author zhigangwang
* @date 2017年1月16日 下午12:11:57  
*/
public interface AmmErrorInterface {
	
	//成功的标记
	public static final String SUCCESS = "SUCCESS";
	
	//操作失败
	public static final String FAIL = "FAIL";
	
	//上一个版本没有执行成功
	public static final String CUR_VERSION_ISFAIL = "CUR_VERSION_ISFAIL";
	
	//没有找到相关的文件
	public static final String NO_FIND_FILE = "NO_FIND_FILE";
	
	//压缩包解压失败
	public static final String ZIP_FILE_FAIL = "ZIP_FILE_FAIL";
	
	
	
	//任务正在执行
	public static final String TASK_IS_RUN= "TASK_IS_RUN";
	
	public static final String TASK_IS_NOTRUN= "TASK_IS_NOTRUN";
	
	//任务已经执行成功
	public static final String TASK_IS_SUCCESS ="TASK_IS_SUCCESS";
}
