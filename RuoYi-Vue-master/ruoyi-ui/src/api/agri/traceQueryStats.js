import request from '@/utils/request'

export function listTraceQueryStats(query) {
    return request({
        url: '/agri/traceQueryStats/list',
        method: 'get',
        params: query
    })
}

export function getTraceQueryStatsDashboard(query) {
    return request({
        url: '/agri/traceQueryStats/dashboard',
        method: 'get',
        params: query
    })
}

export function getTraceQueryStatsInsight(statsId) {
    return request({
        url: '/agri/traceQueryStats/smart/insight/' + statsId,
        method: 'get'
    })
}

export function getTraceQueryStats(statsId) {
    return request({
        url: '/agri/traceQueryStats/' + statsId,
        method: 'get'
    })
}

export function addTraceQueryStats(data) {
    return request({
        url: '/agri/traceQueryStats',
        method: 'post',
        data: data
    })
}

export function updateTraceQueryStats(data) {
    return request({
        url: '/agri/traceQueryStats',
        method: 'put',
        data: data
    })
}

export function delTraceQueryStats(statsId) {
    return request({
        url: '/agri/traceQueryStats/' + statsId,
        method: 'delete'
    })
}
