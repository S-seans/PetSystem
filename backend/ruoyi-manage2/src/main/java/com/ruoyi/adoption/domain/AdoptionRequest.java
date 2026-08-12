package com.ruoyi.adoption.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领养申请对象 tb_adoption_request
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
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

    /** 宠物名称 */
    @Excel(name = "宠物名称")
    private String petName;
}
