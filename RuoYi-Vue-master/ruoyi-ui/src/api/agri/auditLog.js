import request from '@/utils/request'

export function listAuditLog(query) {
    return request({
        url: '/agri/auditLog/list',
        method: 'get',
        params: query
    })
}

export function getAuditLog(auditId) {
    return request({
        url: '/agri/auditLog/' + auditId,
        method: 'get'
    })
}

export function addAuditLog(data) {
    return request({
        url: '/agri/auditLog',
        method: 'post',
        data: data
    })
}

export function updateAuditLog(data) {
    return request({
        url: '/agri/auditLog',
        method: 'put',
        data: data
    })
}

export function delAuditLog(auditId) {
    return request({
        url: '/agri/auditLog/' + auditId,
        method: 'delete'
    })
}

export function getAuditLogDashboard() {
    return request({
        url: '/agri/auditLog/dashboard',
        method: 'get'
    })
}

export function smartDetectAuditLog(auditId) {
    return request({
        url: '/agri/auditLog/smart/detect/' + auditId,
        method: 'get'
    })
}
