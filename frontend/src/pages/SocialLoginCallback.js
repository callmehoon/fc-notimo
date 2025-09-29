import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Box, CircularProgress, Typography, Alert } from '@mui/material';
import authService from '../services/authService';

const SocialLoginCallback = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const handleCallback = async () => {
            try {
                // URL 파라미터에서 소셜 로그인 결과 확인
                const token = searchParams.get('token');
                const refreshToken = searchParams.get('refreshToken');
                const userRole = searchParams.get('userRole');
                const isExisting = searchParams.get('isExisting');
                const errorParam = searchParams.get('error');

                // 소셜 로그인 임시 정보
                const provider = searchParams.get('provider');
                const email = searchParams.get('email');
                const name = searchParams.get('name');

                if (errorParam) {
                    setError('소셜 로그인 중 오류가 발생했습니다: ' + errorParam);
                    setLoading(false);
                    return;
                }

                if (token && refreshToken && isExisting === 'true') {
                    // 기존 사용자 로그인 성공
                    localStorage.setItem('accessToken', token);
                    localStorage.setItem('refreshToken', refreshToken);
                    localStorage.setItem('userRole', userRole);
                    navigate('/workspace');
                } else if (provider && email && name && isExisting === 'false') {
                    // 신규 사용자 - 회원가입 완료 페이지로 이동
                    navigate('/social-signup', {
                        state: {
                            provider,
                            email,
                            name,
                            socialId: searchParams.get('socialId')
                        }
                    });
                } else {
                    setError('소셜 로그인 정보가 올바르지 않습니다.');
                }
            } catch (error) {
                console.error('소셜 로그인 콜백 처리 오류:', error);
                setError('소셜 로그인 처리 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        handleCallback();
    }, [searchParams, navigate]);

    if (loading) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: '100vh',
                    gap: 2
                }}
            >
                <CircularProgress />
                <Typography variant="h6">로그인 처리 중...</Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: '100vh',
                    gap: 2,
                    padding: 2
                }}
            >
                <Alert severity="error" sx={{ maxWidth: 400 }}>
                    {error}
                </Alert>
                <Typography
                    variant="body2"
                    color="primary"
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate('/login')}
                >
                    로그인 페이지로 돌아가기
                </Typography>
            </Box>
        );
    }

    return null;
};

export default SocialLoginCallback;