import request from '@/utils/request'

export function listOutputSalesTrend(query) {
    return request({
        url: '/agri/outputSalesTrend/list',
        method: 'get',
        params: query
    })
}

export function getOutputSalesTrend(trendId) {
    return request({
        url: '/agri/outputSalesTrend/' + trendId,
        method: 'get'
    })
}

export function addOutputSalesTrend(data) {
    return request({
        url: '/agri/outputSalesTrend',
        method: 'post',
        data: data
    })
}

export function updateOutputSalesTrend(data) {
    return request({
        url: '/agri/outputSalesTrend',
        method: 'put',
        data: data
    })
}

export function delOutputSalesTrend(trendId) {
    return request({
        url: '/agri/outputSalesTrend/' + trendId,
        method: 'delete'
    })
}

export function getOutputSalesDashboard(query) {
    return request({
        url: '/agri/outputSalesTrend/dashboard',
        method: 'get',
        timeout: 20000,
        params: query
    })
}
