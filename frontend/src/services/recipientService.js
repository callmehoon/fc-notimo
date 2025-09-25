import api from './api';

const getRecipients = async (workspaceId, pageable) => {
    try {
        const response = await api.get(`/workspaces/${workspaceId}/recipients`, { params: pageable });
        return response.data;
    } catch (error) {
        console.error('Error fetching recipients:', error);
        throw error;
    }
};

const createRecipient = async (workspaceId, recipientData) => {
    try {
        const response = await api.post(`/workspaces/${workspaceId}/recipients`, recipientData);
        return response.data;
    } catch (error) {
        console.error('Error creating recipient:', error);
        throw error;
    }
};

const recipientService = {
    getRecipients,
    createRecipient,
};

export default recipientService;
