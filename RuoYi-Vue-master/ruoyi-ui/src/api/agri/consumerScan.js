import request from '@/utils/request'

export function listConsumerScan(query) {
    return request({
        url: '/agri/consumerScan/list',
        method: 'get',
        params: query
    })
}

export function getConsumerScan(queryId) {
    return request({
        url: '/agri/consumerScan/' + queryId,
        method: 'get'
    })
}

export function addConsumerScan(data) {
    return request({
        url: '/agri/consumerScan',
        method: 'post',
        data: data
    })
}

export function updateConsumerScan(data) {
    return request({
        url: '/agri/consumerScan',
        method: 'put',
        data: data
    })
}

export function scanConsumerTrace(traceCode, data) {
    return request({
        url: '/agri/consumerScan/scan/' + traceCode,
        method: 'post',
        data: data
    })
}

export function delConsumerScan(queryId) {
    return request({
        url: '/agri/consumerScan/' + queryId,
        method: 'delete'
    })
}

export function getConsumerScanDashboard() {
    return request({
        url: '/agri/consumerScan/dashboard',
        method: 'get'
    })
}

export function getConsumerScanOpsDashboard() {
    return request({
        url: '/agri/consumerScan/dashboard/ops',
        method: 'get'
    })
}

export function smartAnalyzeConsumerScan(queryId) {
    return request({
        url: '/agri/consumerScan/smart/analyze/' + queryId,
        method: 'get',
        timeout: 20000
    })
}
