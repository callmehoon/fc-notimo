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
        const response = await api.post(`/workspaces/${workspaceId}/phonebooks/${phoneBookId}/recipients`, {
            recipientIdList: recipientIdList
        });
        return response.data;
    } catch (error) {
        console.error('Error adding recipients to phonebook:', error);
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

const phoneBookService = {
    createPhoneBook,
    getPhoneBooks,
    addRecipientsToPhoneBook,
    getRecipientsInPhoneBook,
};

export default phoneBookService;