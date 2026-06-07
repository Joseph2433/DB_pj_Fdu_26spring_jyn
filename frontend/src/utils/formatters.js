export function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '暂无时间'
}

export function userLabel() {
  return '未知用户'
}

export function authorLabel(entity) {
  return entity?.authorUsername || entity?.username || userLabel(entity?.authorId ?? entity?.id)
}

export function commentCount(post) {
  if (typeof post.commentCount === 'number') return post.commentCount
  return Array.isArray(post.comments) ? post.comments.length : 0
}
