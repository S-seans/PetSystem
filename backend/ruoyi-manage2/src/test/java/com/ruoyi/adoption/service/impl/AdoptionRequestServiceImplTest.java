package com.ruoyi.adoption.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ruoyi.adoption.constant.AdoptionStatus;
import com.ruoyi.adoption.domain.AdoptionRequest;
import com.ruoyi.adoption.mapper.AdoptionRequestMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.pet.constant.PetStatus;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import com.ruoyi.success.constant.SuccessStatus;
import com.ruoyi.success.domain.AdoptionSuccess;
import com.ruoyi.success.service.IAdoptionSuccessService;

/**
 * 领养申请 Service 单元测试：覆盖申请状态机流转与用户/管理员权限隔离
 */
@ExtendWith(MockitoExtension.class)
class AdoptionRequestServiceImplTest
{
    @Mock
    private AdoptionRequestMapper adoptionRequestMapper;

    @Mock
    private IPetService petService;

    @Mock
    private IAdoptionSuccessService adoptionSuccessService;

    @InjectMocks
    private AdoptionRequestServiceImpl adoptionRequestService;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    /** 以指定角色登录（无角色参数即为普通用户） */
    private void loginAs(Long userId, String... roleKeys)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        if (roleKeys != null && roleKeys.length > 0)
        {
            SysRole role = new SysRole();
            role.setRoleKey(roleKeys[0]);
            user.setRoles(Collections.singletonList(role));
        }
        else
        {
            user.setRoles(Collections.emptyList());
        }
        LoginUser loginUser = new LoginUser(userId, 100L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    private Pet mockAvailablePet(Long petId)
    {
        Pet pet = new Pet();
        pet.setPetId(petId);
        pet.setStatus(PetStatus.AVAILABLE);
        return pet;
    }

    private AdoptionRequest newRequest(Long petId)
    {
        AdoptionRequest request = new AdoptionRequest();
        request.setPetId(petId);
        request.setReason("我很喜欢这只宠物");
        return request;
    }

    // ---------- 新增申请 ----------

    @Test
    @DisplayName("普通用户提交申请：强制使用当前用户ID并置为待审核")
    void insertAdoptionRequest_forNormalUser_forcesOwnUserIdAndPending()
    {
        loginAs(1L);
        AdoptionRequest request = newRequest(10L);

        when(petService.selectPetByPetId(10L)).thenReturn(mockAvailablePet(10L));
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(null);
        when(adoptionRequestMapper.selectPendingRequestByPetId(10L)).thenReturn(null);
        when(adoptionRequestMapper.insertAdoptionRequest(any())).thenReturn(1);

        int result = adoptionRequestService.insertAdoptionRequest(request);

        assertEquals(1, result);
        assertEquals(1L, request.getUserId());
        assertEquals(AdoptionStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("管理员代提交：未指定申请人ID时抛出异常")
    void insertAdoptionRequest_adminWithoutUserId_throws()
    {
        loginAs(1L, "admin");
        AdoptionRequest request = newRequest(10L);
        request.setUserId(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.insertAdoptionRequest(request));
        assertEquals("管理员操作时，用户ID不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("宠物不存在时禁止提交申请")
    void insertAdoptionRequest_petNotFound_throws()
    {
        loginAs(1L);
        AdoptionRequest request = newRequest(999L);

        when(petService.selectPetByPetId(999L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.insertAdoptionRequest(request));
        assertEquals("宠物不存在，请检查宠物ID", ex.getMessage());
    }

    @Test
    @DisplayName("宠物已被领养成功时禁止再次申请")
    void insertAdoptionRequest_petAlreadyAdopted_throws()
    {
        loginAs(1L);
        AdoptionRequest request = newRequest(10L);

        AdoptionSuccess success = new AdoptionSuccess();
        success.setStatus(SuccessStatus.SUCCESS);
        when(petService.selectPetByPetId(10L)).thenReturn(mockAvailablePet(10L));
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(success);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.insertAdoptionRequest(request));
        assertEquals("该宠物已被领养成功，无法再次申请", ex.getMessage());
    }

    @Test
    @DisplayName("本人已有该宠物待审核申请时禁止重复提交")
    void insertAdoptionRequest_sameUserPending_throws()
    {
        loginAs(1L);
        AdoptionRequest request = newRequest(10L);

        AdoptionRequest pending = newRequest(10L);
        pending.setRequestId(1L);
        pending.setUserId(1L);
        when(petService.selectPetByPetId(10L)).thenReturn(mockAvailablePet(10L));
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(null);
        when(adoptionRequestMapper.selectPendingRequestByPetId(10L)).thenReturn(pending);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.insertAdoptionRequest(request));
        assertEquals("您已经提交过该宠物的领养申请，请等待审核结果", ex.getMessage());
    }

    @Test
    @DisplayName("他人已有该宠物待审核申请时禁止提交")
    void insertAdoptionRequest_otherUserPending_throws()
    {
        loginAs(1L);
        AdoptionRequest request = newRequest(10L);

        AdoptionRequest pending = newRequest(10L);
        pending.setRequestId(1L);
        pending.setUserId(2L);
        when(petService.selectPetByPetId(10L)).thenReturn(mockAvailablePet(10L));
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(null);
        when(adoptionRequestMapper.selectPendingRequestByPetId(10L)).thenReturn(pending);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.insertAdoptionRequest(request));
        assertEquals("该宠物已有其他用户提交的待审核申请，请等待审核完成或选择其他宠物", ex.getMessage());
    }

    // ---------- 修改/审核 ----------

    @Test
    @DisplayName("普通用户不能修改他人申请")
    void updateAdoptionRequest_normalUserNotOwner_throws()
    {
        loginAs(1L);

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(2L);
        original.setStatus(AdoptionStatus.PENDING);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setReason("修改后的理由");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.updateAdoptionRequest(update));
        assertEquals("无权修改此申请记录", ex.getMessage());
    }

    @Test
    @DisplayName("普通用户不能修改已审核的申请")
    void updateAdoptionRequest_normalUserReviewed_throws()
    {
        loginAs(1L);

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(1L);
        original.setStatus(AdoptionStatus.PASS);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setReason("修改后的理由");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.updateAdoptionRequest(update));
        assertEquals("该申请已审核，无法修改", ex.getMessage());
    }

    @Test
    @DisplayName("普通用户不能修改申请状态")
    void updateAdoptionRequest_normalUserChangeStatus_throws()
    {
        loginAs(1L);

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(1L);
        original.setStatus(AdoptionStatus.PENDING);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setStatus(AdoptionStatus.PASS);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.updateAdoptionRequest(update));
        assertEquals("无权修改申请状态", ex.getMessage());
    }

