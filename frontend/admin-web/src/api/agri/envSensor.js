import request from '@/utils/request'

export function listEnvSensor(query) {
    return request({
        url: '/agri/envSensor/list',
        method: 'get',
        params: query
    })
}

export function getEnvSensorDashboard(query) {
    return request({
        url: '/agri/envSensor/dashboard',
        method: 'get',
        params: query
    })
}

export function diagnoseEnvSensor(recordId) {
    return request({
        url: '/agri/envSensor/smart/diagnose/' + recordId,
        method: 'get'
    })
}

export function getEnvSensor(recordId) {
    return request({
        url: '/agri/envSensor/' + recordId,
        method: 'get'
    })
}

export function addEnvSensor(data) {
    return request({
        url: '/agri/envSensor',
        method: 'post',
        data: data
    })
}

export function updateEnvSensor(data) {
    return request({
        url: '/agri/envSensor',
        method: 'put',
        data: data
    })
}

export function delEnvSensor(recordId) {
    return request({
        url: '/agri/envSensor/' + recordId,
        method: 'delete'
    })
}
