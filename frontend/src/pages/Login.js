import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {Link, Grid, Box, Divider, Typography} from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import authService from "../services/authService";

const Login = () => {
    const navigate = useNavigate();
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
    });
    const [errors, setErrors] = useState({});

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
                alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
                console.error("Login error:", error);
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
                        onClick={() => console.log('Google 로그인')}
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

            <Grid container sx={{ width: '200px', mt : 3, textAlign: 'right' }}>
                <Grid item xs={12}>
                    <Link component={RouterLink} to="/signup" variant="body2">
                        계정이 없으신가요? 회원가입<br/>
                    </Link>
                </Grid>

                <Grid item xs={12} sx={{ mt: 1 }}>
                    <Link component={RouterLink} to="/FindPassword" variant="body2">
                        비밀번호를 잊으셨나요?
                    </Link>
                </Grid>
            </Grid>

        </FormLayout>
    );
};

export default Login;