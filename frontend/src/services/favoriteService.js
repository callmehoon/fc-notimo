// src/services/favoriteService.js
import api from './api';

/**
 * 즐겨찾기 템플릿 목록
 * @param {number|string} workspaceId
 * @param {'PUBLIC'|'INDIVIDUAL'|string|null} templateType
 * @param {{page:number,size:number,sortType?:string,direction?:string}} pageable
 */
export const getFavoriteTemplates = (workspaceId, templateType, pageable) => {
    return api.get(`/workspace/${workspaceId}/favorites`, {
        params: { ...pageable, templateType },
    });
};
