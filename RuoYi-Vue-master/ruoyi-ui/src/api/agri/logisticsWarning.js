import request from '@/utils/request'

export function listLogisticsWarning(query) {
    return request({
        url: '/agri/logisticsWarning/list',
        method: 'get',
        params: query
    })
}

export function getLogisticsWarningDashboard(query) {
    return request({
        url: '/agri/logisticsWarning/dashboard',
        method: 'get',
        params: query
    })
}

export function triageLogisticsWarning(warningId) {
    return request({
        url: '/agri/logisticsWarning/smart/triage/' + warningId,
        method: 'get'
    })
}

export function getLogisticsWarning(warningId) {
    return request({
        url: '/agri/logisticsWarning/' + warningId,
        method: 'get'
    })
}

export function addLogisticsWarning(data) {
    return request({
        url: '/agri/logisticsWarning',
        method: 'post',
        data: data
    })
}

export function updateLogisticsWarning(data) {
    return request({
        url: '/agri/logisticsWarning',
        method: 'put',
        data: data
    })
}

export function generateLogisticsWarning(recordId) {
    return request({
        url: '/agri/logisticsWarning/generate/' + recordId,
        method: 'post'
    })
}

export function delLogisticsWarning(warningId) {
    return request({
        url: '/agri/logisticsWarning/' + warningId,
        method: 'delete'
    })
}
