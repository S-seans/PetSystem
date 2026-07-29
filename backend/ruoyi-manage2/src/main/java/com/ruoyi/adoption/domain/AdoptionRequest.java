package com.ruoyi.adoption.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领养申请对象 tb_adoption_request
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public class AdoptionRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long requestId;

    /** 宠物ID */
    @Excel(name = "宠物ID")
    private Long petId;

    /** 申请人ID */
    @Excel(name = "申请人ID")
    private Long userId;

    /** 领养理由 */
    @Excel(name = "领养理由")
    private String reason;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 审核备注 */
    @Excel(name = "审核备注")
    private String reviewRemark;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reviewTime;

    /** 审核人 */
    @Excel(name = "审核人")
    private String reviewBy;

    public void setRequestId(Long requestId) 
    {
        this.requestId = requestId;
    }

    public Long getRequestId() 
    {
        return requestId;
    }

    public void setPetId(Long petId) 
    {
        this.petId = petId;
    }

    public Long getPetId() 
    {
        return petId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setReason(String reason) 
    {
        this.reason = reason;
    }

    public String getReason() 
    {
        return reason;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setReviewRemark(String reviewRemark) 
    {
        this.reviewRemark = reviewRemark;
    }

    public String getReviewRemark() 
    {
        return reviewRemark;
    }

    public void setReviewTime(Date reviewTime) 
    {
        this.reviewTime = reviewTime;
    }

    public Date getReviewTime() 
    {
        return reviewTime;
    }

    public void setReviewBy(String reviewBy) 
    {
        this.reviewBy = reviewBy;
    }

    public String getReviewBy() 
    {
        return reviewBy;
    }

    /** 宠物名称 */
    @Excel(name = "宠物名称")
    private String petName;

    public void setPetName(String petName)
    {
        this.petName = petName;
    }

    public String getPetName()
    {
        return petName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("requestId", getRequestId())
            .append("petId", getPetId())
            .append("userId", getUserId())
            .append("reason", getReason())
            .append("status", getStatus())
            .append("reviewRemark", getReviewRemark())
            .append("reviewTime", getReviewTime())
            .append("reviewBy", getReviewBy())
            .append("createTime", getCreateTime())
                .append("petName", getPetName())
            .toString();
    }
}
