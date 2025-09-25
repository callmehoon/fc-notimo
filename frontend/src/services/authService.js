import api from './api';

// 로그인 성공 후 토큰을 저장하는 함수
const handleLoginSuccess = (accessToken, refreshToken, userRole) => {
    // 토큰을 localStorage에 저장
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userRole', userRole);
};

// 로그아웃 시 토큰을 삭제하는 함수
const handleLogout = () => {
    // localStorage에서 토큰 삭제
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userRole');
};

/**
 * 로그인 API 호출 함수
 * @param {string} email - 사용자 이메일
 * @param {string} password - 사용자 비밀번호
 * @returns {Promise<object>} - 로그인 응답 데이터
 */
const login = async (email, password) => {
    try {
        const response = await api.post('/auth/login', { email, password });
        const { token: accessToken, refreshToken, userRole } = response.data;

        if (accessToken && refreshToken) {
            handleLoginSuccess(accessToken, refreshToken, userRole);
        }

        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * 회원가입 API 호출 함수
 * @param {object} userData - 회원가입 폼 데이터 (userName, email, userNumber, password, verificationCode)
 * @returns {Promise<object>} - 회원가입 응답 데이터
 */
const signup = async (userData) => {
    try {
        const response = await api.post('/auth/signup', userData);
        return response.data;
    } catch (error) {
        throw error;
    }
};

/**
 * 이메일 인증 코드 발송 API 호출 함수
 * @param {string} email - 인증 코드를 받을 이메일 주소
 * @returns {Promise<object>} - API 응답 데이터
 */
const sendVerificationCode = async (email) => {
    try {
        const response = await api.post('/auth/send-verification-code', { email });
        return response.data;
    } catch (error) {
        throw error;
    }
};

const refreshToken = async () => {
    try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await api.post('/auth/refresh', {}, {
            headers: { 'Authorization': `Bearer ${refreshToken}` }
        });
        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data;
        if (newAccessToken && newRefreshToken) {
            localStorage.setItem('accessToken', newAccessToken);
            localStorage.setItem('refreshToken', newRefreshToken);
        }
        return newAccessToken;
    } catch (error) {
        throw error;
    }
};

const authService = {
    login,
    signup,
    sendVerificationCode,
    logout: handleLogout,
    refreshToken,
};

export default authService;
