import request from '@/utils/request'

export function listSmartContract(query) {
    return request({
        url: '/agri/smartContract/list',
        method: 'get',
        params: query
    })
}

export function getSmartContract(deployId) {
    return request({
        url: '/agri/smartContract/' + deployId,
        method: 'get'
    })
}

export function addSmartContract(data) {
    return request({
        url: '/agri/smartContract',
        method: 'post',
        data: data
    })
}

export function updateSmartContract(data) {
    return request({
        url: '/agri/smartContract',
        method: 'put',
        data: data
    })
}

export function deploySmartContract(deployId) {
    return request({
        url: '/agri/smartContract/deploy/' + deployId,
        method: 'put'
    })
}

export function delSmartContract(deployId) {
    return request({
        url: '/agri/smartContract/' + deployId,
        method: 'delete'
    })
}

export function getSmartContractDashboard() {
    return request({
        url: '/agri/smartContract/dashboard',
        method: 'get'
    })
}

export function getSmartContractDashboardOps() {
    return request({
        url: '/agri/smartContract/dashboard/ops',
        method: 'get'
    })
}

export function smartSecurityContract(deployId) {
    return request({
        url: '/agri/smartContract/smart/security/' + deployId,
        method: 'get'
    })
}
