export function formatPetAge(months) {
  const m = Number(months)
  if (!Number.isFinite(m) || m < 0) return '未知'
  if (m === 0) return '不足1个月'
  const years = Math.floor(m / 12)
  const rest = m % 12
  if (years === 0) return `${rest}个月`
  return rest === 0 ? `${years}岁` : `${years}岁${rest}个月`
}

export function calcPetAgeMonths(years, months) {
  const hasY = years !== null && years !== undefined && years !== ''
  const hasM = months !== null && months !== undefined && months !== ''
  if (!hasY && !hasM) return null
  const y = Number(years) || 0
  const m = Number(months) || 0
  if (y < 0 || m < 0 || m > 11) return null
  return y * 12 + m
}

export function splitPetAge(months) {
  const m = Number(months)
  if (!Number.isFinite(m) || m < 0) return { years: null, months: null }
  return { years: Math.floor(m / 12), months: m % 12 }
}
