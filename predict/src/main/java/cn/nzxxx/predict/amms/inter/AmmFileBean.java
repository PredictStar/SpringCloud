package cn.nzxxx.predict.amms.inter;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.Date;


public class AmmFileBean {
	private Date  revdate;
	// 生效日期
	private Date effectdate;

	// 日期
	private Date cdate;

	private String classtype;

	// 版本号
	private String rev;

	// 名称
	private String name;

	// 机型
	private String actype;

	// 时间戳,作为主键存在
	private Long id;

	// 文件路径
	private String path;



	// 导入的类型
	private String type;

	private String result;

	private String owner;

	private String fromrev;

	private Long fromid;

	private String frompath;

	public Date getRevdate() {
		return revdate;
	}

	public void setRevdate(Date revdate) {
		this.revdate = revdate;
	}

	public String getClasstype() {
		return classtype;
	}

	public void setClasstype(String classtype) {
		this.classtype = classtype;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActype() {
		return actype;
	}

	public void setActype(String actype) {
		this.actype = actype;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getEffectdate() {
		return effectdate;
	}

	public void setEffectdate(Date effectdate) {
		this.effectdate = effectdate;
	}

	public Date getCdate() {
		return cdate;
	}

	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getFromrev() {
		return fromrev;
	}

	public void setFromrev(String fromrev) {
		this.fromrev = fromrev;
	}

	public Long getFromid() {
		return fromid;
	}

	public void setFromid(Long fromid) {
		this.fromid = fromid;
	}

	public String getFrompath() {
		return frompath;
	}

	public void setFrompath(String frompath) {
		this.frompath = frompath;
	}
}