import request from '@/utils/request'

export function listLogisticsTrack(query) {
    return request({
        url: '/agri/logisticsTrack/list',
        method: 'get',
        params: query
    })
}

export function getLogisticsTrack(trackId) {
    return request({
        url: '/agri/logisticsTrack/' + trackId,
        method: 'get'
    })
}

export function addLogisticsTrack(data) {
    return request({
        url: '/agri/logisticsTrack',
        method: 'post',
        data: data
    })
}

export function updateLogisticsTrack(data) {
    return request({
        url: '/agri/logisticsTrack',
        method: 'put',
        data: data
    })
}

export function delLogisticsTrack(trackId) {
    return request({
        url: '/agri/logisticsTrack/' + trackId,
        method: 'delete'
    })
}

export function listLogisticsTimeline(traceCode) {
    return request({
        url: '/agri/logisticsTrack/timeline/' + traceCode,
        method: 'get'
    })
}

export function getLogisticsTrackSummary(traceCode) {
    return request({
        url: '/agri/logisticsTrack/summary/' + traceCode,
        method: 'get'
    })
}

export function getLogisticsTrackDashboardOps() {
    return request({
        url: '/agri/logisticsTrack/dashboard/ops',
        method: 'get'
    })
}

export function smartAnalyzeLogisticsTrack(trackId) {
    return request({
        url: '/agri/logisticsTrack/smart/analyze/' + trackId,
        method: 'get'
    })
}
