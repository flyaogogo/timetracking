<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>工时管理系统</h2>
        <p>Time Tracking System</p>
      </div>
      
      <!-- 表单切换标签 -->
      <div class="form-tabs">
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login"></el-tab-pane>
          <el-tab-pane label="注册" name="register"></el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        v-if="activeTab === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              ref="captchaInputRef"
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              size="large"
              prefix-icon="View"
              style="flex: 1;"
            />
            <div class="captcha-container">
              <div class="captcha-image" title="点击刷新验证码">
                <img :src="captchaImageUrl" alt="验证码" style="width: 100%; height: 100%; cursor: pointer;" @click="refreshCaptcha" />
              </div>
              <el-icon class="refresh-icon" @click="refreshCaptcha" title="点击刷新验证码">
                <Refresh />
              </el-icon>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 注册表单 -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            size="large"
            prefix-icon="Avatar"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="registerLoading"
            @click="handleRegister"
            class="login-btn"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="system-description">
        <p>工时管理系统 - 高效管理项目工时，提升团队协作效率</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Refresh, Avatar, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 表单切换
const activeTab = ref('login')

const loginFormRef = ref()
const registerFormRef = ref()
const captchaInputRef = ref()
const loading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ]
}

// 验证码图片URL
const captchaImageUrl = ref('')

// 刷新验证码
const refreshCaptcha = () => {
  // 调用后端验证码API
  captchaImageUrl.value = `/api/captcha/image?timestamp=${Math.random()}`
}

// 初始化验证码
onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await userStore.loginAction(loginForm)
        if (result.success) {
          ElMessage.success('登录成功')
          router.push('/')
        } else {
          // 显示错误消息
          ElMessage.error(result.message || '登录失败')
          
          // 如果是验证码错误，刷新验证码并选中输入框
          if (result.message && result.message.includes('验证码')) {
            refreshCaptcha()
            // 选中验证码输入框
            setTimeout(() => {
              if (captchaInputRef.value) {
                captchaInputRef.value.select()
              }
            }, 100)
          }
        }
      } catch (error) {
        ElMessage.error('登录失败')
        // 异常情况下也刷新验证码
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        const result = await request.post('/auth/register', {
          username: registerForm.username,
          password: registerForm.password,
          realName: registerForm.realName,
          email: registerForm.email
        })
        
        if (result.code === 200) {
          ElMessage.success(result.message || '注册成功，请登录')
          // 切换到登录表单
          activeTab.value = 'login'
          // 清空注册表单
          Object.keys(registerForm).forEach(key => {
            registerForm[key] = ''
          })
        } else {
          ElMessage.error(result.message || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        registerLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.form-tabs {
  margin-bottom: 20px;
}

.login-tabs {
  --el-tabs-header-height: 40px;
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.captcha-image {
  width: 140px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  transition: all 0.2s ease;
}

.captcha-image:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.refresh-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

.refresh-icon:hover {
  color: #667eea;
  background-color: #f0f0f0;
}

.system-description {
  text-align: center;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 5px;
  margin-top: 20px;
  border: 1px solid #e0f2fe;
}

.system-description p {
  margin: 0;
  color: #0369a1;
  font-size: 13px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
  
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 20px;
  }
  
  .system-description {
    padding: 12px;
  }
  
  .captcha-row {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .captcha-container {
    justify-content: center;
    gap: 10px;
  }
  
  .captcha-image {
    width: 130px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 12px;
  }
  
  .login-box {
    padding: 24px 16px;
  }
  
  .login-header h2 {
    font-size: 18px;
  }
  
  .login-header p {
    font-size: 12px;
  }
  
  .system-description p {
    font-size: 12px;
  }
  
  .captcha-row {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .captcha-container {
    justify-content: center;
    gap: 8px;
  }
  
  .captcha-image {
    width: 120px;
    height: 40px;
  }
  
  .refresh-icon {
    font-size: 16px;
  }
}
</style>