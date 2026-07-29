package com.ruoyi.adoption.service.impl;

import java.util.List;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import com.ruoyi.success.domain.AdoptionSuccess;
import com.ruoyi.success.service.IAdoptionSuccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.adoption.mapper.AdoptionRequestMapper;
import com.ruoyi.adoption.domain.AdoptionRequest;
import com.ruoyi.adoption.service.IAdoptionRequestService;

/**
 * 领养申请Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Service
public class AdoptionRequestServiceImpl implements IAdoptionRequestService 
{
    @Autowired
    private AdoptionRequestMapper adoptionRequestMapper;

    @Autowired
    private IPetService petService;

    @Autowired
    private IAdoptionSuccessService adoptionSuccessService;

    /**
     * 查询领养申请
     * 
     * @param requestId 领养申请主键
     * @return 领养申请
     */
    @Override
    public AdoptionRequest selectAdoptionRequestByRequestId(Long requestId)
    {
        AdoptionRequest adoptionRequest = adoptionRequestMapper.selectAdoptionRequestByRequestId(requestId);

        // 获取当前登录用户
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        // 如果不是管理员角色，检查是否是自己申请的
        if (!SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("administrator")) {
            if (adoptionRequest != null && !currentUser.getUserId().equals(adoptionRequest.getUserId())) {
                throw new RuntimeException("无权查看此申请记录");
            }
        }

        return adoptionRequest;
    }

    /**
     * 查询领养申请列表
     * 
     * @param adoptionRequest 领养申请
     * @return 领养申请
     */
    @Override
    public List<AdoptionRequest> selectAdoptionRequestList(AdoptionRequest adoptionRequest)
    {
        // 获取当前登录用户
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        // 如果不是管理员角色，只查询当前用户的申请
        if (!SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("administrator")) {
            adoptionRequest.setUserId(currentUser.getUserId());
        }

        return adoptionRequestMapper.selectAdoptionRequestList(adoptionRequest);
    }

    /**
     * 检查宠物是否可以申请
     */
    private void checkPetAvailable(Long petId, Long currentUserId) {
        // 检查宠物是否存在
        Pet pet = petService.selectPetByPetId(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在，请检查宠物ID");
        }

        // 检查宠物是否已经被领养成功
        AdoptionSuccess successRecord = adoptionSuccessService.selectAdoptionSuccessByPetId(petId);
        if (successRecord != null && "success".equals(successRecord.getStatus())) {
            throw new RuntimeException("该宠物已被领养成功，无法再次申请");
        }

        // 检查该宠物是否有待审核的申请
        AdoptionRequest pendingRequest = adoptionRequestMapper.selectPendingRequestByPetId(petId);
        if (pendingRequest != null) {
            // 如果当前用户已经有待审核的申请，允许查看但不能重复提交
            if (currentUserId.equals(pendingRequest.getUserId())) {
                throw new RuntimeException("您已经提交过该宠物的领养申请，请等待审核结果");
            } else {
                throw new RuntimeException("该宠物已有其他用户提交的待审核申请，请等待审核完成或选择其他宠物");
            }
        }
    }


    /**
     * 新增领养申请
     * 
     * @param adoptionRequest 领养申请
     * @return 结果
     */
    @Override
    public int insertAdoptionRequest(AdoptionRequest adoptionRequest)
    {
        // 获取当前登录用户
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        // 检查用户角色并处理userId
        if (SecurityUtils.hasRole("user")) {
            // 如果是user角色，强制使用当前用户的ID
            adoptionRequest.setUserId(currentUser.getUserId());
            adoptionRequest.setStatus("pending"); // 设置为待审核状态
        } else if (SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("administrator")) {
            // 如果是admin或administrator角色，检查userId是否为空
            if (adoptionRequest.getUserId() == null) {
                throw new RuntimeException("管理员操作时，用户ID不能为空");
            }
        } else {
            // 其他角色，默认使用当前用户的ID
            adoptionRequest.setUserId(currentUser.getUserId());
        }

        // 检查宠物是否可以申请
        checkPetAvailable(adoptionRequest.getPetId(), adoptionRequest.getUserId());

        adoptionRequest.setCreateTime(DateUtils.getNowDate());
        return adoptionRequestMapper.insertAdoptionRequest(adoptionRequest);
    }

    /**
     * 修改领养申请
     * 
     * @param adoptionRequest 领养申请
     * @return 结果
     */
    @Override
    public int updateAdoptionRequest(AdoptionRequest adoptionRequest)
    {
        // 获取原始申请记录
        AdoptionRequest originalRequest = adoptionRequestMapper.selectAdoptionRequestByRequestId(adoptionRequest.getRequestId());

        if (originalRequest == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 获取当前登录用户
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();

        // 检查权限：如果不是管理员，且申请状态不是待审核，且不是申请人自己，则不允许修改
        if (!SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("administrator")) {
            // 检查是否是申请人自己
            if (!currentUser.getUserId().equals(originalRequest.getUserId())) {
                throw new RuntimeException("无权修改此申请记录");
            }

            // 检查申请状态，如果不是待审核状态，则不允许修改
            if (!"pending".equals(originalRequest.getStatus())) {
                throw new RuntimeException("该申请已审核，无法修改");
            }

            // 普通用户只能修改自己的待审核申请，且不能修改状态
            if (adoptionRequest.getStatus() != null && !adoptionRequest.getStatus().equals(originalRequest.getStatus())) {
                throw new RuntimeException("无权修改申请状态");
            }
        }

        // 管理员修改状态时的额外验证
        if ((SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("administrator"))
                && !adoptionRequest.getStatus().equals(originalRequest.getStatus())) {
            // 状态从其他状态改为pass时，需要验证宠物是否可被领养
            if ("pass".equals(adoptionRequest.getStatus()) && !"pass".equals(originalRequest.getStatus())) {
                checkPetAvailableForAdoption(adoptionRequest.getPetId(), adoptionRequest.getRequestId());
            }
        }

        int result = adoptionRequestMapper.updateAdoptionRequest(adoptionRequest);

        // 如果状态从其他状态改为pass，创建领养成功记录并更新宠物状态
        if ("pass".equals(adoptionRequest.getStatus()) && !"pass".equals(originalRequest.getStatus())) {
            createAdoptionSuccessRecord(adoptionRequest);
            // 更新宠物状态为"已领养"
            updatePetStatus(adoptionRequest.getPetId(), "已领养");
        }

        // 如果状态从pass改为其他状态，更新宠物状态为"可领养"
        if (!"pass".equals(adoptionRequest.getStatus()) && "pass".equals(originalRequest.getStatus())) {
            // 更新宠物状态为"可领养"
            updatePetStatus(adoptionRequest.getPetId(), "可领养");
        }

        return result;
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

    /**
     * 检查宠物是否可以被领养（用于审核通过时）
     */
    private void checkPetAvailableForAdoption(Long petId, Long excludeRequestId) {
        // 检查宠物是否已经被领养成功
        AdoptionSuccess successRecord = adoptionSuccessService.selectAdoptionSuccessByPetId(petId);
        if (successRecord != null && "success".equals(successRecord.getStatus())) {
            throw new RuntimeException("该宠物已被其他申请领养成功，无法再次通过");
        }

        // 检查该宠物是否有其他待审核的申请
        AdoptionRequest pendingRequest = adoptionRequestMapper.selectPendingRequestByPetIdExclude(petId, excludeRequestId);
        if (pendingRequest != null) {
            throw new RuntimeException("该宠物已有其他待审核的申请，请先处理其他申请");
        }
    }


    /**
     * 创建领养成功记录
     */
    private void createAdoptionSuccessRecord(AdoptionRequest adoptionRequest) {
        AdoptionSuccess adoptionSuccess = new AdoptionSuccess();
        adoptionSuccess.setRequestId(adoptionRequest.getRequestId());
        adoptionSuccess.setPetId(adoptionRequest.getPetId());
        adoptionSuccess.setUserId(adoptionRequest.getUserId());
        adoptionSuccess.setAdoptTime(DateUtils.getNowDate()); // 领养日期设为当前时间
        adoptionSuccess.setStatus("success"); // 成功状态

        adoptionSuccessService.insertAdoptionSuccess(adoptionSuccess);
    }

    /**
     * 批量删除领养申请
     * 
     * @param requestIds 需要删除的领养申请主键
     * @return 结果
     */
    @Override
    public int deleteAdoptionRequestByRequestIds(Long[] requestIds)
    {
        return adoptionRequestMapper.deleteAdoptionRequestByRequestIds(requestIds);
    }

    /**
     * 删除领养申请信息
     * 
     * @param requestId 领养申请主键
     * @return 结果
     */
    @Override
    public int deleteAdoptionRequestByRequestId(Long requestId)
    {
        return adoptionRequestMapper.deleteAdoptionRequestByRequestId(requestId);
    }
}
