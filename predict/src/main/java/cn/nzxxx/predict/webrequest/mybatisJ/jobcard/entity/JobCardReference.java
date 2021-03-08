package cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author LG
 * @since 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JobCardReference implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "JOB_CARD_REFERENCE_ID", type = IdType.AUTO)
    private Integer jobCardReferenceId;

    /**
     * 从键
     */
    @TableField("JOB_CARD_ID")
    private Integer jobCardId;

    @TableField("REFERENCE")
    private String reference;

    /**
     * 也可称为DESIGNATION
     */
    @TableField("TITLE")
    private String title;

    /**
     * 手册号-显示时先隐藏掉
     */
    @TableField("MANUAL_NO")
    private String manualNo;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getJobCardReferenceId() {
        return jobCardReferenceId;
    }

    public void setJobCardReferenceId(Integer jobCardReferenceId) {
        this.jobCardReferenceId = jobCardReferenceId;
    }

    public Integer getJobCardId() {
        return jobCardId;
    }

    public void setJobCardId(Integer jobCardId) {
        this.jobCardId = jobCardId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getManualNo() {
        return manualNo;
    }

    public void setManualNo(String manualNo) {
        this.manualNo = manualNo;
    }
}
