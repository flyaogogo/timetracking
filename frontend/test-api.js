// 使用Node.js 18+内置的fetch函数

async function testLogin() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: 'xujun',
        password: '123456'
      })
    });
    
    console.log('登录API响应状态:', response.status);
    const data = await response.json();
    console.log('登录API响应数据:', data);
    
    if (data.code === 200 && data.data.token) {
      console.log('登录成功，获取到token:', data.data.token);
      return data.data.token;
    } else {
      console.log('登录失败:', data.message);
      return null;
    }
  } catch (error) {
    console.error('登录API调用失败:', error);
    return null;
  }
}

async function testPermissionCheck(token) {
  try {
    const response = await fetch('http://localhost:8080/api/project-manager/check-permission', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log('权限检查API响应状态:', response.status);
    const data = await response.json();
    console.log('权限检查API响应数据:', data);
    
    if (data.code === 200) {
      console.log('权限检查成功');
      console.log('isProjectManager:', data.data.isProjectManager);
      console.log('managedProjectsCount:', data.data.managedProjectsCount);
      return data.data;
    } else {
      console.log('权限检查失败:', data.message);
      return null;
    }
  } catch (error) {
    console.error('权限检查API调用失败:', error);
    return null;
  }
}

async function runTests() {
  console.log('开始测试登录API...');
  const token = await testLogin();
  
  if (token) {
    console.log('\n开始测试权限检查API...');
    await testPermissionCheck(token);
  }
}

runTests();
