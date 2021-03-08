package cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 工卡主信息
 * </p>
 *
 * @author LG
 * @since 2021-03-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JobCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "CARDID", type = IdType.AUTO)
    private Integer cardid;

    /**
     * CRJ\BOEING\AIRBUS
     */
    @TableField("CARDSOURCE")
    private String cardsource;

    /**
     * 依据
     */
    @TableField("REFERENCE")
    private String reference;

    /**
     * 作废
     */
    @TableField("IPCOWNER")
    private String ipcowner;

    /**
     * 数据版本号,数据只读显示  Hidden
     */
    @TableField("VERSIONNO")
    private Integer versionno;

    /**
     * 更新时间  Hidden
     */
    @TableField("UPDATEDWHEN")
    private LocalDateTime updatedwhen;

    /**
     * 更新人  Hidden
     */
    @TableField("UPDATEDBY")
    private String updatedby;

    /**
     * 创建人  Hidden
     */
    @TableField("CREATEDBY")
    private String createdby;

    /**
     * 创建时间  Hidden
     */
    @TableField("CREATEDWHEN")
    private LocalDateTime createdwhen;

    /**
     * 排序   Hidden
     */
    @TableField("SORTBY")
    private Integer sortby;

    /**
     * 逻辑删除   Hidden
     */
    @TableField("DELETED")
    private Integer deleted;

    /**
     * 状态
     */
    @TableField("STATE")
    private String state;

    /**
     * 手册ID
     */
    @TableField("AMMID")
    private Integer ammid;

    /**
     * 作废
     */
    @TableField("ADMARK")
    private String admark;

    /**
     * 作废
     */
    @TableField("ETOPSMARK")
    private String etopsmark;

    /**
     * 作废
     */
    @TableField("CPCPMARK")
    private String cpcpmark;

    /**
     * 作废
     */
    @TableField("DIMARK")
    private String dimark;

    /**
     * 作废
     */
    @TableField("MAJORMODMARK")
    private String majormodmark;

    /**
     * english 标题  目前只用英文标题
     */
    @TableField("TITLEEN")
    private String titleen;

    /**
     * 中文     标题
     */
    @TableField("TITLECH")
    private String titlech;

    /**
     * 修订日期 原文件
     */
    @TableField("REVDATE")
    private String revdate;

    /**
     * 版本 原文件
     */
    @TableField("REVISON")
    private String revison;

    /**
     * 工卡号
     */
    @TableField("JOBCARDNO")
    private String jobcardno;

    /**
     * 修订日期
     */
    @TableField("JCREVDATE")
    private LocalDateTime jcrevdate;

    /**
     * 版本
     */
    @TableField("JCREVISON")
    private String jcrevison;

    /**
     * 机型
     */
    @TableField("ACTYPE")
    private String actype;

    /**
     * 编写人
     */
    @TableField("WRITTEN")
    private String written;

    /**
     * 编写日期
     */
    @TableField("WRITTENDATE")
    private LocalDateTime writtendate;

    /**
     * 批准人
     */
    @TableField("APPROVED")
    private String approved;

    /**
     * 批准日期
     */
    @TableField("APPROVEDATE")
    private LocalDateTime approvedate;

    /**
     * 审核人
     */
    @TableField("REVIEWED")
    private String reviewed;

    /**
     * 审核日期
     */
    @TableField("REVIEWEDATE")
    private LocalDateTime reviewedate;

    /**
     * 生效日期
     */
    @TableField("EFFECTIVELYDATE")
    private LocalDateTime effectivelydate;

    /**
     * 适用性
     */
    @TableField("EFFRG")
    private String effrg;

    /**
     * 工卡改版类型 ( N：新工卡， T：工具变化，M：航材变化；O：其他变化)
     */
    @TableField("CHANGETYPE")
    private String changetype;

    /**
     * 是否需要反馈
     */
    @TableField("FEEDBACK")
    private String feedback;

    /**
     * 反馈描述
     */
    @TableField("FEEDBACKDESC")
    private String feedbackdesc;

    /**
     * 编写部门
     */
    @TableField("DEPT")
    private String dept;

    /**
     * 人工改版时，写明来自哪个CARDID
     */
    @TableField("FROMCARDID")
    private Integer fromcardid;

    /**
     * 工卡编辑类型：WORD、WEB
     */
    @TableField("CARDTYPE")
    private String cardtype;

    /**
     * 改版原因
     */
    @TableField("REVISONOTE")
    private String revisonote;

    /**
     * 对应AMS的reference
     */
    @TableField("AMSREFERENCE")
    private String amsreference;

    /**
     * 作废
     */
    @TableField("JOBCARDIFID")
    private Integer jobcardifid;

    /**
     * 必检
     */
    @TableField("CARDCHECK")
    private String cardcheck;

    /**
     * 工时
     */
    @TableField("MHS")
    private String mhs;

    /**
     * 区域
     */
    @TableField("ZONE")
    private String zone;

    /**
     * 接近
     */
    @TableField("ACCESS")
    private String access;

    /**
     * 任务种类
     */
    @TableField("TASKTYPE")
    private String tasktype;

    /**
     * 专业划分
     */
    @TableField("SKILL")
    private String skill;

    /**
     * 适用性
     */
    @TableField("APPL")
    private String appl;

    /**
     * word路径
     */
    @TableField("WORDPATH")
    private String wordpath;

    /**
     * 原始数据id即crj_card,boeing_card,amms_job_card的主键
     */
    @TableField("INITDATAID")
    private String initdataid;

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

    public String getRevdate() {
        return revdate;
    }

    public void setRevdate(String revdate) {
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

    public Integer getFromcardid() {
        return fromcardid;
    }

    public void setFromcardid(Integer fromcardid) {
        this.fromcardid = fromcardid;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getRevisonote() {
        return revisonote;
    }

    public void setRevisonote(String revisonote) {
        this.revisonote = revisonote;
    }

    public String getAmsreference() {
        return amsreference;
    }

    public void setAmsreference(String amsreference) {
        this.amsreference = amsreference;
    }

    public Integer getJobcardifid() {
        return jobcardifid;
    }

    public void setJobcardifid(Integer jobcardifid) {
        this.jobcardifid = jobcardifid;
    }

    public String getCardcheck() {
        return cardcheck;
    }

    public void setCardcheck(String cardcheck) {
        this.cardcheck = cardcheck;
    }

    public String getMhs() {
        return mhs;
    }

    public void setMhs(String mhs) {
        this.mhs = mhs;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getTasktype() {
        return tasktype;
    }

    public void setTasktype(String tasktype) {
        this.tasktype = tasktype;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getAppl() {
        return appl;
    }

    public void setAppl(String appl) {
        this.appl = appl;
    }

    public String getWordpath() {
        return wordpath;
    }

    public void setWordpath(String wordpath) {
        this.wordpath = wordpath;
    }

    public String getInitdataid() {
        return initdataid;
    }

    public void setInitdataid(String initdataid) {
        this.initdataid = initdataid;
    }
}
