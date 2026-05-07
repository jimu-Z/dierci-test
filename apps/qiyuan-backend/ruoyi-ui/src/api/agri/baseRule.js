import request from '@/utils/request'

// 查询基础规则配置列表
export function listBaseRule(query) {
    return request({
        url: '/agri/baseRule/list',
        method: 'get',
        params: query
    })
}

// 查询基础规则配置详细
export function getBaseRule(ruleId) {
    return request({
        url: '/agri/baseRule/' + ruleId,
        method: 'get'
    })
}

// 新增基础规则配置
export function addBaseRule(data) {
    return request({
        url: '/agri/baseRule',
        method: 'post',
        data: data
    })
}

// 修改基础规则配置
export function updateBaseRule(data) {
    return request({
        url: '/agri/baseRule',
        method: 'put',
        data: data
    })
}

// 删除基础规则配置
export function delBaseRule(ruleId) {
    return request({
        url: '/agri/baseRule/' + ruleId,
        method: 'delete'
    })
}

// 导出基础规则配置
export function exportBaseRule(query) {
    return request({
        url: '/agri/baseRule/export',
        method: 'post',
        params: query
    })
}
