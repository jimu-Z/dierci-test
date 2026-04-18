import request from '@/utils/request'

export function listCarbonFootprint(query) {
    return request({
        url: '/agri/carbonFootprint/list',
        method: 'get',
        params: query
    })
}

export function getCarbonFootprint(modelId) {
    return request({
        url: '/agri/carbonFootprint/' + modelId,
        method: 'get'
    })
}

export function addCarbonFootprint(data) {
    return request({
        url: '/agri/carbonFootprint',
        method: 'post',
        data: data
    })
}

export function updateCarbonFootprint(data) {
    return request({
        url: '/agri/carbonFootprint',
        method: 'put',
        data: data
    })
}

export function delCarbonFootprint(modelId) {
    return request({
        url: '/agri/carbonFootprint/' + modelId,
        method: 'delete'
    })
}

export function getCarbonFootprintDashboard(query) {
    return request({
        url: '/agri/carbonFootprint/dashboard/overview',
        method: 'get',
        params: query
    })
}

export function analyzeCarbonFootprint(modelId) {
    return request({
        url: '/agri/carbonFootprint/smart/analyze/' + modelId,
        method: 'get'
    })
}
