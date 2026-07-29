package com.ruoyi.success.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.success.mapper.AdoptionSuccessMapper;
import com.ruoyi.success.domain.AdoptionSuccess;
import com.ruoyi.success.service.IAdoptionSuccessService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 领养成功记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Service
public class AdoptionSuccessServiceImpl implements IAdoptionSuccessService 
{
    @Autowired
    private AdoptionSuccessMapper adoptionSuccessMapper;

    @Autowired
    private IPetService petService;

    /**
     * 根据宠物ID查询领养成功记录
     */
    @Override
    public AdoptionSuccess selectAdoptionSuccessByPetId(Long petId) {
        AdoptionSuccess adoptionSuccess = new AdoptionSuccess();
        adoptionSuccess.setPetId(petId);
        List<AdoptionSuccess> list = adoptionSuccessMapper.selectAdoptionSuccessList(adoptionSuccess);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询领养成功记录
     *
     * @param successId 领养成功记录主键
     * @return 领养成功记录
     */
    @Override
    public AdoptionSuccess selectAdoptionSuccessBySuccessId(Long successId)
    {
        return adoptionSuccessMapper.selectAdoptionSuccessBySuccessId(successId);
    }

    /**
     * 查询领养成功记录列表
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 领养成功记录
     */
    @Override
    public List<AdoptionSuccess> selectAdoptionSuccessList(AdoptionSuccess adoptionSuccess)
    {
        return adoptionSuccessMapper.selectAdoptionSuccessList(adoptionSuccess);
    }

    /**
     * 新增领养成功记录
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 结果
     */
    @Override
    @Transactional
    public int insertAdoptionSuccess(AdoptionSuccess adoptionSuccess)
    {
        adoptionSuccess.setCreateTime(DateUtils.getNowDate());
        int result = adoptionSuccessMapper.insertAdoptionSuccess(adoptionSuccess);

        // 新增领养成功记录时，同步更新宠物状态为"已领养"
        if (result > 0 && adoptionSuccess.getPetId() != null) {
            updatePetStatus(adoptionSuccess.getPetId(), "已领养");
        }

        return result;
    }

    /**
     * 修改领养成功记录
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 结果
     */
    @Override
    public int updateAdoptionSuccess(AdoptionSuccess adoptionSuccess)
    {
        return adoptionSuccessMapper.updateAdoptionSuccess(adoptionSuccess);
    }

    /**
     * 批量删除领养成功记录
     * 
     * @param successIds 需要删除的领养成功记录主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAdoptionSuccessBySuccessIds(Long[] successIds)
    {
        // 删除前先获取记录信息，用于更新宠物状态
        for (Long successId : successIds) {
            AdoptionSuccess adoptionSuccess = adoptionSuccessMapper.selectAdoptionSuccessBySuccessId(successId);
            if (adoptionSuccess != null && adoptionSuccess.getPetId() != null) {
                // 删除领养成功记录时，将宠物状态改回"可领养"
                updatePetStatus(adoptionSuccess.getPetId(), "可领养");
            }
        }

        return adoptionSuccessMapper.deleteAdoptionSuccessBySuccessIds(successIds);
    }

    /**
     * 删除领养成功记录信息
     * 
     * @param successId 领养成功记录主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAdoptionSuccessBySuccessId(Long successId)
    {
        // 删除前先获取记录信息，用于更新宠物状态
        AdoptionSuccess adoptionSuccess = adoptionSuccessMapper.selectAdoptionSuccessBySuccessId(successId);
        if (adoptionSuccess != null && adoptionSuccess.getPetId() != null) {
            // 删除领养成功记录时，将宠物状态改回"可领养"
            updatePetStatus(adoptionSuccess.getPetId(), "可领养");
        }

        return adoptionSuccessMapper.deleteAdoptionSuccessBySuccessId(successId);
    }

    /**
     * 更新宠物状态
     */
    private void updatePetStatus(Long petId, String status) {
        try {
            Pet pet = petService.selectPetByPetId(petId);
            if (pet != null) {
                pet.setStatus(status);
                petService.updatePet(pet);
            }
        } catch (Exception e) {
            // 记录日志但不中断主流程
            System.err.println("更新宠物状态失败，宠物ID: " + petId + ", 目标状态: " + status);
            e.printStackTrace();
        }
    }
}
