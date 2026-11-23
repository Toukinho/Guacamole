const TOKEN = 'guacamole.token'
const BASE = import.meta.env.VITE_API_URL ?? '/api'
export const getToken = () => sessionStorage.getItem(TOKEN) || localStorage.getItem(TOKEN)
const clearToken = () => { sessionStorage.removeItem(TOKEN); localStorage.removeItem(TOKEN) }

async function request(path, options = {}) {
  const token = getToken()
  let response
  try {
    response = await fetch(`${BASE}${path}`, {
      ...options,
      headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
    })
  } catch {
    throw Object.assign(new Error('Falha de conexão'), { status: 0 })
  }
  const data = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    if (response.status === 401 && token) clearToken()
    throw Object.assign(new Error('Falha na requisição'), { status: response.status, data })
  }
  return data
}

export function errorMessage(error) {
  if (error.status === 0) return 'Não foi possível conectar ao servidor. Confira se o backend está ligado.'
  if (error.status === 401) return 'Sua sessão expirou. Entre novamente.'
  if (error.status === 409) return 'Este e-mail já está cadastrado.'
  if (error.status === 429) return 'Muitas tentativas. Aguarde e tente novamente.'
  if (error.status === 400) return 'Confira os dados informados.'
  if (error.status === 404) return 'Não encontrado.'
  return 'Ocorreu um erro. Tente novamente.'
}

async function authenticate(path, body, remember = false) {
  const data = await request(path, { method: 'POST', body: JSON.stringify(body) })
  clearToken()
  ;(remember ? localStorage : sessionStorage).setItem(TOKEN, data.token)
}

export const api = {
  register: (name, email, password) => authenticate('/register', { name, email, password }),
  login: (email, password, remember) => authenticate('/login', { email, password }, remember),
  logout: async () => { try { await request('/logout', { method: 'POST' }) } finally { clearToken() } },
  me: () => request('/me'),
  watchlist: () => request('/me/watchlist'),
  courses: async (page) => {
    const data = await request(`/courses?page=${page - 1}&size=20`)
    return { items: data.content, page: data.page + 1, lastPage: data.totalPages }
  },
  course: (id) => request(`/courses/${id}`),
  lessons: (id) => request(`/courses/${id}/lessons`),
  progress: (id) => request(`/courses/${id}/progress`),
  saveCourse: (id) => request(`/courses/${id}/watchlist`, { method: 'POST' }),
  unsaveCourse: (id) => request(`/courses/${id}/watchlist`, { method: 'DELETE' }),
  lesson: (id) => request(`/lessons/${id}`),
  completeLesson: (id) => request(`/lessons/${id}/complete`, { method: 'POST' }),
  uncompleteLesson: (id) => request(`/lessons/${id}/complete`, { method: 'DELETE' }),
}
