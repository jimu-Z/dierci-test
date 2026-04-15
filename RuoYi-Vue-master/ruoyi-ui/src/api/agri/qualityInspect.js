import request from '@/utils/request'

export function listQualityInspect(query) {
    return request({
        url: '/agri/qualityInspect/list',
        method: 'get',
        params: query
    })
}

export function getQualityInspect(inspectId) {
    return request({
        url: '/agri/qualityInspect/' + inspectId,
        method: 'get'
    })
}

export function addQualityInspect(data) {
    return request({
        url: '/agri/qualityInspect',
        method: 'post',
        data: data
    })
}

export function updateQualityInspect(data) {
    return request({
        url: '/agri/qualityInspect',
        method: 'put',
        data: data
    })
}

export function feedbackQualityInspect(data) {
    return request({
        url: '/agri/qualityInspect/feedback',
        method: 'put',
        data: data
    })
}

export function delQualityInspect(inspectId) {
    return request({
        url: '/agri/qualityInspect/' + inspectId,
        method: 'delete'
    })
}
