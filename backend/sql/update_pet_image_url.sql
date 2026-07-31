-- ----------------------------
-- 可选：将宠物图片地址由 .png 更新为压缩后的 .jpg
-- 说明：
--   1. 内置演示图片已由 PNG 压缩转换为 JPG（约减小 91%）
--   2. 旧地址 /images/pets/*.png 会由后端资源解析器自动回退到 *.jpg，不更新也能正常显示
--   3. 执行本脚本仅为让数据库地址与磁盘文件一致（利于浏览器缓存与排查日志）
-- ----------------------------
update tb_pet
set image_url = replace(image_url, '.png', '.jpg')
where image_url like '/images/pets/%.png';
