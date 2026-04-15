import request from '@/utils/request'

export function listDeviceAccess(query) {
    return request({
        url: '/agri/deviceAccess/list',
        method: 'get',
        params: query
    })
}

export function getDeviceAccess(nodeId) {
    return request({
        url: '/agri/deviceAccess/' + nodeId,
        method: 'get'
    })
}

export function addDeviceAccess(data) {
    return request({
        url: '/agri/deviceAccess',
        method: 'post',
        data: data
    })
}

export function updateDeviceAccess(data) {
    return request({
        url: '/agri/deviceAccess',
        method: 'put',
        data: data
    })
}

export function activateDeviceAccess(nodeId) {
    return request({
        url: '/agri/deviceAccess/activate/' + nodeId,
        method: 'put'
    })
}

export function delDeviceAccess(nodeId) {
    return request({
        url: '/agri/deviceAccess/' + nodeId,
        method: 'delete'
    })
}
