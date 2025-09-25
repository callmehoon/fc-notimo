// src/services/individualTemplateService.js
import api from './api';

/**
 * 나의 템플릿 목록 (page, size, sortType, direction, status, q)
 * DTO: IndividualTemplatePageableRequest
 */
export const listMyTemplates = ({ workspaceId, page, size, sortType, direction, status, q }) => {
    const params = { page, size, sortType, direction };
    if (status) params.status = status;
    if (q) params.q = q; // 검색 파라미터명은 Swagger에서 확인
    return api.get(`/${workspaceId}/templates`, { params });
};

/** 단건 조회 */
export const getMyTemplate = (workspaceId, templateId) =>
    api.get(`/${workspaceId}/templates/${templateId}`);

/** 생성 (AI 결과 저장 / 새 템플릿 만들기) */
export const createMyTemplate = (workspaceId, payload) =>
    api.post(`/templates/${workspaceId}`, payload);

/** 수정 */
export const updateMyTemplate = (workspaceId, templateId, payload) =>
    api.put(`/${workspaceId}/templates/${templateId}`, payload);

/** 삭제 */
export const deleteMyTemplate = (workspaceId, templateId) =>
    api.delete(`/${workspaceId}/templates/${templateId}`);

/** 공유 (엔드포인트는 Swagger에서 확인) */
export const shareMyTemplate = (workspaceId, templateId) =>
    api.post(`/${workspaceId}/templates/${templateId}/share`);
