import request from '@/utils/request'

export function listPestIdentify(query) {
    return request({
        url: '/agri/pestIdentify/list',
        method: 'get',
        params: query
    })
}

export function getPestIdentify(taskId) {
    return request({
        url: '/agri/pestIdentify/' + taskId,
        method: 'get'
    })
}

export function addPestIdentify(data) {
    return request({
        url: '/agri/pestIdentify',
        method: 'post',
        data: data
    })
}

export function updatePestIdentify(data) {
    return request({
        url: '/agri/pestIdentify',
        method: 'put',
        data: data
    })
}

export function feedbackPestIdentify(data) {
    return request({
        url: '/agri/pestIdentify/feedback',
        method: 'put',
        data: data
    })
}

export function invokePestIdentify(taskId) {
    return request({
        url: '/agri/pestIdentify/invoke/' + taskId,
        method: 'post'
    })
}

export function quickInvokePestIdentify(data) {
    return request({
        url: '/agri/pestIdentify/quickInvoke',
        method: 'post',
        data: data,
        timeout: 20000
    })
}

export function getPestIdentifyDashboard(query) {
    return request({
        url: '/agri/pestIdentify/dashboard',
        method: 'get',
        params: query
    })
}

export function delPestIdentify(taskId) {
    return request({
        url: '/agri/pestIdentify/' + taskId,
        method: 'delete'
    })
}
