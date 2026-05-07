import request from '@/utils/request'

export function listAttestationVerify(query) {
    return request({
        url: '/agri/attestationVerify/list',
        method: 'get',
        params: query
    })
}

export function getAttestationVerify(verifyId) {
    return request({
        url: '/agri/attestationVerify/' + verifyId,
        method: 'get'
    })
}

export function addAttestationVerify(data) {
    return request({
        url: '/agri/attestationVerify',
        method: 'post',
        data: data
    })
}

export function updateAttestationVerify(data) {
    return request({
        url: '/agri/attestationVerify',
        method: 'put',
        data: data
    })
}

export function delAttestationVerify(verifyId) {
    return request({
        url: '/agri/attestationVerify/' + verifyId,
        method: 'delete'
    })
}

export function getAttestationVerifyDashboard() {
    return request({
        url: '/agri/attestationVerify/dashboard',
        method: 'get'
    })
}

export function smartVerifyAttestation(verifyId) {
    return request({
        url: '/agri/attestationVerify/smart/verify/' + verifyId,
        method: 'get'
    })
}

export function getAttestationOpsOverview() {
    return request({
        url: '/agri/attestationVerify/ops/overview',
        method: 'get'
    })
}

export function resolveAttestation(verifyId, data) {
    return request({
        url: '/agri/attestationVerify/smart/resolve/' + verifyId,
        method: 'post',
        data: data
    })
}
