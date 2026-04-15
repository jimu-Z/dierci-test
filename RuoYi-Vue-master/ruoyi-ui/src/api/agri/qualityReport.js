import request from '@/utils/request'

export function listQualityReport(query) {
    return request({
        url: '/agri/qualityReport/list',
        method: 'get',
        params: query
    })
}

export function getQualityReport(reportId) {
    return request({
        url: '/agri/qualityReport/' + reportId,
        method: 'get'
    })
}

export function addQualityReport(data) {
    return request({
        url: '/agri/qualityReport',
        method: 'post',
        data: data
    })
}

export function updateQualityReport(data) {
    return request({
        url: '/agri/qualityReport',
        method: 'put',
        data: data
    })
}

export function generateQualityReport(inspectId) {
    return request({
        url: '/agri/qualityReport/generate/' + inspectId,
        method: 'post'
    })
}

export function delQualityReport(reportId) {
    return request({
        url: '/agri/qualityReport/' + reportId,
        method: 'delete'
    })
}
