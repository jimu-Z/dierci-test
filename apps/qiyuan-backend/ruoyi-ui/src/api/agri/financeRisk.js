import request from '@/utils/request'

export function listFinanceRisk(query) {
    return request({
        url: '/agri/financeRisk/list',
        method: 'get',
        params: query
    })
}

export function getFinanceRisk(riskId) {
    return request({
        url: '/agri/financeRisk/' + riskId,
        method: 'get'
    })
}

export function addFinanceRisk(data) {
    return request({
        url: '/agri/financeRisk',
        method: 'post',
        data: data
    })
}

export function updateFinanceRisk(data) {
    return request({
        url: '/agri/financeRisk',
        method: 'put',
        data: data
    })
}

export function delFinanceRisk(riskId) {
    return request({
        url: '/agri/financeRisk/' + riskId,
        method: 'delete'
    })
}

export function getFinanceRiskDashboard() {
    return request({
        url: '/agri/financeRisk/dashboard',
        method: 'get'
    })
}

export function getFinanceRiskOpsDashboard() {
    return request({
        url: '/agri/financeRisk/dashboard/ops',
        method: 'get'
    })
}

export function smartAnalyzeFinanceRisk(riskId) {
    return request({
        url: '/agri/financeRisk/smart/analyze/' + riskId,
        method: 'get'
    })
}
