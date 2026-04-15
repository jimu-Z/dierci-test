import request from '@/utils/request'

export function listWarningSummary(query) {
    return request({
        url: '/agri/warningSummary/list',
        method: 'get',
        params: query
    })
}

export function getWarningSummary(summaryId) {
    return request({
        url: '/agri/warningSummary/' + summaryId,
        method: 'get'
    })
}

export function addWarningSummary(data) {
    return request({
        url: '/agri/warningSummary',
        method: 'post',
        data: data
    })
}

export function updateWarningSummary(data) {
    return request({
        url: '/agri/warningSummary',
        method: 'put',
        data: data
    })
}

export function delWarningSummary(summaryId) {
    return request({
        url: '/agri/warningSummary/' + summaryId,
        method: 'delete'
    })
}
