const normalizeFileBaseUrl = (raw) => {
  const fallback = 'http://localhost:8080'
  if (!raw || typeof raw !== 'string') {
    return fallback
  }
  const trimmed = raw.trim().replace(/\/$/, '')
  return trimmed.endsWith('/api') ? trimmed.slice(0, -4) : trimmed
}

const API_BASE = normalizeFileBaseUrl(import.meta.env.VITE_API_BASE_URL)

export function resolveFileUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  const normalizedPath = url.startsWith('/') ? url : `/${url}`
  return `${API_BASE}${normalizedPath}`
}
