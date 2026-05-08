import request from '@/utils/request'

export function listMarketForecast(query) {
    return request({
        url: '/agri/marketForecast/list',
        method: 'get',
        params: query
    })
}

export function getMarketForecastDashboard(query) {
    return request({
        url: '/agri/marketForecast/dashboard',
        method: 'get',
        params: query
    })
}

export function getMarketForecastReview(forecastId) {
    return request({
        url: '/agri/marketForecast/smart/review/' + forecastId,
        method: 'get'
    })
}

export function getMarketForecast(forecastId) {
    return request({
        url: '/agri/marketForecast/' + forecastId,
        method: 'get'
    })
}

export function addMarketForecast(data) {
    return request({
        url: '/agri/marketForecast',
        method: 'post',
        data: data
    })
}

export function updateMarketForecast(data) {
    return request({
        url: '/agri/marketForecast',
        method: 'put',
        data: data
    })
}

export function predictMarketForecast(data) {
    return request({
        url: '/agri/marketForecast/predict',
        method: 'put',
        data: data
    })
}

export function invokeMarketForecast(forecastId) {
    return request({
        url: '/agri/marketForecast/invoke/' + forecastId,
        method: 'post'
    })
}

export function delMarketForecast(forecastId) {
    return request({
        url: '/agri/marketForecast/' + forecastId,
        method: 'delete'
    })
}
