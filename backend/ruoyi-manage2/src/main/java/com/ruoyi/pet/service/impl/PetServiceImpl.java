package com.ruoyi.pet.service.impl;

import java.util.List;
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
     * 删除宠物信息信息
     * 
     * @param petId 宠物信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePetByPetId(Long petId)
    {
        // 先删除相关的健康记录
        petHealthService.deletePetHealthByPetId(petId);
        // 再删除宠物信息
        return petMapper.deletePetByPetId(petId);
    }
}
