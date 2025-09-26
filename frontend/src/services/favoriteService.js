// src/services/favoriteService.js
import api from './api';

/**
 * 즐겨찾기 템플릿 목록
 * @param {number|string} workspaceId
 * @param {'PUBLIC'|'INDIVIDUAL'|string|null} templateType
 * @param {{page:number,size:number}} pageable
 */
export const getFavoriteTemplates = (workspaceId, templateType, pageable) => {
    const params = {
        page: pageable.page || 0,
        size: pageable.size || 10
    };

    // templateType이 있으면 추가
    if (templateType) {
        params.templateType = templateType;
    }

    return api.get(`/workspace/${workspaceId}/favorites`, {
        params: params
    });
};

/**
 * 공용 템플릿 즐겨찾기 추가
 * @param {number|string} workspaceId
 * @param {number|string} publicTemplateId
 */
export const addPublicTemplateToFavorites = (workspaceId, publicTemplateId) => {
    return api.post('/public/favorite', {
        workspaceId: parseInt(workspaceId),
        publicTemplateId: parseInt(publicTemplateId)
    });
};

/**
 * 개인 템플릿 즐겨찾기 추가
 * @param {number|string} workspaceId
 * @param {number|string} individualTemplateId
 */
export const addIndividualTemplateToFavorites = (workspaceId, individualTemplateId) => {
    return api.post('/individual/favorite', {
        workspaceId: parseInt(workspaceId),
        individualTemplateId: parseInt(individualTemplateId)
    });
};

/**
 * 즐겨찾기 제거 (favoriteId가 필요함)
 * 먼저 즐겨찾기 목록에서 해당 템플릿의 favoriteId를 찾아야 함
 */
export const removeFavorite = (favoriteId) => {
    return api.delete(`/favorites/${favoriteId}`);
};

/**
 * 공용 템플릿 즐겨찾기 제거 (헬퍼 함수)
 * @param {number|string} workspaceId
 * @param {number|string} publicTemplateId
 */
export const removePublicTemplateFromFavorites = async (workspaceId, publicTemplateId) => {
    try {
        // 먼저 즐겨찾기 목록에서 해당 템플릿의 favoriteId를 찾음
        const favorites = await getFavoriteTemplates(workspaceId, 'PUBLIC', { page: 0, size: 1000 });
        const favorite = favorites.data.content?.find(fav => fav.publicTemplateId === parseInt(publicTemplateId));

        if (favorite) {
            return removeFavorite(favorite.favoriteId);
        }
        throw new Error('즐겨찾기 항목을 찾을 수 없습니다.');
    } catch (error) {
        throw error;
    }
};

/**
 * 개인 템플릿 즐겨찾기 제거 (헬퍼 함수)
 * @param {number|string} workspaceId
 * @param {number|string} individualTemplateId
 */
export const removeIndividualTemplateFromFavorites = async (workspaceId, individualTemplateId) => {
    try {
        // 먼저 즐겨찾기 목록에서 해당 템플릿의 favoriteId를 찾음
        const favorites = await getFavoriteTemplates(workspaceId, 'INDIVIDUAL', { page: 0, size: 1000 });
        const favorite = favorites.data.content?.find(fav => fav.individualTemplateId === parseInt(individualTemplateId));

        if (favorite) {
            return removeFavorite(favorite.favoriteId);
        }
        throw new Error('즐겨찾기 항목을 찾을 수 없습니다.');
    } catch (error) {
        throw error;
    }
};
