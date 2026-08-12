package com.ruoyi.success.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领养成功记录对象 tb_adoption_success
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}
