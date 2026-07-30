package com.ruoyi.success.mapper;

import java.util.List;
import com.ruoyi.success.domain.AdoptionSuccess;

/**
 * 领养成功记录Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface AdoptionSuccessMapper 
{
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
     * 删除领养成功记录
     * 
     * @param successId 领养成功记录主键
     * @return 结果
     */
    public int deleteAdoptionSuccessBySuccessId(Long successId);

    /**
     * 批量删除领养成功记录
     *
     * @param successIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAdoptionSuccessBySuccessIds(Long[] successIds);

    /**
     * 统计领养成功记录数量
     *
     * @return 数量
     */
    public Long countAdoptionSuccess();
}