    @Test
    @DisplayName("普通用户可修改自己的待审核申请（不涉及状态）")
    void updateAdoptionRequest_normalUserOwnPending_ok()
    {
        loginAs(1L);

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(1L);
        original.setStatus(AdoptionStatus.PENDING);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);
        when(adoptionRequestMapper.updateAdoptionRequest(any())).thenReturn(1);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setReason("修改后的理由");

        int result = adoptionRequestService.updateAdoptionRequest(update);
        assertEquals(1, result);
        verify(adoptionSuccessService, never()).insertAdoptionSuccess(any());
        verify(petService, never()).updatePetStatus(anyLong(), any());
    }

    @Test
    @DisplayName("管理员审核通过：创建成功故事并更新宠物为已领养")
    void updateAdoptionRequest_adminPass_createsSuccessAndUpdatesPet()
    {
        loginAs(1L, "admin");

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(2L);
        original.setStatus(AdoptionStatus.PENDING);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(null);
        when(adoptionRequestMapper.selectPendingRequestByPetIdExclude(10L, 1L)).thenReturn(null);
        when(adoptionRequestMapper.updateAdoptionRequest(any())).thenReturn(1);
        when(adoptionSuccessService.insertAdoptionSuccess(any())).thenReturn(1);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setUserId(2L);
        update.setStatus(AdoptionStatus.PASS);

        int result = adoptionRequestService.updateAdoptionRequest(update);

        assertEquals(1, result);
        verify(adoptionSuccessService).insertAdoptionSuccess(any());
        verify(petService).updatePetStatus(10L, PetStatus.ADOPTED);
    }

    @Test
    @DisplayName("管理员将已通过改为其他状态：宠物状态回退为可领养")
    void updateAdoptionRequest_adminRevertPass_restoresPetAvailable()
    {
        loginAs(1L, "admin");

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(2L);
        original.setStatus(AdoptionStatus.PASS);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);
        when(adoptionRequestMapper.updateAdoptionRequest(any())).thenReturn(1);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setUserId(2L);
        update.setStatus(AdoptionStatus.REJECT);

        int result = adoptionRequestService.updateAdoptionRequest(update);

        assertEquals(1, result);
        verify(adoptionSuccessService, never()).insertAdoptionSuccess(any());
        verify(petService).updatePetStatus(10L, PetStatus.AVAILABLE);
    }

    @Test
    @DisplayName("管理员审核通过时若宠物已有成功记录则禁止")
    void updateAdoptionRequest_adminPass_petAlreadyAdopted_throws()
    {
        loginAs(1L, "admin");

        AdoptionRequest original = newRequest(10L);
        original.setRequestId(1L);
        original.setUserId(2L);
        original.setStatus(AdoptionStatus.PENDING);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(original);

        AdoptionSuccess success = new AdoptionSuccess();
        success.setStatus(SuccessStatus.SUCCESS);
        when(adoptionSuccessService.selectAdoptionSuccessByPetId(10L)).thenReturn(success);

        AdoptionRequest update = newRequest(10L);
        update.setRequestId(1L);
        update.setUserId(2L);
        update.setStatus(AdoptionStatus.PASS);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.updateAdoptionRequest(update));
        assertEquals("该宠物已被其他申请领养成功，无法再次通过", ex.getMessage());
    }

    // ---------- 查询与删除 ----------

    @Test
    @DisplayName("普通用户查询列表：强制限定为当前用户")
    void selectAdoptionRequestList_normalUser_forcesUserId()
    {
        loginAs(1L);
        AdoptionRequest query = new AdoptionRequest();
        when(adoptionRequestMapper.selectAdoptionRequestList(any())).thenReturn(Collections.emptyList());

        adoptionRequestService.selectAdoptionRequestList(query);

        assertEquals(1L, query.getUserId());
    }

    @Test
    @DisplayName("普通用户不能删除他人的申请")
    void deleteAdoptionRequestByRequestIds_normalUserOtherOwner_throws()
    {
        loginAs(1L);

        AdoptionRequest other = newRequest(10L);
        other.setRequestId(1L);
        other.setUserId(2L);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(other);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adoptionRequestService.deleteAdoptionRequestByRequestIds(new Long[] { 1L }));
        assertEquals("无权删除此申请记录", ex.getMessage());
    }

    @Test
    @DisplayName("普通用户可删除自己的申请")
    void deleteAdoptionRequestByRequestIds_normalUserOwn_ok()
    {
        loginAs(1L);

        AdoptionRequest own = newRequest(10L);
        own.setRequestId(1L);
        own.setUserId(1L);
        when(adoptionRequestMapper.selectAdoptionRequestByRequestId(1L)).thenReturn(own);
        when(adoptionRequestMapper.deleteAdoptionRequestByRequestIds(new Long[] { 1L })).thenReturn(1);

        int result = adoptionRequestService.deleteAdoptionRequestByRequestIds(new Long[] { 1L });
        assertEquals(1, result);
        assertTrue(result > 0);
    }
}
