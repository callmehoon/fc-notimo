// src/services/userService.js
import api from './api';

// 사용자 프로필 조회
const getUserProfile = async () => {
    try {
        const res = await api.get('/users/profile');
        return res.data;
    } catch (error) {
        console.error('프로필 조회 실패:', error);
        throw error;
    }
};

// 사용자 프로필 업데이트
const updateUserProfile = async (profileData) => {
    try {
        const res = await api.put('/users/profile', {
            userName: profileData.userName,
            userNumber: profileData.userNumber
        });
        return res.data;
    } catch (error) {
        console.error('프로필 업데이트 실패:', error);
        throw error;
    }
};

// 사용자 계정 삭제
const deleteUserAccount = async (password) => {
    try {
        const res = await api.delete('/users/account', {
            data: {
                password,
                confirmation: '회원탈퇴'
            }
        });
        return res.data;
    } catch (error) {
        console.error('계정 삭제 실패:', error);
        throw error;
    }
};

// 계정 통합 관련 함수들 (기존 구현된 API 사용)
const sendAccountIntegrationCode = async (email) => {
    try {
        const res = await api.post('/auth/send-verification-code', { email });
        return res.data;
    } catch (error) {
        console.error('계정 통합 인증 코드 발송 실패:', error);
        throw error;
    }
};

const verifyAccountIntegrationCode = async (email, verificationCode) => {
    // 검증은 완료 단계에서 한 번에 처리하므로 임시 성공 응답
    return Promise.resolve({
        success: true,
        message: '인증 코드가 확인되었습니다.'
    });
};

const completeAccountIntegration = async (email, verificationCode, password) => {
    try {
        const res = await api.post('/auth/add-local-auth', {
            email,
            verificationCode,
            password
        });
        return res.data;
    } catch (error) {
        console.error('계정 통합 완료 실패:', error);
        throw error;
    }
};

const userService = {
    getUserProfile,
    updateUserProfile,
    deleteUserAccount,
    sendAccountIntegrationCode,
    verifyAccountIntegrationCode,
    completeAccountIntegration,
};

export default userService;