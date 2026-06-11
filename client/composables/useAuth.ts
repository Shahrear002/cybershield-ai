import { ref } from 'vue'

export const useAuth = () => {
  const token = useCookie<string | null>('auth_token', {
    maxAge: 60 * 60 * 10, // 10 hours
    path: '/'
  })
  
  const role = useCookie<string | null>('auth_role', {
    maxAge: 60 * 60 * 10,
    path: '/'
  })

  const username = useCookie<string | null>('auth_username', {
    maxAge: 60 * 60 * 10,
    path: '/'
  })

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')

  const login = (jwt: string, userRole: string, userName: string) => {
    token.value = jwt
    role.value = userRole
    username.value = userName
  }

  const router = useRouter()

  const logout = () => {
    token.value = null
    role.value = null
    username.value = null
    router.push('/login')
  }

  return {
    token,
    role,
    username,
    isLoggedIn,
    isAdmin,
    login,
    logout
  }
}
