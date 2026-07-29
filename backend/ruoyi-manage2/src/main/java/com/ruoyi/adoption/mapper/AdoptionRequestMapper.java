package com.ruoyi.adoption.mapper;

import java.util.List;
import com.ruoyi.adoption.domain.AdoptionRequest;

/**
 * 领养申请Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface AdoptionRequestMapper 
{
    /**
     * 查询领养申请
     * 
     * @param requestId 领养申请主键
     * @return 领养申请
     */
    public AdoptionRequest selectAdoptionRequestByRequestId(Long requestId);

    /**
     * 查询领养申请列表
     * 
     * @param adoptionRequest 领养申请
     * @return 领养申请集合
     */
    public List<AdoptionRequest> selectAdoptionRequestList(AdoptionRequest adoptionRequest);

    /**
     * 根据宠物ID查询待审核的申请
     *
     * @param petId 宠物ID
     * @return 领养申请
     */
    public AdoptionRequest selectPendingRequestByPetId(Long petId);

    /**
     * 根据宠物ID查询待审核的申请（排除指定申请ID）
     *
     * @param petId 宠物ID
     * @param excludeRequestId 排除的申请ID
     * @return 领养申请
     */
    public AdoptionRequest selectPendingRequestByPetIdExclude(Long petId, Long excludeRequestId);

    /**
     * 新增领养申请
     * 
     * @param adoptionRequest 领养申请
     * @return 结果
     */
    public int insertAdoptionRequest(AdoptionRequest adoptionRequest);

    /**
     * 修改领养申请
     * 
     * @param adoptionRequest 领养申请
     * @return 结果
     */
    public int updateAdoptionRequest(AdoptionRequest adoptionRequest);

    /**
     * 删除领养申请
     * 
     * @param requestId 领养申请主键
     * @return 结果
     */
    public int deleteAdoptionRequestByRequestId(Long requestId);

    /**
     * 批量删除领养申请
     * 
     * @param requestIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAdoptionRequestByRequestIds(Long[] requestIds);
}
