package com.ruoyi.health.controller;

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
import com.ruoyi.health.domain.PetHealth;
import com.ruoyi.health.service.IPetHealthService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 宠物健康记录Controller
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/health/health")
public class PetHealthController extends BaseController
{
    @Autowired
    private IPetHealthService petHealthService;

    /**
     * 查询宠物健康记录列表
     */
    @PreAuthorize("@ss.hasPermi('health:health:list')")
    @GetMapping("/list")
    public TableDataInfo list(PetHealth petHealth)
    {
        startPage();
        List<PetHealth> list = petHealthService.selectPetHealthList(petHealth);
        return getDataTable(list);
    }

    /**
     * 导出宠物健康记录列表
     */
    @PreAuthorize("@ss.hasPermi('health:health:export')")
    @Log(title = "宠物健康记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PetHealth petHealth)
    {
        List<PetHealth> list = petHealthService.selectPetHealthList(petHealth);
        ExcelUtil<PetHealth> util = new ExcelUtil<PetHealth>(PetHealth.class);
        util.exportExcel(response, list, "宠物健康记录数据");
    }

    /**
     * 获取宠物健康记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:health:query')")
    @GetMapping(value = "/{healthId}")
    public AjaxResult getInfo(@PathVariable("healthId") Long healthId)
    {
        return success(petHealthService.selectPetHealthByHealthId(healthId));
    }

    /**
     * 新增宠物健康记录
     */
    @PreAuthorize("@ss.hasPermi('health:health:add')")
    @Log(title = "宠物健康记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PetHealth petHealth)
    {
        return toAjax(petHealthService.insertPetHealth(petHealth));
    }

    /**
     * 修改宠物健康记录
     */
    @PreAuthorize("@ss.hasPermi('health:health:edit')")
    @Log(title = "宠物健康记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PetHealth petHealth)
    {
        return toAjax(petHealthService.updatePetHealth(petHealth));
    }

    /**
     * 删除宠物健康记录
     */
    @PreAuthorize("@ss.hasPermi('health:health:remove')")
    @Log(title = "宠物健康记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{healthIds}")
    public AjaxResult remove(@PathVariable Long[] healthIds)
    {
        return toAjax(petHealthService.deletePetHealthByHealthIds(healthIds));
    }
}
