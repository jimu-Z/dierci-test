import request from '@/utils/request'

export function listDataUplink(query) {
    return request({
        url: '/agri/dataUplink/list',
        method: 'get',
        params: query
    })
}

export function getDataUplink(uplinkId) {
    return request({
        url: '/agri/dataUplink/' + uplinkId,
        method: 'get'
    })
}

export function addDataUplink(data) {
    return request({
        url: '/agri/dataUplink',
        method: 'post',
        data: data
    })
}

export function updateDataUplink(data) {
    return request({
        url: '/agri/dataUplink',
        method: 'put',
        data: data
    })
}

export function executeDataUplink(uplinkId) {
    return request({
        url: '/agri/dataUplink/uplink/' + uplinkId,
        method: 'put'
    })
}

export function delDataUplink(uplinkId) {
    return request({
        url: '/agri/dataUplink/' + uplinkId,
        method: 'delete'
    })
}
