import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/pet/pet/dashboard/stats',
    method: 'get'
  })
}
