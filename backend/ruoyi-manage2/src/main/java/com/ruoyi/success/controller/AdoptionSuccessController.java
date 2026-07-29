package com.ruoyi.success.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.success.domain.AdoptionSuccess;
import com.ruoyi.success.service.IAdoptionSuccessService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 领养成功记录Controller
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/success/success")
public class AdoptionSuccessController extends BaseController
{
    @Autowired
    private IAdoptionSuccessService adoptionSuccessService;

    /**
     * 查询领养成功记录列表
     */
    @PreAuthorize("@ss.hasPermi('success:success:list')")
    @GetMapping("/list")
    public TableDataInfo list(AdoptionSuccess adoptionSuccess)
    {
        startPage();
        List<AdoptionSuccess> list = adoptionSuccessService.selectAdoptionSuccessList(adoptionSuccess);
        return getDataTable(list);
    }

    /**
     * 导出领养成功记录列表
     */
    @PreAuthorize("@ss.hasPermi('success:success:export')")
    @Log(title = "领养成功记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AdoptionSuccess adoptionSuccess)
    {
        List<AdoptionSuccess> list = adoptionSuccessService.selectAdoptionSuccessList(adoptionSuccess);
        ExcelUtil<AdoptionSuccess> util = new ExcelUtil<AdoptionSuccess>(AdoptionSuccess.class);
        util.exportExcel(response, list, "领养成功记录数据");
    }

    /**
     * 获取领养成功记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('success:success:query')")
    @GetMapping(value = "/{successId}")
    public AjaxResult getInfo(@PathVariable("successId") Long successId)
    {
        return success(adoptionSuccessService.selectAdoptionSuccessBySuccessId(successId));
    }

    /**
     * 新增领养成功记录
     */
    @PreAuthorize("@ss.hasPermi('success:success:add')")
    @Log(title = "领养成功记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AdoptionSuccess adoptionSuccess)
    {
        return toAjax(adoptionSuccessService.insertAdoptionSuccess(adoptionSuccess));
    }

    /**
     * 修改领养成功记录
     */
    @PreAuthorize("@ss.hasPermi('success:success:edit')")
    @Log(title = "领养成功记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AdoptionSuccess adoptionSuccess)
    {
        return toAjax(adoptionSuccessService.updateAdoptionSuccess(adoptionSuccess));
    }

    /**
     * 删除领养成功记录
     */
    @PreAuthorize("@ss.hasPermi('success:success:remove')")
    @Log(title = "领养成功记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{successIds}")
    public AjaxResult remove(@PathVariable Long[] successIds)
    {
        return toAjax(adoptionSuccessService.deleteAdoptionSuccessBySuccessIds(successIds));
    }
}
