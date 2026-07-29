package com.ruoyi.adoption.service;

import java.util.List;
import com.ruoyi.adoption.domain.AdoptionRequest;

/**
 * 领养申请Service接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface IAdoptionRequestService 
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
     * 批量删除领养申请
     * 
     * @param requestIds 需要删除的领养申请主键集合
     * @return 结果
     */
    public int deleteAdoptionRequestByRequestIds(Long[] requestIds);

    /**
     * 删除领养申请信息
     * 
     * @param requestId 领养申请主键
     * @return 结果
     */
    public int deleteAdoptionRequestByRequestId(Long requestId);
}
