export default defineNuxtRouteMiddleware((to, from) => {
  const { isLoggedIn, isAdmin } = useAuth()

  if (!isLoggedIn.value) {
    return navigateTo('/login')
  }

  if (to.path.startsWith('/admin') && !isAdmin.value) {
    return navigateTo('/')
  }
})
