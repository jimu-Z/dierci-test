import request from '@/utils/request'

export function listYieldForecast(query) {
    return request({
        url: '/agri/yieldForecast/list',
        method: 'get',
        params: query
    })
}

export function getYieldForecast(forecastId) {
    return request({
        url: '/agri/yieldForecast/' + forecastId,
        method: 'get'
    })
}

export function addYieldForecast(data) {
    return request({
        url: '/agri/yieldForecast',
        method: 'post',
        data: data
    })
}

export function updateYieldForecast(data) {
    return request({
        url: '/agri/yieldForecast',
        method: 'put',
        data: data
    })
}

export function predictYieldForecast(data) {
    return request({
        url: '/agri/yieldForecast/predict',
        method: 'put',
        data: data
    })
}

export function invokeYieldForecast(forecastId) {
    return request({
        url: '/agri/yieldForecast/invoke/' + forecastId,
        method: 'post'
    })
}

export function delYieldForecast(forecastId) {
    return request({
        url: '/agri/yieldForecast/' + forecastId,
        method: 'delete'
    })
}
