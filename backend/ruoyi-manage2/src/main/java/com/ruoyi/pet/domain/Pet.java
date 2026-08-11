package com.ruoyi.pet.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 宠物信息对象 tb_pet
 * 
 * @author ruoyi
 * @date 2025-11-04
 */
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

    public void setPetId(Long petId) 
    {
        this.petId = petId;
    }

    public Long getPetId() 
    {
        return petId;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setBreed(String breed) 
    {
        this.breed = breed;
    }

    public String getBreed() 
    {
        return breed;
    }

    public void setAge(Long age) 
    {
        this.age = age;
    }

    public Long getAge() 
    {
        return age;
    }

    public void setAgeMin(Long ageMin) 
    {
        this.ageMin = ageMin;
    }

    public Long getAgeMin() 
    {
        return ageMin;
    }

    public void setAgeMax(Long ageMax) 
    {
        this.ageMax = ageMax;
    }

    public Long getAgeMax() 
    {
        return ageMax;
    }

    public void setGender(String gender) 
    {
        this.gender = gender;
    }

    public String getGender() 
    {
        return gender;
    }

    public void setWeight(BigDecimal weight) 
    {
        this.weight = weight;
    }

    public BigDecimal getWeight() 
    {
        return weight;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setRescueDate(Date rescueDate) 
    {
        this.rescueDate = rescueDate;
    }

    public Date getRescueDate() 
    {
        return rescueDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("petId", getPetId())
            .append("name", getName())
            .append("breed", getBreed())
            .append("age", getAge())
            .append("gender", getGender())
            .append("weight", getWeight())
            .append("status", getStatus())
            .append("imageUrl", getImageUrl())
            .append("description", getDescription())
            .append("rescueDate", getRescueDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
