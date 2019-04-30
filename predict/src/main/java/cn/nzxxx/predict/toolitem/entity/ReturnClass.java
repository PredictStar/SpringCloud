package cn.nzxxx.predict.toolitem.entity;

public class ReturnClass {
	
	/**
	 * 状态码
	 * 200-正常;300-正常错误返回;400-跳转至首页;500-异常错误;510-不可能异常错误
	 * 210-冲突
	 * 0  -受影响数据为0行
	 */
    private String statusCode;
    /**
     * 状态描述
     */
    private String stateDescribe;
    /**
     * 值描述
     */
    private Object valueDescribe;
    
    
    
    

	public Object getValueDescribe() {
		return valueDescribe;
	}

	public void setValueDescribe(Object valueDescribe) {
		this.valueDescribe = valueDescribe;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStateDescribe() {
		return stateDescribe;
	}

	public void setStateDescribe(String describe) {
		this.stateDescribe = describe;
	}
    
    
    
    
}