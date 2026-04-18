import request from '@/utils/request'

export function listBrandTrace(query) {
    return request({
        url: '/agri/brandTrace/list',
        method: 'get',
        params: query
    })
}

export function getBrandTrace(pageId) {
    return request({
        url: '/agri/brandTrace/' + pageId,
        method: 'get'
    })
}

export function previewBrandTrace(traceCode) {
    return request({
        url: '/agri/brandTrace/preview/' + traceCode,
        method: 'get'
    })
}

export function addBrandTrace(data) {
    return request({
        url: '/agri/brandTrace',
        method: 'post',
        data: data
    })
}

export function updateBrandTrace(data) {
    return request({
        url: '/agri/brandTrace',
        method: 'put',
        data: data
    })
}

export function publishBrandTrace(pageId) {
    return request({
        url: '/agri/brandTrace/publish/' + pageId,
        method: 'put'
    })
}

export function delBrandTrace(pageId) {
    return request({
        url: '/agri/brandTrace/' + pageId,
        method: 'delete'
    })
}

export function getBrandTraceDashboard() {
    return request({
        url: '/agri/brandTrace/dashboard',
        method: 'get'
    })
}

export function getBrandTraceOpsDashboard() {
    return request({
        url: '/agri/brandTrace/dashboard/ops',
        method: 'get'
    })
}

export function smartInspectBrandTrace(pageId) {
    return request({
        url: '/agri/brandTrace/smart/inspect/' + pageId,
        method: 'get'
    })
}
