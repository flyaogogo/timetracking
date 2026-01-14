<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>工时管理系统</h2>
        <p>Time Tracking System</p>
      </div>
      
      <el-form
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
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
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
          ElMessage.error(result.message || '登录失败')
        }
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
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