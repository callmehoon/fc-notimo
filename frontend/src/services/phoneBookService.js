import api from './api';

// 주소록 생성
const createPhoneBook = async (workspaceId, phoneBookData) => {
    try {
        const response = await api.post(`/workspaces/${workspaceId}/phonebooks`, phoneBookData);
        return response.data;
    } catch (error) {
        console.error('Error creating phonebook:', error);
        throw error;
    }
};

// 주소록 목록 조회
const getPhoneBooks = async (workspaceId) => {
    try {
        const response = await api.get(`/workspaces/${workspaceId}/phonebooks`);
        return response.data;
    } catch (error) {
        console.error('Error fetching phonebooks:', error);
        throw error;
    }
};

// 주소록에 수신자 일괄 추가
const addRecipientsToPhoneBook = async (workspaceId, phoneBookId, recipientIdList) => {
    try {
        console.log('API Call:', {
            url: `/workspaces/${workspaceId}/phonebooks/${phoneBookId}/recipients`,
            data: { recipientIdList },
            workspaceId,
            phoneBookId,
            recipientIdList
        });

        const response = await api.post(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}/recipients`, {
            recipientIds: recipientIdList
        });
        return response.data;
    } catch (error) {
        console.error('Error adding recipients to phonebook:', {
            message: error.message,
            status: error.response?.status,
            statusText: error.response?.statusText,
            data: error.response?.data,
            url: error.config?.url,
            method: error.config?.method,
            requestData: error.config?.data
        });
        throw error;
    }
};

// 특정 주소록의 수신자 목록 조회
const getRecipientsInPhoneBook = async (workspaceId, phoneBookId, pageable = {}) => {
    try {
        const response = await api.get(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}/recipients`, { params: pageable });
        return response.data;
    } catch (error) {
        console.error('Error fetching recipients in phonebook:', error);
        throw error;
    }
};

// 주소록 수정
const updatePhoneBook = async (workspaceId, phoneBookId, phoneBookData) => {
    try {
        const response = await api.put(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}`, phoneBookData);
        return response.data;
    } catch (error) {
        console.error('Error updating phonebook:', error);
        throw error;
    }
};

// 주소록 삭제 (소프트 삭제)
const deletePhoneBook = async (workspaceId, phoneBookId) => {
    try {
        const response = await api.delete(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting phonebook:', error);
        throw error;
    }
};

// 주소록에서 수신자 일괄 삭제
const deleteRecipientsFromPhoneBook = async (workspaceId, phoneBookId, recipientIds) => {
    try {
        const response = await api.delete(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}/recipients`, {
            data: { recipientIds: recipientIds }
        });
        return response.data;
    } catch (error) {
        console.error('Error deleting recipients from phonebook:', error);
        throw error;
    }
};

const phoneBookService = {
    createPhoneBook,
    getPhoneBooks,
    addRecipientsToPhoneBook,
    getRecipientsInPhoneBook,
    updatePhoneBook,
    deletePhoneBook,
    deleteRecipientsFromPhoneBook,
};

export default phoneBookService;