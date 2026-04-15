import request from '@/utils/request'

export function listSupplyContract(query) {
    return request({
        url: '/agri/supplyContract/list',
        method: 'get',
        params: query
    })
}

export function getSupplyContract(contractId) {
    return request({
        url: '/agri/supplyContract/' + contractId,
        method: 'get'
    })
}

export function addSupplyContract(data) {
    return request({
        url: '/agri/supplyContract',
        method: 'post',
        data: data
    })
}

export function updateSupplyContract(data) {
    return request({
        url: '/agri/supplyContract',
        method: 'put',
        data: data
    })
}

export function delSupplyContract(contractId) {
    return request({
        url: '/agri/supplyContract/' + contractId,
        method: 'delete'
    })
}
