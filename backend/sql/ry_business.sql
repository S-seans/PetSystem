-- ----------------------------
-- 业务表 SQL 脚本
-- 说明：手动执行，不会自动运行，不与现有数据冲突
-- 如果表已存在会报错但不影响已有数据，可加 IF NOT EXISTS
-- ----------------------------

-- ----------------------------
-- 1、宠物信息表
-- ----------------------------
drop table if exists tb_pet;
create table tb_pet (
  pet_id           bigint(20)      not null auto_increment    comment '宠物ID',
  name             varchar(50)     not null                   comment '宠物名称',
  breed            varchar(50)     default ''                 comment '品种',
  age              bigint(4)       default 0                  comment '年龄（月）',
  gender           char(1)         default '1'                comment '性别（1公 0母）',
  weight           decimal(10,2)   default 0.00               comment '体重kg',
  status           varchar(20)     default '可领养'            comment '状态（可领养/已领养）',
  image_url        varchar(500)    default ''                 comment '照片地址',
  description      varchar(500)    default ''                 comment '状况描述',
  rescue_date      date                                       comment '救助日期',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default ''                 comment '备注',
  primary key (pet_id)
) engine=innodb auto_increment=100 comment = '宠物信息表';

-- ----------------------------
-- 2、领养申请表
-- ----------------------------
drop table if exists tb_adoption_request;
create table tb_adoption_request (
  request_id       bigint(20)      not null auto_increment    comment '申请ID',
  pet_id           bigint(20)      not null                   comment '宠物ID',
  user_id          bigint(20)      not null                   comment '申请人ID',
  reason           varchar(500)    default ''                 comment '领养理由',
  status           varchar(20)     default 'pending'          comment '状态（pending待审核/pass通过/out已领养/reject已拒绝）',
  review_remark    varchar(500)    default ''                 comment '审核备注',
  review_time      datetime                                   comment '审核时间',
  review_by        varchar(64)     default ''                 comment '审核人',
  create_time      datetime                                   comment '创建时间',
  -- 生成列：status=pending 时等于 pet_id，否则为 NULL（NULL 可重复，唯一索引仅约束待审核记录）
  pending_pet_id   bigint(20)      generated always as (case when status = 'pending' then pet_id else null end) stored comment '待审核宠物ID（生成列，防重复申请兜底）',
  primary key (request_id),
  -- 同一宠物同一时刻最多一条待审核申请（数据库级兜底，配合 Service 层业务校验）
  unique key uk_pending_pet (pending_pet_id)
) engine=innodb auto_increment=100 comment = '领养申请表';

-- ----------------------------
-- 3、宠物健康记录表
-- ----------------------------
drop table if exists tb_pet_health;
create table tb_pet_health (
  health_id        bigint(20)      not null auto_increment    comment '健康记录ID',
  pet_id           bigint(20)      not null                   comment '宠物ID',
  record_date      date                                       comment '记录日期',
  vaccine_name     varchar(100)    default ''                 comment '疫苗名称',
  is_sterilized    int(1)          default 0                  comment '是否绝育（1是 0否）',
  health_status    varchar(50)     default ''                 comment '健康状态（HEALTHY/SICK/DEAD/GOOD/RECOVERING）',
  description      varchar(500)    default ''                 comment '详细描述',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  primary key (health_id)
) engine=innodb auto_increment=100 comment = '宠物健康记录表';

-- ----------------------------
-- 4、领养成功故事表
-- ----------------------------
drop table if exists tb_adoption_success;
create table tb_adoption_success (
  success_id       bigint(20)      not null auto_increment    comment '成功故事ID',
  request_id       bigint(20)      default null               comment '申请ID',
  pet_id           bigint(20)      not null                   comment '宠物ID',
  user_id          bigint(20)      not null                   comment '领养人ID',
  adopt_time       datetime                                   comment '领养时间',
  follow_up_date   date                                       comment '回访日期',
  status           varchar(20)     default ''                 comment '状态',
  remark           varchar(500)    default ''                 comment '备注',
  create_time      datetime                                   comment '创建时间',
  primary key (success_id)
) engine=innodb auto_increment=100 comment = '领养成功故事表';

