import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  // 初始化时从localStorage恢复用户信息
  const initUser = () => {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')
    
    console.log('初始化用户信息:', { storedUser: !!storedUser, storedToken: !!storedToken })
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUser && storedUser !== 'null') {
      try {
        const parsedUser = JSON.parse(storedUser)
        user.value = parsedUser
        console.log('用户信息恢复成功:', parsedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        user.value = null
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        token.value = ''
      }
    }
  }

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUser = (newUser) => {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const loginAction = async (loginForm) => {
    try {
      const response = await login(loginForm)
      if (response.code === 200) {
        setToken(response.data.token)
        setUser(response.data.user)
        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('登录失败:', error)
      return { success: false, message: '登录失败' }
    }
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 初始化用户信息
  initUser()

  return {
    token,
    user,
    setToken,
    setUser,
    loginAction,
    logout,
    initUser
  }
})