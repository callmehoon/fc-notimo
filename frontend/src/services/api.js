// src/services/api.js
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
});

/** ================================
 * 요청 인터셉터: accessToken 자동 첨부
 * ================================ */
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) config.headers['Authorization'] = `Bearer ${token}`;
        return config;
    },
    (error) => Promise.reject(error)
);

/** ================================
 * 응답 인터셉터: 401 처리 (토큰 갱신 큐)
 *  - 이 로직은 "여기에서만" 등록하세요.
 *  - 다른 파일에 같은 코드가 있으면 삭제!
 * ================================ */
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach((prom) => {
        if (error) prom.reject(error);
        else prom.resolve(token);
    });
    failedQueue = [];
};

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error?.config;

        // 네트워크 오류 등 방어
        if (!error.response) {
            return Promise.reject(error);
        }

        // 401 & 재시도 플래그 없을 때 & 로그인 요청이 아닐 때만 수행
        const isLoginRequest = originalRequest.url.endsWith('/auth/login');
        if (error.response.status === 401 && !originalRequest._retry && !isLoginRequest) {
            // 이미 갱신 중이면 큐에 대기
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers['Authorization'] = 'Bearer ' + token;
                        return api(originalRequest);
                    })
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // 순환 참조를 피하기 위해 직접 토큰 갱신 API 호출
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) {
                    throw new Error('No refresh token available');
                }

                const refreshResponse = await axios.post(
                    `${API_BASE_URL}/auth/refresh`,
                    {},
                    { headers: { Authorization: `Bearer ${refreshToken}` } }
                );

                const { accessToken: newAccessToken, refreshToken: newRefreshToken } = refreshResponse.data;

                if (newAccessToken && newRefreshToken) {
                    localStorage.setItem('accessToken', newAccessToken);
                    localStorage.setItem('refreshToken', newRefreshToken);
                }

                // 기본 헤더 및 재요청 헤더 갱신
                api.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
                originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;

                processQueue(null, newAccessToken);
                return api(originalRequest);
            } catch (refreshError) {
                processQueue(refreshError, null);
                // 토큰 정리
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('userRole');
                window.location.href = '/login';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default api;
