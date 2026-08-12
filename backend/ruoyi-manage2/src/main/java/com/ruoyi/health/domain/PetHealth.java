package com.ruoyi.health.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 宠物健康记录对象 tb_pet_health
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetHealth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 健康记录ID */
    private Long healthId;

    /** 宠物ID */
    @Excel(name = "宠物ID")
    private Long petId;

    private String petName;

    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "记录日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 疫苗名称 */
    @Excel(name = "疫苗名称")
    private String vaccineName;

    /** 是否绝育 */
    @Excel(name = "是否绝育")
    private Integer isSterilized;

    /** 状态 */
    @Excel(name = "状态")
    private String healthStatus;

    /** 详细描述 */
    @Excel(name = "详细描述")
    private String description;


}
