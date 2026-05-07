import request from '@/utils/request'

export function listTodoTaskReminder(query) {
    return request({
        url: '/agri/todoTaskReminder/list',
        method: 'get',
        params: query
    })
}

export function getTodoTaskReminder(reminderId) {
    return request({
        url: '/agri/todoTaskReminder/' + reminderId,
        method: 'get'
    })
}

export function getTodoTaskReminderDashboard() {
    return request({
        url: '/agri/todoTaskReminder/dashboard',
        method: 'get'
    })
}

export function smartDispatchTodoTaskReminder(reminderId) {
    return request({
        url: '/agri/todoTaskReminder/smart/dispatch/' + reminderId,
        method: 'get'
    })
}

export function addTodoTaskReminder(data) {
    return request({
        url: '/agri/todoTaskReminder',
        method: 'post',
        data: data
    })
}

export function updateTodoTaskReminder(data) {
    return request({
        url: '/agri/todoTaskReminder',
        method: 'put',
        data: data
    })
}

export function delTodoTaskReminder(reminderId) {
    return request({
        url: '/agri/todoTaskReminder/' + reminderId,
        method: 'delete'
    })
}
