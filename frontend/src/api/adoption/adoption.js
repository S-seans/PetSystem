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

// 删除领养申请
export function delAdoption(requestId) {
  return request({
    url: '/adoption/adoption/' + requestId,
    method: 'delete'
  })
}
