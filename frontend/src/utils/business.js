/**
 * 业务常量：宠物/领养/健康状态与性别
 * 与后端 com.ruoyi.pet.constant、com.ruoyi.adoption.constant、com.ruoyi.success.constant 保持一致
 */

/** 宠物状态 */
export const PET_STATUS = Object.freeze({
  AVAILABLE: '可领养',
  ADOPTED: '已领养'
})

/** 宠物性别（1公 0母） */
export const PET_GENDER = Object.freeze({
  MALE: '1',
  FEMALE: '0'
})

/** 领养申请状态 */
export const ADOPTION_STATUS = Object.freeze({
  PENDING: 'pending',
  PASS: 'pass',
  OUT: 'out',
  REJECT: 'reject'
})

/** 领养申请状态中文文案 */
export const ADOPTION_STATUS_TEXT = Object.freeze({
  [ADOPTION_STATUS.PENDING]: '待审核',
  [ADOPTION_STATUS.PASS]: '通过',
  [ADOPTION_STATUS.OUT]: '已领养',
  [ADOPTION_STATUS.REJECT]: '已拒绝'
})

/** 健康状态 */
export const HEALTH_STATUS = Object.freeze({
  HEALTHY: 'HEALTHY',
  SICK: 'SICK',
  RECOVERING: 'RECOVERING',
  GOOD: 'GOOD',
  DEAD: 'DEAD'
})

/** 健康状态中文文案 */
export const HEALTH_STATUS_TEXT = Object.freeze({
  [HEALTH_STATUS.HEALTHY]: '健康',
  [HEALTH_STATUS.SICK]: '生病',
  [HEALTH_STATUS.RECOVERING]: '康复中',
  [HEALTH_STATUS.GOOD]: '良好',
  [HEALTH_STATUS.DEAD]: '死亡'
})

/** 领养申请状态文本转换 */
export function adoptionStatusText(status) {
  return ADOPTION_STATUS_TEXT[status] || status || '未知'
}

/** 健康状态文本转换 */
export function healthStatusText(status) {
  return HEALTH_STATUS_TEXT[status] || status || '未知'
}

/** 性别文本转换（'1'公 '0'母） */
export function genderText(gender) {
  if (gender === PET_GENDER.MALE) return '公'
  if (gender === PET_GENDER.FEMALE) return '母'
  return '未知'
}
