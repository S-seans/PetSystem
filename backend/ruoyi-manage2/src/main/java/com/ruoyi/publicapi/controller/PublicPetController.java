package com.ruoyi.publicapi.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.pet.constant.PetStatus;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;

@RestController
@RequestMapping("/api/public")
public class PublicPetController extends BaseController
{
    @Autowired
    private IPetService petService;

    @Anonymous
    @GetMapping("/pets")
    public TableDataInfo listPets(Pet pet)
    {
        startPage();
        if (pet == null)
        {
            pet = new Pet();
        }
        pet.setStatus(PetStatus.AVAILABLE);
        List<Pet> list = petService.selectPublicPetList(pet);
        return getDataTable(list);
    }

    @Anonymous
    @GetMapping("/pets/{petId}")
    public AjaxResult getPet(@PathVariable Long petId)
    {
        Pet pet = petService.selectPublicPetByPetId(petId);
        if (pet == null)
        {
            return AjaxResult.error("宠物不存在");
        }
        return AjaxResult.success(pet);
    }
}
