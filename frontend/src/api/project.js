import request from '@/utils/request'

export const getProjectList = (params) => {
  return request({
    url: '/projects',
    method: 'get',
    params
  })
}

export const getProjectById = (id) => {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

export const createProject = (data) => {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

export const updateProject = (id, data) => {
  return request({
    url: `/projects/${id}`,
    method: 'put',
    data
  })
}

export const deleteProject = (id) => {
  return request({
    url: `/projects/${id}`,
    method: 'delete'
  })
}

export const getUserProjects = (userId, params) => {
  return request({
    url: `/projects/user/${userId}`,
    method: 'get',
    params
  })
}