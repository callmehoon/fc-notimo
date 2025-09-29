// src/services/authService.js
import api from './api';

// 로그인 성공 후 토큰 저장
const handleLoginSuccess = (accessToken, refreshToken, userRole) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userRole', userRole);
};

// 로그아웃 (로컬 토큰 삭제)
const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userRole');
};

/** 로그인 */
const login = async (email, password) => {
    try {
        // 로그인 시에는 기존 토큰 없이 호출
        const res = await api.post('/auth/login', { email, password }, {
            headers: { Authorization: undefined }
        });
        const { token: accessToken, refreshToken, userRole } = res.data;
        if (accessToken && refreshToken) {
            handleLoginSuccess(accessToken, refreshToken, userRole);
        }
        return res.data;
    } catch (error) {
        console.error('로그인 실패:', error);
        console.error('에러 전체 객체:', error);
        console.error('응답 객체:', error.response);
        console.error('응답 데이터:', error.response?.data);
        console.error('응답 상태:', error.response?.status);
        console.error('네트워크 에러 여부:', !error.response);

        // 회원탈퇴된 계정에 대한 특별한 메시지 처리
        const errorMessage = (error.response?.data?.message || error.message || '').toLowerCase();
        const errorCode = (error.response?.data?.code || '').toUpperCase();

        const isDeletedAccountError = () => {
            const keywords = [
                '탈퇴', '비활성화', '삭제', 'deactivated', 'deleted', 'withdrawn',
                'inactive', 'soft delete', '계정이', '존재하지', '찾을 수 없',
                'not found', 'user not'
            ];
            const errorCodes = ['USER_NOT_FOUND', 'ACCOUNT_DELETED'];

            return keywords.some(keyword => errorMessage.includes(keyword)) ||
                   errorCodes.some(code => errorCode.includes(code));
        };

        if ((error.response?.status === 401 || !error.response) && isDeletedAccountError()) {
            const customError = new Error('이미 탈퇴한 계정입니다.\n같은 이메일로 새로 회원가입하시거나 다른 계정을 이용해주세요.');
            customError.isDeletedAccount = true;
            customError.originalError = error;
            throw customError;
        }

        throw error;
    }
};

/** 회원가입 */
const signup = async (userData) => {
    const res = await api.post('/auth/signup', userData);
    return res.data;
};

/** 이메일 인증 코드 발송 */
const sendVerificationCode = async (email) => {
    const res = await api.post('/auth/send-verification-code', { email });
    return res.data;
};

/** 액세스 토큰 갱신 */
const refreshToken = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    const res = await api.post(
        '/auth/refresh',
        {},
        { headers: { Authorization: `Bearer ${refreshToken}` } }
    );
    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = res.data;

    if (newAccessToken && newRefreshToken) {
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
    }
    return newAccessToken;
};

/** 소셜 로그인 제공자 목록 조회 */
const getSocialProviders = async () => {
    const res = await api.get('/auth/social/providers');
    return res.data;
};

/** 구글 소셜 로그인 시작 */
const loginWithGoogle = () => {
    // 디버깅: 환경변수와 생성된 URL 확인
    const baseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
    const googleAuthUrl = `${baseUrl}/oauth2/authorization/google`;

    console.log('🚀 Google Login 시작');
    console.log('📍 Base URL:', baseUrl);
    console.log('🔗 Google Auth URL:', googleAuthUrl);
    console.log('🌐 현재 환경변수 REACT_APP_API_BASE_URL:', process.env.REACT_APP_API_BASE_URL);

    // 백엔드의 OAuth2 인증 URL로 리다이렉트
    window.location.href = googleAuthUrl;
};

/** 소셜 회원가입 완료 */
const completeSocialSignup = async (socialSignupData) => {
    const res = await api.post('/auth/social/signup', socialSignupData);
    const { accessToken, refreshToken, userRole } = res.data;
    if (accessToken && refreshToken) {
        handleLoginSuccess(accessToken, refreshToken, userRole);
    }
    return res.data;
};

/** 비밀번호 찾기: 인증 코드 발송 */
const sendPasswordResetCode = async (email) => {
    const res = await api.post('/auth/send-password-reset-code', { email });
    return res.data;
};

/** 비밀번호 재설정 */
const resetPassword = async (email, verificationCode, newPassword) => {
    const res = await api.post('/auth/reset-password', {
        email,
        verificationCode,
        newPassword
    });
    return res.data;
};

const authService = {
    login,
    signup,
    sendVerificationCode,
    logout: handleLogout,
    refreshToken,
    getSocialProviders,
    loginWithGoogle,
    completeSocialSignup,
    sendPasswordResetCode,
    resetPassword,
};

export default authService;
