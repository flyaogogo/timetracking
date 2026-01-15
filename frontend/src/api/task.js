import request from '@/utils/request'

export const getTaskList = (params) => {
  return request({
    url: '/tasks',
    method: 'get',
    params
  })
}

export const getTaskById = (id) => {
  return request({
    url: `/tasks/${id}`,
    method: 'get'
  })
}

export const getMyTasks = (params) => {
  return request({
    url: '/tasks/my',
    method: 'get',
    params
  })
}

export const createTask = (data) => {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}

export const updateTask = (id, data) => {
  return request({
    url: `/tasks/${id}`,
    method: 'put',
    data
  })
}

export const deleteTask = (id) => {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

export const getUserTasks = (userId, params) => {
  return request({
    url: `/tasks/user/${userId}`,
    method: 'get',
    params
  })
}

export const importTasks = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/tasks/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadTaskImportTemplate = () => {
  return request({
    url: '/tasks/import/template',
    method: 'get',
    responseType: 'blob'
  })
}