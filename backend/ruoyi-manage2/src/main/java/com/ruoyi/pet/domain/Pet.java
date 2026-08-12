package com.ruoyi.pet.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 宠物信息对象 tb_pet
 * 
 * @author ruoyi
 * @date 2025-11-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Pet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 宠物ID */
    private Long petId;

    /** 宠物名称 */
    @Excel(name = "宠物名称")
    private String name;

    /** 品种 */
    @Excel(name = "品种")
    private String breed;

    /** 年龄（月） */
    @Excel(name = "年龄", readConverterExp = "月=")
    private Long age;

    /** 年龄下限（月，查询用） */
    private Long ageMin;

    /** 年龄上限（月，查询用） */
    private Long ageMax;

    /** 性别 */
    @Excel(name = "性别")
    private String gender;

    /** 体重kg */
    @Excel(name = "体重kg")
    private BigDecimal weight;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 照片 */
    @Excel(name = "照片")
    private String imageUrl;

    /** 状况描述 */
    @Excel(name = "状况描述")
    private String description;

    /** 救助日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "救助日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date rescueDate;
}
