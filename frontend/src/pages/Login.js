import React, { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {Link, Grid, Box, Divider, Typography} from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import FindPassword from "./FindPassword";


const Login = () => {
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
    });
    const [emailError, setEmailError] = useState(false);
    const [emailHelperText, setEmailHelperText] = useState('');

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    const validateEmail = (email) => {
        if (!email) {
            setEmailError(true);
            setEmailHelperText('이메일을 입력해주세요.');
            return false;
        } else if (!emailRegex.test(email)) {
            setEmailError(true);
            setEmailHelperText('유효한 이메일 형식이 아닙니다.');
            return false;
        } else {
            setEmailError(false);
            setEmailHelperText('');
            return true;
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormValues((prev) => ({
            ...prev,
            [name]: value,
        }));

        if (name === 'email') {
            validateEmail(value);
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const isEmailValid = validateEmail(formValues.email);

        if (isEmailValid) {
            console.log('로그인 시도:', formValues);
            // 여기에 백엔드로 데이터를 전송하는 로그인 로직을 구현합니다.
        } else {
            console.log('유효성 검사 실패');
        }
    };

    return (
        // 공통 레이아웃 컴포넌트를 사용하여 중앙 정렬과 제목을 처리합니다.
        <FormLayout title="로그인">
            {/* 공통 텍스트 필드를 사용하여 이메일 입력을 받습니다. */}
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
                    error={emailError}
                    helperText={emailHelperText}
                />
                {/* 공통 텍스트 필드를 사용하여 비밀번호 입력을 받습니다. */}
                <CommonTextField
                    required
                    name="password"
                    label="비밀번호"
                    type="password"
                    id="password"
                    autoComplete="current-password"
                    value={formValues.password}
                    onChange={handleChange}
                />
                <CommonButton
                    type="submit"
                    fullWidth
                    variant="contained"
                    onClick={handleSubmit}
                    sx={{ mt: 3, mb: 2 }} // 버튼 위아래 여백을 설정합니다.
                    disabled={emailError} // 이메일 에러가 있으면 버튼 비활성화
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
                    {/* 각 소셜 로그인 버튼을 여기에 추가합니다. */}
                    {/* Google 로그인 버튼 */}
                    <CommonButton
                        variant="outlined"
                        onClick={() => console.log('Google 로그인')}
                        sx={{
                            // 여기에 각 소셜 버튼의 아이콘과 스타일을 추가할 수 있습니다.
                        }}
                    >
                        Google
                    </CommonButton>
                    {/*Kakao 로그인 버튼 */}
                    <CommonButton
                        variant="outlined"
                        onClick={() => console.log('Kakao 로그인')}
                        sx={{
                            backgroundColor: '#FEE500', // 카카오 색상
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
                                backgroundColor: '#03C75A', // 네이버 색상
                                color: '#FFFFFF',
                                '&:hover': {
                                    backgroundColor: '#03C75A',

                                    // 여기에 각 소셜 버튼의 아이콘과 스타일을 추가할 수 있습니다.
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