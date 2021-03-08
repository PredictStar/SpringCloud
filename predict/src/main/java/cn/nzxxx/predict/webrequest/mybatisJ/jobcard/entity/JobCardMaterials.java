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
public class JobCardMaterials implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "JOB_CARD_MATERIALS_ID", type = IdType.AUTO)
    private Integer jobCardMaterialsId;

    /**
     * 从键
     */
    @TableField("JOB_CARD_ID")
    private Integer jobCardId;

    /**
     * 即REFERENCE
     */
    @TableField("PN")
    private String pn;

    /**
     * 描述
     */
    @TableField("DESCR")
    private String descr;

    /**
     * 数量
     */
    @TableField("QTY")
    private String qty;

    /**
     * 备注
     */
    @TableField("NOTE")
    private String note;

    /**
     * 视情(扩展字段)
     */
    @TableField("AS_APPROPRIATE")
    private String asAppropriate;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getJobCardMaterialsId() {
        return jobCardMaterialsId;
    }

    public void setJobCardMaterialsId(Integer jobCardMaterialsId) {
        this.jobCardMaterialsId = jobCardMaterialsId;
    }

    public Integer getJobCardId() {
        return jobCardId;
    }

    public void setJobCardId(Integer jobCardId) {
        this.jobCardId = jobCardId;
    }

    public String getPn() {
        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAsAppropriate() {
        return asAppropriate;
    }

    public void setAsAppropriate(String asAppropriate) {
        this.asAppropriate = asAppropriate;
    }
}
