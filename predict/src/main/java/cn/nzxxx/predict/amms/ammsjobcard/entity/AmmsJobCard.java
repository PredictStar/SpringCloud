package cn.nzxxx.predict.amms.ammsjobcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class AmmsJobCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "CARDID", type = IdType.AUTO)
    private Integer cardid;

    @TableField("CARDSOURCE")
    private String cardsource;

    @TableField("REFERENCE")
    private String reference;

    @TableField("IPCOWNER")
    private String ipcowner;

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

    @TableField("DELETED")
    private Integer deleted;

    @TableField("STATE")
    private String state;

    @TableField("AMMID")
    private Integer ammid;

    @TableField("ADMARK")
    private String admark;

    @TableField("ETOPSMARK")
    private String etopsmark;

    @TableField("CPCPMARK")
    private String cpcpmark;

    @TableField("DIMARK")
    private String dimark;

    @TableField("MAJORMODMARK")
    private String majormodmark;

    @TableField("TITLEEN")
    private String titleen;

    @TableField("TITLECH")
    private String titlech;

    @TableField("REVDATE")
    private Integer revdate;

    @TableField("REVISON")
    private String revison;

    @TableField("JOBCARDNO")
    private String jobcardno;

    @TableField("JCREVDATE")
    private LocalDateTime jcrevdate;

    @TableField("JCREVISON")
    private String jcrevison;

    @TableField("ACTYPE")
    private String actype;

    @TableField("WRITTEN")
    private String written;

    @TableField("WRITTENDATE")
    private LocalDateTime writtendate;

    @TableField("APPROVED")
    private String approved;

    @TableField("APPROVEDATE")
    private LocalDateTime approvedate;

    @TableField("REVIEWED")
    private String reviewed;

    @TableField("REVIEWEDATE")
    private LocalDateTime reviewedate;

    @TableField("EFFECTIVELYDATE")
    private LocalDateTime effectivelydate;

    @TableField("EFFRG")
    private String effrg;

    @TableField("CHANGETYPE")
    private String changetype;

    @TableField("FEEDBACK")
    private String feedback;

    @TableField("FEEDBACKDESC")
    private String feedbackdesc;

    @TableField("DEPT")
    private String dept;

    @TableField("REFERENCEMATCH")
    private String referencematch;

    @TableField("REVISONOTE")
    private String revisonote;

    @TableField("CARDTYPE")
    private String cardtype;

    @TableField("FROMCARDID")
    private Integer fromcardid;

    /**
     * 所对应word,生成地址
     */
    @TableField("WORDPATH")
    private String wordpath;

    /**
     * 错误记录
     */
    @TableField("ERRORRECORD")
    private String errorrecord;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getCardid() {
        return cardid;
    }

    public void setCardid(Integer cardid) {
        this.cardid = cardid;
    }

    public String getCardsource() {
        return cardsource;
    }

    public void setCardsource(String cardsource) {
        this.cardsource = cardsource;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getIpcowner() {
        return ipcowner;
    }

    public void setIpcowner(String ipcowner) {
        this.ipcowner = ipcowner;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getAmmid() {
        return ammid;
    }

    public void setAmmid(Integer ammid) {
        this.ammid = ammid;
    }

    public String getAdmark() {
        return admark;
    }

    public void setAdmark(String admark) {
        this.admark = admark;
    }

    public String getEtopsmark() {
        return etopsmark;
    }

    public void setEtopsmark(String etopsmark) {
        this.etopsmark = etopsmark;
    }

    public String getCpcpmark() {
        return cpcpmark;
    }

    public void setCpcpmark(String cpcpmark) {
        this.cpcpmark = cpcpmark;
    }

    public String getDimark() {
        return dimark;
    }

    public void setDimark(String dimark) {
        this.dimark = dimark;
    }

    public String getMajormodmark() {
        return majormodmark;
    }

    public void setMajormodmark(String majormodmark) {
        this.majormodmark = majormodmark;
    }

    public String getTitleen() {
        return titleen;
    }

    public void setTitleen(String titleen) {
        this.titleen = titleen;
    }

    public String getTitlech() {
        return titlech;
    }

    public void setTitlech(String titlech) {
        this.titlech = titlech;
    }

    public Integer getRevdate() {
        return revdate;
    }

    public void setRevdate(Integer revdate) {
        this.revdate = revdate;
    }

    public String getRevison() {
        return revison;
    }

    public void setRevison(String revison) {
        this.revison = revison;
    }

    public String getJobcardno() {
        return jobcardno;
    }

    public void setJobcardno(String jobcardno) {
        this.jobcardno = jobcardno;
    }

    public LocalDateTime getJcrevdate() {
        return jcrevdate;
    }

    public void setJcrevdate(LocalDateTime jcrevdate) {
        this.jcrevdate = jcrevdate;
    }

    public String getJcrevison() {
        return jcrevison;
    }

    public void setJcrevison(String jcrevison) {
        this.jcrevison = jcrevison;
    }

    public String getActype() {
        return actype;
    }

    public void setActype(String actype) {
        this.actype = actype;
    }

    public String getWritten() {
        return written;
    }

    public void setWritten(String written) {
        this.written = written;
    }

    public LocalDateTime getWrittendate() {
        return writtendate;
    }

    public void setWrittendate(LocalDateTime writtendate) {
        this.writtendate = writtendate;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public LocalDateTime getApprovedate() {
        return approvedate;
    }

    public void setApprovedate(LocalDateTime approvedate) {
        this.approvedate = approvedate;
    }

    public String getReviewed() {
        return reviewed;
    }

    public void setReviewed(String reviewed) {
        this.reviewed = reviewed;
    }

    public LocalDateTime getReviewedate() {
        return reviewedate;
    }

    public void setReviewedate(LocalDateTime reviewedate) {
        this.reviewedate = reviewedate;
    }

    public LocalDateTime getEffectivelydate() {
        return effectivelydate;
    }

    public void setEffectivelydate(LocalDateTime effectivelydate) {
        this.effectivelydate = effectivelydate;
    }

    public String getEffrg() {
        return effrg;
    }

    public void setEffrg(String effrg) {
        this.effrg = effrg;
    }

    public String getChangetype() {
        return changetype;
    }

    public void setChangetype(String changetype) {
        this.changetype = changetype;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getFeedbackdesc() {
        return feedbackdesc;
    }

    public void setFeedbackdesc(String feedbackdesc) {
        this.feedbackdesc = feedbackdesc;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getReferencematch() {
        return referencematch;
    }

    public void setReferencematch(String referencematch) {
        this.referencematch = referencematch;
    }

    public String getRevisonote() {
        return revisonote;
    }

    public void setRevisonote(String revisonote) {
        this.revisonote = revisonote;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public Integer getFromcardid() {
        return fromcardid;
    }

    public void setFromcardid(Integer fromcardid) {
        this.fromcardid = fromcardid;
    }

    public String getWordpath() {
        return wordpath;
    }

    public void setWordpath(String wordpath) {
        this.wordpath = wordpath;
    }

    public String getErrorrecord() {
        return errorrecord;
    }

    public void setErrorrecord(String errorrecord) {
        this.errorrecord = errorrecord;
    }
}
