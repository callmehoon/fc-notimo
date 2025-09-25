import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Axios 요청 인터셉터 추가
api.interceptors.request.use(
    (config) => {
        // localStorage에서 accessToken 가져오기
        const token = localStorage.getItem('accessToken');

        // 토큰이 있으면 헤더에 추가
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        // 요청 에러 처리
        return Promise.reject(error);
    }
);

export const getPublicTemplates = (pageable) => {
    return api.get('/public-templates', { params: pageable });
};

export const createIndividualTemplateFromPublic = (workspaceId, publicTemplateId) => {
    return api.post(`/templates/${workspaceId}/from-public/${publicTemplateId}`);
};

export const getIndividualTemplate = (workspaceId, templateId) => {
    return api.get(`/${workspaceId}/templates/${templateId}`);
};

export const deletePublicTemplate = (templateId) => {
    return api.delete(`/admin/public-templates/${templateId}`);
};

export const getFavoriteTemplates = (workspaceId, templateType, pageable) => {
    return api.get(`/workspace/${workspaceId}/favorites`, { params: { ...pageable, templateType } });
};

export default api;
