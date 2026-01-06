import request from '@/utils/request'

export const getTimeEntryList = (params) => {
  return request({
    url: '/time-entries',
    method: 'get',
    params
  })
}

export const getTimeEntryById = (id) => {
  return request({
    url: `/time-entries/${id}`,
    method: 'get'
  })
}

export const getPendingApprovals = (params) => {
  return request({
    url: '/time-entries/pending',
    method: 'get',
    params
  })
}

export const getMonthlyStats = (params) => {
  return request({
    url: '/time-entries/stats/monthly',
    method: 'get',
    params
  })
}

export const createTimeEntry = (data) => {
  return request({
    url: '/time-entries',
    method: 'post',
    data
  })
}

export const updateTimeEntry = (id, data) => {
  return request({
    url: `/time-entries/${id}`,
    method: 'put',
    data
  })
}

export const approveTimeEntry = (id, data) => {
  return request({
    url: `/time-entries/${id}/approve`,
    method: 'post',
    data
  })
}

export const deleteTimeEntry = (id) => {
  return request({
    url: `/time-entries/${id}`,
    method: 'delete'
  })
}

export const getUserTimeEntries = (userId, params) => {
  return request({
    url: `/time-entries/user/${userId}`,
    method: 'get',
    params
  })
}

export const getPendingApprovalsByManager = (managerId, params) => {
  return request({
    url: `/time-entries/pending/manager/${managerId}`,
    method: 'get',
    params
  })
}