import request from '@/utils/request'

export function listPublicPets() {
  return request({
    url: '/api/public/pets',
    method: 'get',
    headers: { isToken: false }
  })
}

export function getPublicPet(petId) {
  return request({
    url: '/api/public/pets/' + petId,
    method: 'get',
    headers: { isToken: false }
  })
}
