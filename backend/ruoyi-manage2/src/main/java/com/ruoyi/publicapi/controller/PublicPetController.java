package com.ruoyi.publicapi.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;

@RestController
@RequestMapping("/api/public")
public class PublicPetController
{
    @Autowired
    private IPetService petService;

    @Anonymous
    @GetMapping("/pets")
    public AjaxResult listPets()
    {
        Pet query = new Pet();
        query.setStatus("可领养");
        List<Pet> list = petService.selectPetList(query);
        return AjaxResult.success(list);
    }

    @Anonymous
    @GetMapping("/pets/{petId}")
    public AjaxResult getPet(@PathVariable Long petId)
    {
        Pet pet = petService.selectPetByPetId(petId);
        if (pet == null)
        {
            return AjaxResult.error("宠物不存在");
        }
        return AjaxResult.success(pet);
    }
}
