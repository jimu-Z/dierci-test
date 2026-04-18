import request from '@/utils/request'

export function listUserAccess(query) {
    return request({
        url: '/agri/userAccess/list',
        method: 'get',
        params: query
    })
}

export function getUserAccess(grantId) {
    return request({
        url: '/agri/userAccess/' + grantId,
        method: 'get'
    })
}

export function addUserAccess(data) {
    return request({
        url: '/agri/userAccess',
        method: 'post',
        data: data
    })
}

export function updateUserAccess(data) {
    return request({
        url: '/agri/userAccess',
        method: 'put',
        data: data
    })
}

export function grantUserAccess(grantId) {
    return request({
        url: '/agri/userAccess/grant/' + grantId,
        method: 'put'
    })
}

export function delUserAccess(grantId) {
    return request({
        url: '/agri/userAccess/' + grantId,
        method: 'delete'
    })
}

export function getUserAccessDashboard() {
    return request({
        url: '/agri/userAccess/dashboard',
        method: 'get'
    })
}

export function smartRecommendUserAccess(grantId) {
    return request({
        url: '/agri/userAccess/smart/recommend/' + grantId,
        method: 'get'
    })
}
