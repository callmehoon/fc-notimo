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
                    >
                        Google
                    </CommonButton>
                    <CommonButton
                        variant="outlined"
                        onClick={() => console.log('Kakao 로그인')}
                        sx={{
                            backgroundColor: '#FEE500',
                            color: '#000000',
                            '&:hover': {
                                backgroundColor: '#FEE500',
                            }
                        }}
                    >
                        Kakao
                    </CommonButton>
                    <CommonButton
                        variant="outlined"
                        onClick={() => console.log('naver 로그인')}
                        sx={{
                                backgroundColor: '#03C75A',
                                color: '#FFFFFF',
                                '&:hover': {
                                    backgroundColor: '#03C75A',
                                }
                                }}
                    >
                        naver
                    </CommonButton>
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