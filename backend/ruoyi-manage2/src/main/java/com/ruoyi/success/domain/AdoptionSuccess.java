package com.ruoyi.success.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领养成功记录对象 tb_adoption_success
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public class AdoptionSuccess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long successId;

    /** 申请ID */
    @Excel(name = "申请ID")
    private Long requestId;

    /** 宠物ID */
    @Excel(name = "宠物ID")
    private Long petId;

    /** 领养人ID */
    @Excel(name = "领养人ID")
    private Long userId;

    /** 领养日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "领养日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date adoptTime;

    /** 回访日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "回访日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date followUpDate;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public void setSuccessId(Long successId) 
    {
        this.successId = successId;
    }

    public Long getSuccessId() 
    {
        return successId;
    }

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

    public void setAdoptTime(Date adoptTime) 
    {
        this.adoptTime = adoptTime;
    }

    public Date getAdoptTime() 
    {
        return adoptTime;
    }

    public void setFollowUpDate(Date followUpDate) 
    {
        this.followUpDate = followUpDate;
    }

    public Date getFollowUpDate() 
    {
        return followUpDate;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("successId", getSuccessId())
            .append("requestId", getRequestId())
            .append("petId", getPetId())
            .append("userId", getUserId())
            .append("adoptTime", getAdoptTime())
            .append("followUpDate", getFollowUpDate())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
