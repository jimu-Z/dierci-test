function filterExcerptLine(items) {
  if (!Array.isArray(items)) {
    return []
  }
  return items.filter(item => !(typeof item === 'string' && item.indexOf('AI原文摘录：') === 0))
}

export function normalizeSmartResult(payload, listKey) {
  const source = payload || {}
  const normalized = {
    ...source,
    aiOriginalExcerpt: source.aiOriginalExcerpt || source.ai_original_excerpt || ''
  }
  if (listKey) {
    normalized[listKey] = filterExcerptLine(source[listKey])
  }
  return normalized
}
