package com.ruoyi.pet.controller;

import java.nio.file.Paths;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pet.constant.PetStatus;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import com.ruoyi.success.service.IAdoptionSuccessService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 宠物信息Controller
 * 
 * @author ruoyi
 * @date 2025-11-04
 */
@RestController
@RequestMapping("/pet/pet")
public class PetController extends BaseController
{
    @Autowired
    private IPetService petService;

    @Autowired
    private IAdoptionSuccessService adoptionSuccessService;

    /**
     * 获取首页统计数据
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:list')")
    @GetMapping("/dashboard/stats")
    public AjaxResult dashboardStats()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("waiting", petService.countPetByStatus(PetStatus.AVAILABLE));
        ajax.put("adoptedCount", adoptionSuccessService.countAdoptionSuccess());
        ajax.put("totalPets", petService.countPetByStatus(null));
        return ajax;
    }

    /**
     * 上传宠物图片（保存到宠物图片目录，通过 /images/** 访问）
     */
    @PreAuthorize("@ss.hasAnyPermi('pet:pet:add','pet:pet:edit')")
    @Log(title = "宠物图片上传", businessType = BusinessType.UPDATE)
    @PostMapping("/upload")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file)
    {
        try
        {
            // 图片大小限制 2MB
            if (file.isEmpty() || file.getSize() > 2 * 1024 * 1024)
            {
                return AjaxResult.error("上传图片大小不能超过 2MB");
            }
            // 仅允许图片格式（jpg/jpeg/png/bmp/gif）
            FileUploadUtils.assertAllowed(file, MimeTypeUtils.IMAGE_EXTENSION);

            // 宠物图片目录：与内置演示图片保持一致（uploadPath/images/pets）
            String petsDir = RuoYiConfig.getProfile() + "/images/pets";
            String fileName = FileUploadUtils.uuidFilename(file);
            String absPath = FileUploadUtils.getAbsoluteFile(petsDir, fileName).getAbsolutePath();
            file.transferTo(Paths.get(absPath));

            String url = "/images/pets/" + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", url);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            logger.error("宠物图片上传失败", e);
            return AjaxResult.error(StringUtils.defaultIfEmpty(e.getMessage(), "上传失败"));
        }
    }

    /**
     * 查询宠物信息列表
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:list')")
    @GetMapping("/list")
    public TableDataInfo list(Pet pet)
    {
        startPage();
        List<Pet> list = petService.selectPetList(pet);
        return getDataTable(list);
    }

    /**
     * 导出宠物信息列表
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:export')")
    @Log(title = "宠物信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Pet pet)
    {
        List<Pet> list = petService.selectPetList(pet);
        ExcelUtil<Pet> util = new ExcelUtil<Pet>(Pet.class);
        util.exportExcel(response, list, "宠物信息数据");
    }

    /**
     * 获取宠物信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:query')")
    @GetMapping(value = "/{petId}")
    public AjaxResult getInfo(@PathVariable("petId") Long petId)
    {
        return success(petService.selectPetByPetId(petId));
    }

    /**
     * 新增宠物信息
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:add')")
    @Log(title = "宠物信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Pet pet)
    {
        return toAjax(petService.insertPet(pet));
    }

    /**
     * 修改宠物信息
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:edit')")
    @Log(title = "宠物信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Pet pet)
    {
        return toAjax(petService.updatePet(pet));
    }

    /**
     * 删除宠物信息
     */
    @PreAuthorize("@ss.hasPermi('pet:pet:remove')")
    @Log(title = "宠物信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{petIds}")
    public AjaxResult remove(@PathVariable Long[] petIds)
    {
        return toAjax(petService.deletePetByPetIds(petIds));
    }
}
