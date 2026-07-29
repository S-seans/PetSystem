package com.ruoyi.pet.mapper;

import java.util.List;
import com.ruoyi.pet.domain.Pet;

/**
 * 宠物信息Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-04
 */
public interface PetMapper 
{
    /**
     * 查询宠物信息
     * 
     * @param petId 宠物信息主键
     * @return 宠物信息
     */
    public Pet selectPetByPetId(Long petId);

    /**
     * 查询宠物信息列表
     * 
     * @param pet 宠物信息
     * @return 宠物信息集合
     */
    public List<Pet> selectPetList(Pet pet);

    /**
     * 新增宠物信息
     * 
     * @param pet 宠物信息
     * @return 结果
     */
    public int insertPet(Pet pet);

    /**
     * 修改宠物信息
     * 
     * @param pet 宠物信息
     * @return 结果
     */
    public int updatePet(Pet pet);

    /**
     * 删除宠物信息
     * 
     * @param petId 宠物信息主键
     * @return 结果
     */
    public int deletePetByPetId(Long petId);

    /**
     * 批量删除宠物信息
     * 
     * @param petIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePetByPetIds(Long[] petIds);
}
