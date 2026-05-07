import request from '@/utils/request'

export function listDeviceStatusMonitor(query) {
    return request({
        url: '/agri/deviceStatusMonitor/list',
        method: 'get',
        params: query
    })
}

export function getDeviceStatusMonitor(monitorId) {
    return request({
        url: '/agri/deviceStatusMonitor/' + monitorId,
        method: 'get'
    })
}

export function addDeviceStatusMonitor(data) {
    return request({
        url: '/agri/deviceStatusMonitor',
        method: 'post',
        data: data
    })
}

export function updateDeviceStatusMonitor(data) {
    return request({
        url: '/agri/deviceStatusMonitor',
        method: 'put',
        data: data
    })
}

export function delDeviceStatusMonitor(monitorId) {
    return request({
        url: '/agri/deviceStatusMonitor/' + monitorId,
        method: 'delete'
    })
}

export function getDeviceStatusMonitorDashboard(query) {
    return request({
        url: '/agri/deviceStatusMonitor/dashboard/overview',
        method: 'get',
        params: query
    })
}

export function getDeviceStatusMonitorAlerts(query) {
    return request({
        url: '/agri/deviceStatusMonitor/dashboard/alerts',
        method: 'get',
        params: query
    })
}
