import api from './api';

// 로그인 성공 후 토큰을 저장하고 API 헤더를 설정하는 함수
const handleLoginSuccess = (accessToken, refreshToken) => {
    // 토큰을 localStorage에 저장
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    // 모든 API 요청 헤더에 Access Token을 기본으로 포함시킴
    api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
};

// 로그아웃 시 토큰을 삭제하고 API 헤더를 초기화하는 함수
const handleLogout = () => {
    // localStorage에서 토큰 삭제
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    // API 헤더에서 Authorization 정보 삭제
    delete api.defaults.headers.common['Authorization'];
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
        const { token: accessToken, refreshToken } = response.data;

        if (accessToken && refreshToken) {
            handleLoginSuccess(accessToken, refreshToken);
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

const authService = {
    login,
    signup,
    sendVerificationCode,
    logout: handleLogout,
};

export default authService;
