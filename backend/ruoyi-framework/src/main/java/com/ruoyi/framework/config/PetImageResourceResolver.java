package com.ruoyi.framework.config;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * 宠物图片解析器
 * 
 * 兼容历史数据：数据库中宠物图片地址可能仍为 /images/pets/xxx.png，
 * 而磁盘文件已转为更小的 jpg 格式，当 .png 文件不存在时回退查找同名 .jpg 文件。
 */
public class PetImageResourceResolver implements ResourceResolver
{
    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath,
            List<? extends Resource> locations, ResourceResolverChain chain)
    {
        Resource resource = chain.resolveResource(request, requestPath, locations);
        if (resource != null)
        {
            return resource;
        }
        if (requestPath.endsWith(".png"))
        {
            String jpgPath = requestPath.substring(0, requestPath.length() - 4) + ".jpg";
            return chain.resolveResource(request, jpgPath, locations);
        }
        return null;
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain)
    {
        return chain.resolveUrlPath(resourcePath, locations);
    }
}
