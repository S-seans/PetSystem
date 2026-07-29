import request from '@/utils/request'

// 查询宠物健康记录列表
export function listHealth(query) {
  return request({
    url: '/health/health/list',
    method: 'get',
    params: query
  })
}

// 查询宠物健康记录详细
export function getHealth(healthId) {
  return request({
    url: '/health/health/' + healthId,
    method: 'get'
  })
}

// 新增宠物健康记录
export function addHealth(data) {
  return request({
    url: '/health/health',
    method: 'post',
    data: data
  })
}

// 修改宠物健康记录
export function updateHealth(data) {
  return request({
    url: '/health/health',
    method: 'put',
    data: data
  })
}

// 删除宠物健康记录
export function delHealth(healthId) {
  return request({
    url: '/health/health/' + healthId,
    method: 'delete'
  })
}
