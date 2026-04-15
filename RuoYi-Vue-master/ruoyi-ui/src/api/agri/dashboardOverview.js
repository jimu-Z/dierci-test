import request from '@/utils/request'

export function listDashboardOverview(query) {
    return request({
        url: '/agri/dashboardOverview/list',
        method: 'get',
        params: query
    })
}

export function getDashboardOverview(overviewId) {
    return request({
        url: '/agri/dashboardOverview/' + overviewId,
        method: 'get'
    })
}

export function addDashboardOverview(data) {
    return request({
        url: '/agri/dashboardOverview',
        method: 'post',
        data: data
    })
}

export function updateDashboardOverview(data) {
    return request({
        url: '/agri/dashboardOverview',
        method: 'put',
        data: data
    })
}

export function delDashboardOverview(overviewId) {
    return request({
        url: '/agri/dashboardOverview/' + overviewId,
        method: 'delete'
    })
}