-- ----------------------------
-- 5、菜单数据（使用 2000+ 号段，不与现有菜单冲突）
-- ----------------------------
-- 一级菜单：宠物管理
insert into sys_menu values('2000', '宠物管理', '0', '5', 'pet', null, '', '', 1, 0, 'M', '0', '0', '', 'pet', 'admin', sysdate(), '', null, '宠物管理目录');

-- 二级菜单
insert into sys_menu values('2010', '宠物信息', '2000', '1', 'pet',   'pet/pet/index',         '', '', 1, 0, 'C', '0', '0', 'pet:pet:list',         'animal',     'admin', sysdate(), '', null, '宠物信息菜单');
insert into sys_menu values('2020', '领养申请', '2000', '2', 'adoption', 'adoption/adoption/index', '', '', 1, 0, 'C', '0', '0', 'adoption:adoption:list', 'form',       'admin', sysdate(), '', null, '领养申请菜单');
insert into sys_menu values('2030', '健康记录', '2000', '3', 'health',  'health/health/index',   '', '', 1, 0, 'C', '0', '0', 'health:health:list',    'health',     'admin', sysdate(), '', null, '健康记录菜单');
insert into sys_menu values('2040', '成功故事', '2000', '4', 'success', 'success/success/index', '', '', 1, 0, 'C', '0', '0', 'success:success:list',   'star',       'admin', sysdate(), '', null, '成功故事菜单');

-- 宠物信息按钮
insert into sys_menu values('2011', '宠物查询', '2010', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'pet:pet:query',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '宠物新增', '2010', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'pet:pet:add',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '宠物修改', '2010', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'pet:pet:edit',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '宠物删除', '2010', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'pet:pet:remove',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2015', '宠物导出', '2010', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'pet:pet:export',   '#', 'admin', sysdate(), '', null, '');

-- 领养申请按钮
insert into sys_menu values('2021', '申请查询', '2020', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'adoption:adoption:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '申请新增', '2020', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'adoption:adoption:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2023', '申请修改', '2020', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'adoption:adoption:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2024', '申请删除', '2020', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'adoption:adoption:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2025', '申请导出', '2020', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'adoption:adoption:export', '#', 'admin', sysdate(), '', null, '');

-- 健康记录按钮
insert into sys_menu values('2031', '健康查询', '2030', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'health:health:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2032', '健康新增', '2030', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'health:health:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2033', '健康修改', '2030', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'health:health:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2034', '健康删除', '2030', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'health:health:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2035', '健康导出', '2030', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'health:health:export', '#', 'admin', sysdate(), '', null, '');

-- 成功故事按钮
insert into sys_menu values('2041', '故事查询', '2040', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'success:success:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2042', '故事新增', '2040', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'success:success:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2043', '故事修改', '2040', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'success:success:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2044', '故事删除', '2040', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'success:success:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2045', '故事导出', '2040', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'success:success:export', '#', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 6、角色-菜单关联（为普通角色分配业务菜单）
-- ----------------------------
insert into sys_role_menu values ('2', '2000');
insert into sys_role_menu values ('2', '2010');
insert into sys_role_menu values ('2', '2020');
insert into sys_role_menu values ('2', '2030');
insert into sys_role_menu values ('2', '2040');
insert into sys_role_menu values ('2', '2011');
insert into sys_role_menu values ('2', '2012');
insert into sys_role_menu values ('2', '2013');
insert into sys_role_menu values ('2', '2014');
insert into sys_role_menu values ('2', '2015');
insert into sys_role_menu values ('2', '2021');
insert into sys_role_menu values ('2', '2022');
insert into sys_role_menu values ('2', '2023');
insert into sys_role_menu values ('2', '2024');
insert into sys_role_menu values ('2', '2025');
insert into sys_role_menu values ('2', '2031');
insert into sys_role_menu values ('2', '2032');
insert into sys_role_menu values ('2', '2033');
insert into sys_role_menu values ('2', '2034');
insert into sys_role_menu values ('2', '2035');
insert into sys_role_menu values ('2', '2041');
insert into sys_role_menu values ('2', '2042');
insert into sys_role_menu values ('2', '2043');
insert into sys_role_menu values ('2', '2044');
insert into sys_role_menu values ('2', '2045');
