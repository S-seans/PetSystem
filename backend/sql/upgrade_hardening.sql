-- ----------------------------
-- 加固升级 SQL（需在已初始化过的数据库上手动执行一次）
-- 说明：
--   1. 本脚本只做"增量加固"，不会清空或重建已有表
--   2. 若已执行过本脚本，再次执行会因列/索引已存在而报错，可忽略
--   3. 全新安装请直接使用 ry_business.sql（已包含以下加固内容）
-- ----------------------------

-- 1、领养申请表：为"同一宠物同一时刻最多一条待审核申请"增加数据库级兜底
--    生成列：status=pending 时等于 pet_id，否则为 NULL（NULL 可重复，唯一索引仅约束待审核记录）
alter table tb_adoption_request
  add column pending_pet_id bigint(20)
    generated always as (case when status = 'pending' then pet_id else null end) stored
    comment '待审核宠物ID（生成列，防重复申请兜底）'
    after create_time;

alter table tb_adoption_request
  add unique key uk_pending_pet (pending_pet_id);

-- 说明：若表中已存在"同一宠物多条待审核申请"的历史脏数据，上述唯一索引会创建失败，
-- 请先清理脏数据后再执行，例如保留最新一条：
-- delete r from tb_adoption_request r
--   join tb_adoption_request k
--     on r.pet_id = k.pet_id and r.status = 'pending' and k.status = 'pending'
--    and r.request_id < k.request_id;
