package cn.nzxxx.predict.amms.jobcardbody.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Blob;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author LG
 * @since 2021-03-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AmmsJobCardbody implements Serializable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getCardbodyid() {
        return cardbodyid;
    }

    public void setCardbodyid(Integer cardbodyid) {
        this.cardbodyid = cardbodyid;
    }

    public Integer getCardid() {
        return cardid;
    }

    public void setCardid(Integer cardid) {
        this.cardid = cardid;
    }

    public Integer getVersionno() {
        return versionno;
    }

    public void setVersionno(Integer versionno) {
        this.versionno = versionno;
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

    public Integer getSortby() {
        return sortby;
    }

    public void setSortby(Integer sortby) {
        this.sortby = sortby;
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

    public String getBodyname() {
        return bodyname;
    }

    public void setBodyname(String bodyname) {
        this.bodyname = bodyname;
    }

    public String getEffectivity() {
        return effectivity;
    }

    public void setEffectivity(String effectivity) {
        this.effectivity = effectivity;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Blob getBodyimag() {
        return bodyimag;
    }

    public void setBodyimag(Blob bodyimag) {
        this.bodyimag = bodyimag;
    }

    public Integer getCardnodeid() {
        return cardnodeid;
    }

    public void setCardnodeid(Integer cardnodeid) {
        this.cardnodeid = cardnodeid;
    }

    public Integer getSortbydecimal() {
        return sortbydecimal;
    }

    public void setSortbydecimal(Integer sortbydecimal) {
        this.sortbydecimal = sortbydecimal;
    }

    public String getRefblock() {
        return refblock;
    }

    public void setRefblock(String refblock) {
        this.refblock = refblock;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getImagipcowner() {
        return imagipcowner;
    }

    public void setImagipcowner(String imagipcowner) {
        this.imagipcowner = imagipcowner;
    }

    public String getImagacreg() {
        return imagacreg;
    }

    public void setImagacreg(String imagacreg) {
        this.imagacreg = imagacreg;
    }

    @TableId(value = "CARDBODYID", type = IdType.AUTO)
    private Integer cardbodyid;

    @TableField("CARDID")
    private Integer cardid;

    @TableField("VERSIONNO")
    private Integer versionno;

    @TableField("UPDATEDWHEN")
    private LocalDateTime updatedwhen;

    @TableField("UPDATEDBY")
    private String updatedby;

    @TableField("CREATEDBY")
    private String createdby;

    @TableField("CREATEDWHEN")
    private LocalDateTime createdwhen;

    @TableField("SORTBY")
    private Integer sortby;

    /**
     * 1是删除
     */
    @TableField("DELETED")
    private Integer deleted;

    @TableField("BODYTYPE")
    private String bodytype;

    @TableField("BODYNAME")
    private String bodyname;

    @TableField("EFFECTIVITY")
    private String effectivity;

    @TableField("BODY")
    private String body;

    @TableField("SEQ")
    private Integer seq;

    @TableField("BODYIMAG")
    private Blob bodyimag;

    @TableField("CARDNODEID")
    private Integer cardnodeid;

    @TableField("SORTBYDECIMAL")
    private Integer sortbydecimal;

    @TableField("REFBLOCK")
    private String refblock;

    /**
     * 签署线
     */
    @TableField("SIGNATURE")
    private String signature;

    /**
     * 图片所属owner，用于区分适用性
     */
    @TableField("IMAGIPCOWNER")
    private String imagipcowner;

    /**
     * 存在图片适用性acreg
     */
    @TableField("IMAGACREG")
    private String imagacreg;


}
