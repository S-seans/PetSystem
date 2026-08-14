import request from '@/utils/request'

/**
 * 查询领养记录（成功故事）列表（公开只读）
 */
export function listPublicSuccess(query) {
  return request({
    url: '/api/public/success',
    method: 'get',
    headers: { isToken: false },
    params: query
  })
}
