import request from '@/utils/request'

export function listThirdApi(query) {
    return request({
        url: '/agri/thirdApi/list',
        method: 'get',
        params: query
    })
}

export function getThirdApi(accessId) {
    return request({
        url: '/agri/thirdApi/' + accessId,
        method: 'get'
    })
}

export function addThirdApi(data) {
    return request({
        url: '/agri/thirdApi',
        method: 'post',
        data: data
    })
}

export function updateThirdApi(data) {
    return request({
        url: '/agri/thirdApi',
        method: 'put',
        data: data
    })
}

export function delThirdApi(accessId) {
    return request({
        url: '/agri/thirdApi/' + accessId,
        method: 'delete'
    })
}

export function probeThirdApi(accessId) {
    return request({
        url: '/agri/thirdApi/probe/' + accessId,
        method: 'post'
    })
}
