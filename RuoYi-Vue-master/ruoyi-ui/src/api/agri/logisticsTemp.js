import request from '@/utils/request'

export function listLogisticsTemp(query) {
    return request({
        url: '/agri/logisticsTemp/list',
        method: 'get',
        params: query
    })
}

export function getLogisticsTemp(recordId) {
    return request({
        url: '/agri/logisticsTemp/' + recordId,
        method: 'get'
    })
}

export function addLogisticsTemp(data) {
    return request({
        url: '/agri/logisticsTemp',
        method: 'post',
        data: data
    })
}

export function updateLogisticsTemp(data) {
    return request({
        url: '/agri/logisticsTemp',
        method: 'put',
        data: data
    })
}

export function delLogisticsTemp(recordId) {
    return request({
        url: '/agri/logisticsTemp/' + recordId,
        method: 'delete'
    })
}
