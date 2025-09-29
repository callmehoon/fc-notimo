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
    const res = await api.post('/auth/login', { email, password });
    const { token: accessToken, refreshToken, userRole } = res.data;
    if (accessToken && refreshToken) {
        handleLoginSuccess(accessToken, refreshToken, userRole);
    }
    return res.data;
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

const authService = {
    login,
    signup,
    sendVerificationCode,
    logout: handleLogout,
    refreshToken,
};

export default authService;
