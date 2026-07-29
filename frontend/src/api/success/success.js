import request from '@/utils/request'

// 查询领养成功记录列表
export function listSuccess(query) {
  return request({
    url: '/success/success/list',
    method: 'get',
    params: query
  })
}

// 查询领养成功记录详细
export function getSuccess(successId) {
  return request({
    url: '/success/success/' + successId,
    method: 'get'
  })
}

// 新增领养成功记录
export function addSuccess(data) {
  return request({
    url: '/success/success',
    method: 'post',
    data: data
  })
}

// 修改领养成功记录
export function updateSuccess(data) {
  return request({
    url: '/success/success',
    method: 'put',
    data: data
  })
}

// 删除领养成功记录
export function delSuccess(successId) {
  return request({
    url: '/success/success/' + successId,
    method: 'delete'
  })
}
