import request from '@/utils/request'

export function listFarmOp(query) {
    return request({
        url: '/agri/farmOp/list',
        method: 'get',
        params: query
    })
}

export function getFarmOp(operationId) {
    return request({
        url: '/agri/farmOp/' + operationId,
        method: 'get'
    })
}

export function addFarmOp(data) {
    return request({
        url: '/agri/farmOp',
        method: 'post',
        data: data
    })
}

export function updateFarmOp(data) {
    return request({
        url: '/agri/farmOp',
        method: 'put',
        data: data
    })
}

export function delFarmOp(operationId) {
    return request({
        url: '/agri/farmOp/' + operationId,
        method: 'delete'
    })
}
