import React, { useState, useEffect } from 'react';
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom';
import {Link, Box, Divider, Typography} from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import authService from "../services/authService";

const Login = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
    });
    const [errors, setErrors] = useState({});

    // 소셜 로그인 성공 처리
    useEffect(() => {
        const success = searchParams.get('success');
        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');
        const isNewUser = searchParams.get('isNewUser');
        const accountIntegrated = searchParams.get('accountIntegrated');
        const provider = searchParams.get('provider');
        const error = searchParams.get('error');
        const errorMessage = searchParams.get('message');

        // 소셜 로그인 오류 처리
        if (error) {
            // URL에서 모든 파라미터 제거
            navigate('/login', { replace: true });

            // 소셜 로그인 오류
            alert(errorMessage ? decodeURIComponent(errorMessage) : '소셜 로그인 중 오류가 발생했습니다.');
            return;
        }

        // 소셜 로그인 성공 처리 (기존 사용자)
        if (success === 'true' && accessToken && refreshToken && isNewUser === 'false') {
            // sessionStorage로 중복 처리 방지
            const socialLoginKey = `social_login_${Date.now()}`;
            const processed = sessionStorage.getItem('socialLoginProcessed');

            if (processed) {
                navigate('/login', { replace: true });
                return;
            }

            sessionStorage.setItem('socialLoginProcessed', socialLoginKey);

            // URL에서 모든 파라미터 제거
            navigate('/login', { replace: true });

            // 토큰 저장
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            // 계정 통합 알림
            if (accountIntegrated === 'true') {
                setTimeout(() => {
                    alert(`${provider} 계정이 기존 계정과 자동으로 연결되었습니다.`);
                    // 메시지 표시 후 워크스페이스로 이동
                    sessionStorage.removeItem('socialLoginProcessed');
                    navigate('/workspace', { replace: true });
                }, 100);
            } else {
                // 계정 통합이 아닌 일반 로그인 - 바로 워크스페이스로 이동
                sessionStorage.removeItem('socialLoginProcessed');
                navigate('/workspace', { replace: true });
            }
        }
    }, [searchParams, navigate]);

    const validate = () => {
        let tempErrors = {};
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        tempErrors.email = !formValues.email
            ? '이메일을 입력해주세요.'
            : emailRegex.test(formValues.email)
            ? ""
            : "유효한 이메일 형식이 아닙니다.";
        tempErrors.password = formValues.password ? "" : "비밀번호를 입력해주세요.";
        setErrors(tempErrors);
        return Object.values(tempErrors).every(x => x === "");
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormValues({
            ...formValues,
            [name]: value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (validate()) {
            try {
                await authService.login(formValues.email, formValues.password);
                console.log(formValues.email, formValues.password);
                navigate('/workspace'); // 로그인 성공 시 워크스페이스 선택 페이지로 이동
            } catch (error) {
                console.error("Login error:", error);

                // 탈퇴한 계정에 대한 특별한 메시지
                if (error.isDeletedAccount) {
                    const userChoice = window.confirm(
                        `${error.message}\n\n새로 회원가입하시겠습니까?`
                    );
                    if (userChoice) {
                        navigate('/signup');
                    }
                } else if (error.response?.status === 401) {
                    // 일반적인 로그인 실패 (이메일/비밀번호 불일치)
                    alert('이메일 또는 비밀번호가 올바르지 않습니다.');
                } else if (error.response?.data?.message) {
                    // 백엔드에서 제공하는 에러 메시지
                    alert(error.response.data.message);
                } else {
                    // 일반적인 에러 메시지
                    alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
                }
            }
        }
    };

    return (
        <FormLayout title="로그인" onSubmit={handleSubmit}>
            <Box
                sx={{
                    width: '400px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '15px'
                }}
            >
                <CommonTextField
                    required
                    id="email"
                    label="이메일 주소"
                    name="email"
                    autoComplete="email"
                    autoFocus
                    value={formValues.email}
                    onChange={handleChange}
                    error={!!errors.email}
                    helperText={errors.email}
                />
                <CommonTextField
                    required
                    name="password"
                    label="비밀번호"
                    type="password"
                    id="password"
                    autoComplete="current-password"
                    value={formValues.password}
                    onChange={handleChange}
                    error={!!errors.password}
                    helperText={errors.password}
                />
                <CommonButton
                    type="submit"
                    fullWidth
                    variant="contained"
                    sx={{ mt: 3, mb: 2 }}
                    disabled={!formValues.email || !formValues.password}
                >
                    로그인
                </CommonButton>
            </Box>

            {/* --- 소셜 로그인 섹션 시작 --- */}
            <Box sx={{ width: '100%', mt: 2, mb: 2 }}>
                <Divider>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        또는
                    </Typography>
                </Divider>
                <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 2 }}>
                    <CommonButton
                        variant="outlined"
                        onClick={() => {
                            console.log('🔥 구글 로그인 버튼이 클릭되었습니다!');
                            console.log('authService 객체:', authService);
                            console.log('loginWithGoogle 함수:', authService.loginWithGoogle);
                            try {
                                authService.loginWithGoogle();
                            } catch (error) {
                                console.error('❌ authService.loginWithGoogle() 실행 중 오류:', error);
                            }
                        }}
                        sx={{
                            backgroundColor: '#ffffff',
                            color: '#3c4043',
                            border: '1px solid #dadce0',
                            borderRadius: '4px',
                            padding: '10px 24px',
                            fontSize: '14px',
                            fontWeight: 500,
                            fontFamily: 'Google Sans, Roboto, arial, sans-serif',
                            textTransform: 'none',
                            '&:hover': {
                                backgroundColor: '#f8f9fa',
                                boxShadow: '0 1px 2px 0 rgba(60,64,67,.30), 0 1px 3px 1px rgba(60,64,67,.15)',
                                border: '1px solid #dadce0',
                            },
                            '&:focus': {
                                backgroundColor: '#f8f9fa',
                                boxShadow: '0 1px 2px 0 rgba(60,64,67,.30), 0 1px 3px 1px rgba(60,64,67,.15)',
                                border: '1px solid #4285f4',
                            },
                            '&:active': {
                                backgroundColor: '#f1f3f4',
                                boxShadow: '0 1px 2px 0 rgba(60,64,67,.30), 0 2px 6px 2px rgba(60,64,67,.15)',
                            },
                            minWidth: '200px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '12px'
                        }}
                        startIcon={
                            <svg width="18" height="18" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                            </svg>
                        }
                    >
                        Google로 로그인
                    </CommonButton>
                    {/*<CommonButton*/}
                    {/*    variant="outlined"*/}
                    {/*    onClick={() => console.log('Kakao 로그인')}*/}
                    {/*    sx={{*/}
                    {/*        backgroundColor: '#FEE500',*/}
                    {/*        color: '#000000',*/}
                    {/*        '&:hover': {*/}
                    {/*            backgroundColor: '#FEE500',*/}
                    {/*        }*/}
                    {/*    }}*/}
                    {/*>*/}
                    {/*    Kakao*/}
                    {/*</CommonButton>*/}
                    {/*<CommonButton*/}
                    {/*    variant="outlined"*/}
                    {/*    onClick={() => console.log('naver 로그인')}*/}
                    {/*    sx={{*/}
                    {/*            backgroundColor: '#03C75A',*/}
                    {/*            color: '#FFFFFF',*/}
                    {/*            '&:hover': {*/}
                    {/*                backgroundColor: '#03C75A',*/}
                    {/*            }*/}
                    {/*            }}*/}
                    {/*>*/}
                    {/*    naver*/}
                    {/*</CommonButton>*/}
                </Box>
            </Box>

            <Box sx={{ width: '200px', mt: 3, textAlign: 'right' }}>
                <Box>
                    <Link component={RouterLink} to="/signup" variant="body2">
                        계정이 없으신가요? 회원가입<br/>
                    </Link>
                </Box>

                <Box sx={{ mt: 1 }}>
                    <Link component={RouterLink} to="/FindPassword" variant="body2">
                        비밀번호를 잊으셨나요?
                    </Link>
                </Box>
            </Box>

        </FormLayout>
    );
};

export default Login;