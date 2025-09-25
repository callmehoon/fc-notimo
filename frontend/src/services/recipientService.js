import api from './api';

// 수신자 목록 조회 (페이징 지원)
const getRecipients = async (workspaceId, pageable = {}) => {
    try {
        const response = await api.get(`/workspaces/${workspaceId}/recipients`, { params: pageable });
        return response.data;
    } catch (error) {
        console.error('Error fetching recipients:', error);
        throw error;
    }
};

// 수신자 생성
const createRecipient = async (workspaceId, recipientData) => {
    try {
        const response = await api.post(`/workspaces/${workspaceId}/recipients`, recipientData);
        return response.data;
    } catch (error) {
        console.error('Error creating recipient:', error);
        throw error;
    }
};

// 수신자 수정
const updateRecipient = async (workspaceId, recipientId, recipientData) => {
    try {
        const response = await api.put(`/workspaces/${workspaceId}/recipients/${recipientId}`, recipientData);
        return response.data;
    } catch (error) {
        console.error('Error updating recipient:', error);
        throw error;
    }
};

// 수신자 삭제 (소프트 삭제)
const deleteRecipient = async (workspaceId, recipientId) => {
    try {
        const response = await api.delete(`/workspaces/${workspaceId}/recipients/${recipientId}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting recipient:', error);
        throw error;
    }
};

const recipientService = {
    getRecipients,
    createRecipient,
    updateRecipient,
    deleteRecipient,
};

export default recipientService;
