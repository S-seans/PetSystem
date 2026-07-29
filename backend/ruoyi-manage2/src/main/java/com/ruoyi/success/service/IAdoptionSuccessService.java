package com.ruoyi.success.service;

import java.util.List;
import com.ruoyi.success.domain.AdoptionSuccess;

/**
 * 领养成功记录Service接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface IAdoptionSuccessService 
{

    /**
     * 根据宠物ID查询领养成功记录
     *
     * @param petId 宠物ID
     * @return 领养成功记录
     */
    public AdoptionSuccess selectAdoptionSuccessByPetId(Long petId);

    /**
     * 查询领养成功记录
     * 
     * @param successId 领养成功记录主键
     * @return 领养成功记录
     */
    public AdoptionSuccess selectAdoptionSuccessBySuccessId(Long successId);

    /**
     * 查询领养成功记录列表
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 领养成功记录集合
     */
    public List<AdoptionSuccess> selectAdoptionSuccessList(AdoptionSuccess adoptionSuccess);

    /**
     * 新增领养成功记录
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 结果
     */
    public int insertAdoptionSuccess(AdoptionSuccess adoptionSuccess);

    /**
     * 修改领养成功记录
     * 
     * @param adoptionSuccess 领养成功记录
     * @return 结果
     */
    public int updateAdoptionSuccess(AdoptionSuccess adoptionSuccess);

    /**
     * 批量删除领养成功记录
     * 
     * @param successIds 需要删除的领养成功记录主键集合
     * @return 结果
     */
    public int deleteAdoptionSuccessBySuccessIds(Long[] successIds);

    /**
     * 删除领养成功记录信息
     * 
     * @param successId 领养成功记录主键
     * @return 结果
     */
    public int deleteAdoptionSuccessBySuccessId(Long successId);
}
