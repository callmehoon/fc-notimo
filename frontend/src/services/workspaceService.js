import api from './api';

/**
 * 사용자가 속한 모든 워크스페이스 목록을 조회하는 API 호출 함수
 * @returns {Promise<Array<object>>} - 워크스페이스 목록
 */
const getWorkspaces = async () => {
    try {
        const response = await api.get('/workspaces');
        return response.data;
    } catch (error) {
        console.error('Error fetching workspaces:', error);
        throw error;
    }
};

/**
 * 새로운 워크스페이스를 생성하는 API 호출 함수
 * @param {object} workspaceData - 생성할 워크스페이스 데이터
 * @returns {Promise<object>} - 생성된 워크스페이스 정보
 */
const createWorkspace = async (workspaceData) => {
    try {
        const response = await api.post('/workspaces', workspaceData);
        return response.data;
    } catch (error) {
        console.error('Error creating workspace:', error);
        throw error;
    }
};

/**
 * ID로 특정 워크스페이스의 상세 정보를 조회하는 API 호출 함수
 * @param {string} id - 조회할 워크스페이스의 ID
 * @returns {Promise<object>} - 워크스페이스 상세 정보
 */
const getWorkspaceById = async (id) => {
    try {
        const response = await api.get(`/workspaces/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching workspace with id ${id}:`, error);
        throw error;
    }
};

/**
 * 특정 워크스페이스의 정보를 수정하는 API 호출 함수
 * @param {string} id - 수정할 워크스페이스의 ID
 * @param {object} workspaceData - 수정할 워크스페이스 데이터
 * @returns {Promise<object>} - 수정된 워크스페이스 정보
 */
const updateWorkspace = async (id, workspaceData) => {
    try {
        const response = await api.put(`/workspaces/${id}`, workspaceData);
        return response.data;
    } catch (error) {
        console.error(`Error updating workspace with id ${id}:`, error);
        throw error;
    }
};

/**
 * 특정 워크스페이스를 삭제하는 API 호출 함수
 * @param {string} id - 삭제할 워크스페이스의 ID
 * @returns {Promise<void>}
 */
const deleteWorkspace = async (id) => {
    try {
        await api.delete(`/workspaces/${id}`);
    } catch (error) {
        console.error(`Error deleting workspace with id ${id}:`, error);
        throw error;
    }
};

const workspaceService = {
    getWorkspaces,
    createWorkspace,
    getWorkspaceById,
    updateWorkspace,
    deleteWorkspace,
};

export default workspaceService;
