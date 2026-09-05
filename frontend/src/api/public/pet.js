import request from '@/utils/request'

export function listPublicPets(query, config) {
  return request({
    url: '/api/public/pets',
    method: 'get',
    headers: { isToken: false },
    params: query,
    ...config
  })
}

export function getPublicPet(petId, config) {
  return request({
    url: '/api/public/pets/' + petId,
    method: 'get',
    headers: { isToken: false },
    ...config
  })
}
