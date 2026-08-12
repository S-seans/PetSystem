package com.ruoyi.pet.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.health.domain.PetHealth;
import com.ruoyi.health.service.IPetHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.pet.mapper.PetMapper;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宠物信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-04
 */
@Service
public class PetServiceImpl implements IPetService 
{
    private static final Logger logger = LoggerFactory.getLogger(PetServiceImpl.class);

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private IPetHealthService petHealthService;


    /**
     * 查询宠物信息
     * 
     * @param petId 宠物信息主键
     * @return 宠物信息
     */
    @Override
    public Pet selectPetByPetId(Long petId)
    {
        return petMapper.selectPetByPetId(petId);
    }

    /**
     * 查询宠物信息列表
     * 
     * @param pet 宠物信息
     * @return 宠物信息
     */
    @Override
    public List<Pet> selectPetList(Pet pet)
    {
        return petMapper.selectPetList(pet);
    }

    /**
     * 新增宠物信息
     * 
     * @param pet 宠物信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertPet(Pet pet)
    {
        // 设置默认状态为"可领养"
        if (pet.getStatus() == null || pet.getStatus().trim().isEmpty()) {
            pet.setStatus("可领养");
        }

        pet.setCreateTime(DateUtils.getNowDate());
        int result = petMapper.insertPet(pet);

        // 获取新插入的宠物ID
        Long petId = pet.getPetId();

        // 创建对应的健康记录
        if (result > 0 && petId != null) {
            PetHealth petHealth = new PetHealth();
            petHealth.setPetId(petId);
            petHealth.setRecordDate(DateUtils.getNowDate()); // 设置记录日期为当前日期
            petHealth.setHealthStatus(""); // 默认健康状态
            petHealth.setIsSterilized(0); // 默认未绝育
            petHealth.setCreateBy(pet.getCreateBy());
            petHealth.setCreateTime(DateUtils.getNowDate());

            petHealthService.insertPetHealth(petHealth);
        }

        return result;
    }

    /**
     * 修改宠物信息
     * 
     * @param pet 宠物信息
     * @return 结果
     */
    @Override
    public int updatePet(Pet pet)
    {
        pet.setUpdateTime(DateUtils.getNowDate());
        return petMapper.updatePet(pet);
    }

    /**
     * 批量删除宠物信息
     * 
     * @param petIds 需要删除的宠物信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePetByPetIds(Long[] petIds)
    {
        // 先删除相关的健康记录
        petHealthService.deletePetHealthByPetIds(petIds);
        // 再删除宠物信息
        return petMapper.deletePetByPetIds(petIds);

    }

    /**
     * 更新宠物状态
     *
     * @param petId 宠物ID
     * @param status 目标状态
     */
    @Override
    public void updatePetStatus(Long petId, String status)
    {
        try
        {
            Pet pet = petMapper.selectPetByPetId(petId);
            if (pet != null)
            {
                pet.setStatus(status);
                petMapper.updatePet(pet);
            }
        }
        catch (Exception e)
        {
            logger.error("更新宠物状态失败，宠物ID: {}, 目标状态: {}", petId, status, e);
        }
    }

    /**
     * 按状态统计宠物数量
     *
     * @param status 状态
     * @return 数量
     */
    @Override
    public Long countPetByStatus(String status)
    {
        return petMapper.countPetByStatus(status);
    }

    /**
     * 查询公开宠物信息列表（仅展示字段）
     *
     * @param pet 宠物信息
     * @return 宠物信息集合
     */
    @Override
    public List<Pet> selectPublicPetList(Pet pet)
    {
        return petMapper.selectPublicPetList(pet);
    }

    /**
     * 查询公开宠物信息（仅展示字段）
     *
     * @param petId 宠物信息主键
     * @return 宠物信息
     */
    @Override
    public Pet selectPublicPetByPetId(Long petId)
    {
        return petMapper.selectPublicPetByPetId(petId);
    }
}
