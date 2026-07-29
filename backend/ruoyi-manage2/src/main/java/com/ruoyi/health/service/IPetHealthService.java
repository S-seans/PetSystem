package com.ruoyi.health.service;

import java.util.List;
import com.ruoyi.health.domain.PetHealth;

/**
 * 宠物健康记录Service接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface IPetHealthService 
{
    /**
     * 查询宠物健康记录
     * 
     * @param healthId 宠物健康记录主键
     * @return 宠物健康记录
     */
    public PetHealth selectPetHealthByHealthId(Long healthId);

    /**
     * 查询宠物健康记录列表
     * 
     * @param petHealth 宠物健康记录
     * @return 宠物健康记录集合
     */
    public List<PetHealth> selectPetHealthList(PetHealth petHealth);

    /**
     * 新增宠物健康记录
     * 
     * @param petHealth 宠物健康记录
     * @return 结果
     */
    public int insertPetHealth(PetHealth petHealth);

    /**
     * 修改宠物健康记录
     * 
     * @param petHealth 宠物健康记录
     * @return 结果
     */
    public int updatePetHealth(PetHealth petHealth);

    /**
     * 批量删除宠物健康记录
     * 
     * @param healthIds 需要删除的宠物健康记录主键集合
     * @return 结果
     */
    public int deletePetHealthByHealthIds(Long[] healthIds);

    /**
     * 删除宠物健康记录信息
     * 
     * @param healthId 宠物健康记录主键
     * @return 结果
     */
    public int deletePetHealthByHealthId(Long healthId);

    /**
     * 根据宠物ID删除健康记录
     *
     * @param petId 宠物ID
     * @return 结果
     */
    public int deletePetHealthByPetId(Long petId);

    /**
     * 根据宠物ID批量删除健康记录
     *
     * @param petIds 宠物ID数组
     * @return 结果
     */
    public int deletePetHealthByPetIds(Long[] petIds);
}
