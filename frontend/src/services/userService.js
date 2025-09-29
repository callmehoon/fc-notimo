// src/services/userService.js
import api from './api';

// 사용자 프로필 조회
const getUserProfile = async () => {
    try {
        const res = await api.get('/users/profile');
        const profile = res.data;

        // 백엔드 응답을 프론트엔드 형식에 맞게 변환
        const transformedProfile = {
            userId: profile.userId,
            userName: profile.userName,
            userEmail: profile.userEmail,
            userNumber: profile.userNumber,
            userRole: profile.userRole,
            createdAt: profile.createdAt,
            lastLoginAt: profile.createdAt, // 백엔드에 lastLoginAt이 없으므로 createdAt으로 대체
            // 계정 타입 결정 로직
            accountType: determineAccountType(profile.authMethods),
            // 연결된 소셜 계정 목록
            connectedSocial: profile.authMethods?.socialMethods || []
        };

        return transformedProfile;
    } catch (error) {
        console.error('프로필 조회 실패:', error);
        throw error;
    }
};

// 계정 타입 결정 함수
const determineAccountType = (authMethods) => {
    if (!authMethods) return 'local';

    const hasLocal = authMethods.hasLocalAuth;
    const hasSocial = authMethods.socialMethods && authMethods.socialMethods.length > 0;

    if (hasLocal && hasSocial) {
        return 'integrated'; // 로컬 + 소셜 통합 계정
    } else if (hasSocial) {
        return 'social'; // 소셜 전용 계정
    } else {
        return 'local'; // 로컬 전용 계정
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
const deleteUserAccount = async (password, confirmText = '회원탈퇴') => {
    try {
        console.log('회원탈퇴 요청:', { password: '***', confirmText });
        const res = await api.delete('/users/account', {
            data: {
                password,
                confirmText
            }
        });
        return res.data;
    } catch (error) {
        console.error('계정 삭제 실패:', error);
        console.error('응답 데이터:', error.response?.data);
        throw error;
    }
};

// 계정 통합 관련 함수들 (새로운 전용 엔드포인트 사용)
const sendAccountIntegrationCode = async (email) => {
    try {
        const res = await api.post('/auth/send-account-merge-code', { email });
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

// 비밀번호 변경
const changePassword = async (currentPassword, newPassword) => {
    try {
        const res = await api.put('/users/password', {
            currentPassword,
            newPassword
        });
        return res.data;
    } catch (error) {
        console.error('비밀번호 변경 실패:', error);
        throw error;
    }
};

const userService = {
    getUserProfile,
    updateUserProfile,
    deleteUserAccount,
    changePassword,
    sendAccountIntegrationCode,
    verifyAccountIntegrationCode,
    completeAccountIntegration,
};

export default userService;