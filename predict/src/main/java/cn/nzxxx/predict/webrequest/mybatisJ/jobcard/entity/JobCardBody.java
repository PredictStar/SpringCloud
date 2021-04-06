package cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author LG
 * @since 2021-03-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JobCardBody implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "JCBODYID", type = IdType.AUTO)
    private Integer jcbodyid;

    /**
     * 从键
     */
    @TableField("JOBCARDID")
    private Integer jobcardid;

    @TableField("UPDATEDWHEN")
    private LocalDateTime updatedwhen;

    @TableField("UPDATEDBY")
    private String updatedby;

    @TableField("CREATEDBY")
    private String createdby;

    @TableField("CREATEDWHEN")
    private LocalDateTime createdwhen;

    /**
     * 排序
     */
    @TableField("ORDERBY")
    private Double orderby;

    /**
     * 删除标记(1是删,默认0)
     */
    @TableField("DELETED")
    private Integer deleted;

    /**
     * 类型(txt;image;table)
     */
    @TableField("BODYTYPE")
    private String bodytype;

    /**
     * 原始内容(文本:<p>x</p>;图片:<image><p>标题</p>)
     */
    @TableField("BODYVAL")
    private String bodyval;

    /**
     * 匹配度(文本才有此)
     */
    @TableField("MATCHN")
    private Double matchn;

    /**
     * 内容翻译结果(文本才有此)
     */
    @TableField("TRANSLATERES")
    private String translateres;

    /**
     * 签署线
     */
    @TableField("SIGNATURE")
    private String signature;

    /**
     * 图片标题(没啥用,在图片的内容里有)
     */
    @TableField("IMAGENAME")
    private String imagename;

    @TableField("IDI")
    private String idi;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getJcbodyid() {
        return jcbodyid;
    }

    public void setJcbodyid(Integer jcbodyid) {
        this.jcbodyid = jcbodyid;
    }

    public Integer getJobcardid() {
        return jobcardid;
    }

    public void setJobcardid(Integer jobcardid) {
        this.jobcardid = jobcardid;
    }

    public LocalDateTime getUpdatedwhen() {
        return updatedwhen;
    }

    public void setUpdatedwhen(LocalDateTime updatedwhen) {
        this.updatedwhen = updatedwhen;
    }

    public String getUpdatedby() {
        return updatedby;
    }

    public void setUpdatedby(String updatedby) {
        this.updatedby = updatedby;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public LocalDateTime getCreatedwhen() {
        return createdwhen;
    }

    public void setCreatedwhen(LocalDateTime createdwhen) {
        this.createdwhen = createdwhen;
    }

    public Double getOrderby() {
        return orderby;
    }

    public void setOrderby(Double orderby) {
        this.orderby = orderby;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getBodytype() {
        return bodytype;
    }

    public void setBodytype(String bodytype) {
        this.bodytype = bodytype;
    }

    public String getBodyval() {
        return bodyval;
    }

    public void setBodyval(String bodyval) {
        this.bodyval = bodyval;
    }

    public Double getMatchn() {
        return matchn;
    }

    public void setMatchn(Double matchn) {
        this.matchn = matchn;
    }

    public String getTranslateres() {
        return translateres;
    }

    public void setTranslateres(String translateres) {
        this.translateres = translateres;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getIdi() {
        return idi;
    }

    public void setIdi(String idi) {
        this.idi = idi;
    }
}
