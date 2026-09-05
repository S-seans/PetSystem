import request from '@/utils/request'

/**
 * 查询宠物健康记录列表（公开只读）
 */
export function listPublicHealth(query, config) {
  return request({
    url: '/api/public/health',
    method: 'get',
    headers: { isToken: false },
    params: query,
    ...config
  })
}
