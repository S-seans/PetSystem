import request from '@/utils/request'

export function listPublicPets(query) {
  return request({
    url: '/api/public/pets',
    method: 'get',
    headers: { isToken: false },
    params: query
  })
}

export function getPublicPet(petId) {
  return request({
    url: '/api/public/pets/' + petId,
    method: 'get',
    headers: { isToken: false }
  })
}
