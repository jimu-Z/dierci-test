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
