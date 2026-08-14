package com.ruoyi.publicapi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.health.domain.PetHealth;
import com.ruoyi.health.service.IPetHealthService;
import com.ruoyi.success.domain.AdoptionSuccess;
import com.ruoyi.success.service.IAdoptionSuccessService;

/**
 * 用户端公开查询 Controller（只读）
 *
 * 供用户端页面查看领养记录（成功故事）与宠物健康记录，
 * 均为匿名可访问的只读接口，不含任何写操作。
 */
@RestController
@RequestMapping("/api/public")
public class PublicViewController extends BaseController
{
    @Autowired
    private IAdoptionSuccessService adoptionSuccessService;

    @Autowired
    private IPetHealthService petHealthService;

    /**
     * 查询领养记录（领养成功故事）列表
     */
    @Anonymous
    @GetMapping("/success")
    public TableDataInfo listSuccess(AdoptionSuccess adoptionSuccess)
    {
        startPage();
        if (adoptionSuccess == null)
        {
            adoptionSuccess = new AdoptionSuccess();
        }
        List<AdoptionSuccess> list = adoptionSuccessService.selectAdoptionSuccessList(adoptionSuccess);
        return getDataTable(list);
    }

    /**
     * 查询宠物健康记录列表
     */
    @Anonymous
    @GetMapping("/health")
    public TableDataInfo listHealth(PetHealth petHealth)
    {
        startPage();
        if (petHealth == null)
        {
            petHealth = new PetHealth();
        }
        List<PetHealth> list = petHealthService.selectPetHealthList(petHealth);
        return getDataTable(list);
    }
}
