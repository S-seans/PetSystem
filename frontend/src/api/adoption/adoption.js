import request from '@/utils/request'

// 查询领养申请列表
export function listAdoption(query) {
  return request({
    url: '/adoption/adoption/list',
    method: 'get',
    params: query
  })
}

// 查询领养申请详细
export function getAdoption(requestId) {
  return request({
    url: '/adoption/adoption/' + requestId,
    method: 'get'
  })
}

// 新增领养申请
export function addAdoption(data) {
  return request({
    url: '/adoption/adoption',
    method: 'post',
    data: data
  })
}

// 修改领养申请
export function updateAdoption(data) {
  return request({
    url: '/adoption/adoption',
    method: 'put',
    data: data
  })
}

// 查询当前登录用户自己的领养申请列表（用户端）
export function listMyAdoption(query) {
  return request({
    url: '/adoption/adoption/my',
    method: 'get',
    params: query
  })
}

// 撤销当前登录用户自己的领养申请（用户端）
export function delMyAdoption(requestId) {
  return request({
    url: '/adoption/adoption/my/' + requestId,
    method: 'delete'
  })
}

// 删除领养申请
export function delAdoption(requestId) {
  return request({
    url: '/adoption/adoption/' + requestId,
    method: 'delete'
  })
}
