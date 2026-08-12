package com.ruoyi.health.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.health.mapper.PetHealthMapper;
import com.ruoyi.health.domain.PetHealth;
import com.ruoyi.health.service.IPetHealthService;

/**
 * 宠物健康记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Service
public class PetHealthServiceImpl implements IPetHealthService 
{
    @Autowired
    private PetHealthMapper petHealthMapper;

    /**
     * 查询宠物健康记录
     * 
     * @param healthId 宠物健康记录主键
     * @return 宠物健康记录
     */
    @Override
    public PetHealth selectPetHealthByHealthId(Long healthId)
    {
        return petHealthMapper.selectPetHealthByHealthId(healthId);
    }

    /**
     * 查询宠物健康记录列表
     * 
     * @param petHealth 宠物健康记录
     * @return 宠物健康记录
     */
    @Override
    public List<PetHealth> selectPetHealthList(PetHealth petHealth)
    {
        return petHealthMapper.selectPetHealthList(petHealth);
    }

    /**
     * 新增宠物健康记录
     * 
     * @param petHealth 宠物健康记录
     * @return 结果
     */
    @Override
    public int insertPetHealth(PetHealth petHealth)
    {
        petHealth.setCreateTime(DateUtils.getNowDate());
        return petHealthMapper.insertPetHealth(petHealth);
    }

    /**
     * 修改宠物健康记录
     * 
     * @param petHealth 宠物健康记录
     * @return 结果
     */
    @Override
    public int updatePetHealth(PetHealth petHealth)
    {
        return petHealthMapper.updatePetHealth(petHealth);
    }

    /**
     * 批量删除宠物健康记录
     * 
     * @param healthIds 需要删除的宠物健康记录主键
     * @return 结果
     */
    @Override
    public int deletePetHealthByHealthIds(Long[] healthIds)
    {
        return petHealthMapper.deletePetHealthByHealthIds(healthIds);
    }

    /**
     * 根据宠物ID批量删除健康记录
     *
     * @param petIds 宠物ID数组
     * @return 结果
     */
    @Override
    public int deletePetHealthByPetIds(Long[] petIds)
    {
        return petHealthMapper.deletePetHealthByPetIds(petIds);
    }
}
