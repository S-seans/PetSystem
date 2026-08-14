-- ----------------------------
-- 加固升级 SQL（需在已初始化过的数据库上手动执行一次）
-- 说明：
--   1. 本脚本只做"增量加固"，不会清空或重建已有表
--   2. 若已执行过本脚本，再次执行会因列/索引/触发器已存在而报错，可忽略
--   3. 全新安装请直接使用 ry_business.sql（已包含以下加固内容）
--   4. 不使用 STORED 生成列：给"带外键的表"添加 STORED 生成列需要整表重建，
--      重建时 InnoDB 会重挂外键，报 ERROR 1215 (Cannot add foreign key constraint)。
--      因此改用"普通列 + 触发器"维护，语义完全一致且不触碰外键。
-- ----------------------------

-- 1、领养申请表：增加"同一宠物同一时刻最多一条待审核申请"的数据库级兜底列
alter table tb_adoption_request
  add column pending_pet_id bigint(20) null
    comment '待审核宠物ID（防重复申请兜底，由触发器维护）'
    after create_time;

-- 2、唯一索引（仅约束 pending 记录：pending_pet_id 为 NULL 的行不参与唯一性判断）
alter table tb_adoption_request
  add unique key uk_pending_pet (pending_pet_id);

-- 3、触发器：插入时自动维护 pending_pet_id（status=pending 时等于 pet_id，否则为 NULL）
drop trigger if exists trg_adoption_request_bi;
delimiter $$
create trigger trg_adoption_request_bi before insert on tb_adoption_request
for each row
begin
  set new.pending_pet_id = if(new.status = 'pending', new.pet_id, null);
end$$
delimiter ;

-- 4、触发器：更新时自动维护 pending_pet_id（审核通过/拒绝后自动释放槽位）
drop trigger if exists trg_adoption_request_bu;
delimiter $$
create trigger trg_adoption_request_bu before update on tb_adoption_request
for each row
begin
  set new.pending_pet_id = if(new.status = 'pending', new.pet_id, null);
end$$
delimiter ;

-- 说明：
-- a) 若第 1 步提示"Duplicate column name 'pending_pet_id'"，说明之前部分执行过，
--    可跳过第 1 步，直接继续执行第 2~4 步。
-- b) 若第 2 步因"同一宠物多条待审核申请"的历史脏数据创建失败，请先清理脏数据，
--    例如保留最新一条：
-- delete r from tb_adoption_request r
--   join tb_adoption_request k
--     on r.pet_id = k.pet_id and r.status = 'pending' and k.status = 'pending'
--    and r.request_id < k.request_id;
--    再重新执行第 2~4 步。
-- c) 自查表上是否已有外键（若存在，说明 1215 报错即由此引起）：
-- select table_name, constraint_name, constraint_type
--   from information_schema.table_constraints
--  where table_schema = database() and table_name = 'tb_adoption_request';
