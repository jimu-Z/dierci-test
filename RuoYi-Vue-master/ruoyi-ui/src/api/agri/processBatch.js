import request from '@/utils/request'

export function listProcessBatch(query) {
    return request({
        url: '/agri/processBatch/list',
        method: 'get',
        params: query
    })
}

export function getProcessBatchDashboard(query) {
    return request({
        url: '/agri/processBatch/dashboard',
        method: 'get',
        params: query
    })
}

export function getProcessBatchCheck(linkId) {
    return request({
        url: '/agri/processBatch/smart/check/' + linkId,
        method: 'get'
    })
}

export function getProcessBatch(linkId) {
    return request({
        url: '/agri/processBatch/' + linkId,
        method: 'get'
    })
}

export function addProcessBatch(data) {
    return request({
        url: '/agri/processBatch',
        method: 'post',
        data: data
    })
}

export function updateProcessBatch(data) {
    return request({
        url: '/agri/processBatch',
        method: 'put',
        data: data
    })
}

export function delProcessBatch(linkId) {
    return request({
        url: '/agri/processBatch/' + linkId,
        method: 'delete'
    })
}
