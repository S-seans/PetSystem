package com.ruoyi.adoption.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.adoption.constant.AdoptionStatus;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.adoption.domain.AdoptionRequest;
import com.ruoyi.adoption.service.IAdoptionRequestService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 领养申请Controller
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/adoption/adoption")
public class AdoptionRequestController extends BaseController
{
    @Autowired
    private IAdoptionRequestService adoptionRequestService;

    /**
     * 查询领养申请列表
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:list')")
    @GetMapping("/list")
    public TableDataInfo list(AdoptionRequest adoptionRequest)
    {
        startPage();
        List<AdoptionRequest> list = adoptionRequestService.selectAdoptionRequestList(adoptionRequest);
        return getDataTable(list);
    }

    /**
     * 查询当前登录用户自己的领养申请列表（用户端）
     */
    @GetMapping("/my")
    public TableDataInfo my(AdoptionRequest adoptionRequest)
    {
        startPage();
        if (adoptionRequest == null)
        {
            adoptionRequest = new AdoptionRequest();
        }
        // /my 语义固定为"我的申请"，无论角色都限定为当前登录用户
        adoptionRequest.setUserId(SecurityUtils.getLoginUser().getUserId());
        List<AdoptionRequest> list = adoptionRequestService.selectAdoptionRequestList(adoptionRequest);
        return getDataTable(list);
    }

    /**
     * 撤销当前登录用户自己的待审核领养申请（用户端）
     */
    @DeleteMapping("/my/{requestId}")
    public AjaxResult myRemove(@PathVariable("requestId") Long requestId)
    {
        AdoptionRequest adoptionRequest = adoptionRequestService.selectAdoptionRequestByRequestId(requestId);
        if (adoptionRequest == null)
        {
            return error("申请记录不存在");
        }
        // 非管理员仅能撤销自己的申请；删除归属校验由 Service 层统一处理
        if (!AdoptionStatus.PENDING.equals(adoptionRequest.getStatus()))
        {
            return error("该申请已审核，无法撤销");
        }
        return toAjax(adoptionRequestService.deleteAdoptionRequestByRequestIds(new Long[] { requestId }));
    }

    /**
     * 导出领养申请列表
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:export')")
    @Log(title = "领养申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AdoptionRequest adoptionRequest)
    {
        List<AdoptionRequest> list = adoptionRequestService.selectAdoptionRequestList(adoptionRequest);
        ExcelUtil<AdoptionRequest> util = new ExcelUtil<AdoptionRequest>(AdoptionRequest.class);
        util.exportExcel(response, list, "领养申请数据");
    }

    /**
     * 获取领养申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:query')")
    @GetMapping(value = "/{requestId}")
    public AjaxResult getInfo(@PathVariable("requestId") Long requestId)
    {
        return success(adoptionRequestService.selectAdoptionRequestByRequestId(requestId));
    }

    /**
     * 新增领养申请
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:add')")
    @Log(title = "领养申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AdoptionRequest adoptionRequest)
    {
        return toAjax(adoptionRequestService.insertAdoptionRequest(adoptionRequest));
    }

    /**
     * 修改领养申请（归属/状态变更等权限校验由 Service 层统一处理）
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:edit')")
    @Log(title = "领养申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AdoptionRequest adoptionRequest)
    {
        return toAjax(adoptionRequestService.updateAdoptionRequest(adoptionRequest));
    }

    /**
     * 删除领养申请（非管理员仅能删除自己的申请，由 Service 层校验）
     */
    @PreAuthorize("@ss.hasPermi('adoption:adoption:remove')")
    @Log(title = "领养申请", businessType = BusinessType.DELETE)
	@DeleteMapping("/{requestIds}")
    public AjaxResult remove(@PathVariable Long[] requestIds)
    {
        return toAjax(adoptionRequestService.deleteAdoptionRequestByRequestIds(requestIds));
    }
}
