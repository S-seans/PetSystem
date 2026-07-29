import request from '@/utils/request'

// 查询宠物信息列表
export function listPet(query) {
  return request({
    url: '/pet/pet/list',
    method: 'get',
    params: query
  })
}

// 查询宠物信息详细
export function getPet(petId) {
  return request({
    url: '/pet/pet/' + petId,
    method: 'get'
  })
}

// 新增宠物信息
export function addPet(data) {
  return request({
    url: '/pet/pet',
    method: 'post',
    data: data
  })
}

// 修改宠物信息
export function updatePet(data) {
  return request({
    url: '/pet/pet',
    method: 'put',
    data: data
  })
}

// 删除宠物信息
export function delPet(petId) {
  return request({
    url: '/pet/pet/' + petId,
    method: 'delete'
  })
}
