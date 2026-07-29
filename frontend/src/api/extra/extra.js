import request from '@/utils/request'

// 查询用户扩展信息列表
export function listExtra(query) {
  return request({
    url: '/extra/extra/list',
    method: 'get',
    params: query
  })
}

// 查询用户扩展信息详细
export function getExtra(userId) {
  return request({
    url: '/extra/extra/' + userId,
    method: 'get'
  })
}

// 新增用户扩展信息
export function addExtra(data) {
  return request({
    url: '/extra/extra',
    method: 'post',
    data: data
  })
}

// 修改用户扩展信息
export function updateExtra(data) {
  return request({
    url: '/extra/extra',
    method: 'put',
    data: data
  })
}

// 删除用户扩展信息
export function delExtra(userId) {
  return request({
    url: '/extra/extra/' + userId,
    method: 'delete'
  })
}
