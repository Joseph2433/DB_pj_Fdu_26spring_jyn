export function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '暂无时间'
}

export function userLabel(id) {
  return `用户 #${id}`
}

export function commentCount(post) {
  if (typeof post.commentCount === 'number') return post.commentCount
  return Array.isArray(post.comments) ? post.comments.length : 0
}
