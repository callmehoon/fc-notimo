// src/services/userService.js
import api from './api';

// 사용자 프로필 조회
const getUserProfile = async () => {
    // 임시 목업 데이터 - 백엔드 API가 준비되면 실제 API 호출로 변경
    return new Promise((resolve) => {
        setTimeout(() => {
            const mockProfile = {
                userId: 1,
                userName: '홍길동',
                userEmail: 'hong.gildong@example.com',
                userNumber: '010-1234-5678',
                userRole: 'USER', // 'USER', 'ADMIN'
                accountType: 'local', // 'local', 'social', 'integrated'
                connectedSocial: [], // ['google', 'kakao', 'naver']
                createdAt: '2024-01-15T10:30:00',
                lastLoginAt: '2024-01-20T14:25:00'
            };
            resolve(mockProfile);
        }, 800);
    });

    // 실제 API 호출 (백엔드 준비시 주석 해제)
    // const res = await api.get('/users/profile');
    // return res.data;
};

// 사용자 프로필 업데이트
const updateUserProfile = async (profileData) => {
    // 임시 목업 처리 - 백엔드 API가 준비되면 실제 API 호출로 변경
    return new Promise((resolve) => {
        setTimeout(() => {
            console.log('프로필 업데이트 요청:', profileData);
            resolve({
                success: true,
                message: '프로필이 성공적으로 업데이트되었습니다.'
            });
        }, 1000);
    });

    // 실제 API 호출 (백엔드 준비시 주석 해제)
    // const res = await api.put('/users/profile', {
    //     userName: profileData.userName,
    //     userNumber: profileData.userNumber
    // });
    // return res.data;
};

// 사용자 계정 삭제
const deleteUserAccount = async (password) => {
    // 임시 목업 처리 - 백엔드 API가 준비되면 실제 API 호출로 변경
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (password === 'correctPassword') { // 임시 검증
                console.log('계정 삭제 요청');
                resolve({
                    success: true,
                    message: '계정이 성공적으로 삭제되었습니다.'
                });
            } else {
                reject(new Error('비밀번호가 올바르지 않습니다.'));
            }
        }, 1200);
    });

    // 실제 API 호출 (백엔드 준비시 주석 해제)
    // const res = await api.delete('/users/account', {
    //     data: { password }
    // });
    // return res.data;
};

// 계정 통합 관련 함수들
const sendAccountIntegrationCode = async (email) => {
    // 임시 목업 처리
    return new Promise((resolve) => {
        setTimeout(() => {
            console.log('계정 통합 인증 코드 발송:', email);
            resolve({
                success: true,
                message: '인증 코드가 발송되었습니다.'
            });
        }, 1000);
    });
};

const verifyAccountIntegrationCode = async (email, verificationCode) => {
    // 임시 목업 처리
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (verificationCode === '123456') {
                resolve({
                    success: true,
                    message: '인증 코드가 확인되었습니다.'
                });
            } else {
                reject(new Error('인증 코드가 올바르지 않습니다.'));
            }
        }, 800);
    });
};

const completeAccountIntegration = async (email, verificationCode, password) => {
    // 임시 목업 처리
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (verificationCode === '123456') {
                console.log('계정 통합 완료:', { email, password });
                resolve({
                    success: true,
                    message: '계정 통합이 완료되었습니다.'
                });
            } else {
                reject(new Error('인증 코드가 올바르지 않습니다.'));
            }
        }, 1200);
    });
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