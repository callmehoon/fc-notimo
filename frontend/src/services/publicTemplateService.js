// src/services/publicTemplateService.js
import api from './api';

export const getPublicTemplates = (pageable) => {
    return api.get('/public-templates', { params: pageable });
};

export const createIndividualTemplateFromPublic = (workspaceId, publicTemplateId) => {
    return api.post(`/templates/${workspaceId}/from-public/${publicTemplateId}`);
};

export const deletePublicTemplate = (templateId) => {
    return api.delete(`/admin/public-templates/${templateId}`);
};
