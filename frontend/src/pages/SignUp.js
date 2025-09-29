import React, { useState, useEffect } from "react";
import { Link as RouterLink, useNavigate, useSearchParams } from "react-router-dom";
import {Link, Grid, Box} from "@mui/material";
import FormLayout from "../components/layout/FormLayout";
import CommonTextField from "../components/form/CommonTextField";
import CommonButton from "../components/button/CommonButton";
import authService from "../services/authService";

// 회원가입 페이지
const SignUp = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        name: '',
        phone: '',
        verificationCode: '',
    });

    const [errors, setErrors] = useState({});
    const [isVerificationCodeSent, setIsVerificationCodeSent] = useState(false);

    // 소셜 로그인 감지 및 리다이렉트
    useEffect(() => {
        const isNewUser = searchParams.get('isNewUser');
        const provider = searchParams.get('provider');
        const email = searchParams.get('email');
        const name = searchParams.get('name');
        const socialId = searchParams.get('socialId');

        if (isNewUser === 'true' && provider && email && name) {
            // 소셜 회원가입 페이지로 리다이렉트
            navigate('/social-signup', {
                state: {
                    provider,
                    email: decodeURIComponent(email),
                    name: decodeURIComponent(name),
                    socialId
                }
            });
        }
    }, [searchParams, navigate]);

    const validateForm = (isSubmitting = false) => {
        let tempErrors = {};
        const emailRegex = /^[\w-.]+@[\w-]+\.[\w-]{2,4}$/;
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        const phoneRegex = /^010-\d{4}-\d{4}$/;

        if (!formValues.email) {
            tempErrors.email = '이메일은 필수 항목입니다.';
        } else if (!emailRegex.test(formValues.email)) {
            tempErrors.email = '유효한 이메일 주소를 입력해주세요.';
        }

        if (isSubmitting) {
            if (!formValues.password) {
                tempErrors.password = '비밀번호는 필수 항목입니다.';
            } else if (!passwordRegex.test(formValues.password)) {
                tempErrors.password = '비밀번호는 최소 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.';
            }

            if (!formValues.confirmPassword) {
                tempErrors.confirmPassword = '비밀번호 확인은 필수 항목입니다.';
            } else if (formValues.password !== formValues.confirmPassword) {
                tempErrors.confirmPassword = '비밀번호가 일치하지 않습니다.';
            }

            if (!formValues.name) {
                tempErrors.name = '이름은 필수 항목입니다.';
            }

            if (!formValues.phone) {
                tempErrors.phone = '휴대폰 번호는 필수 항목입니다.';
            } else if (!phoneRegex.test(formValues.phone)) {
                tempErrors.phone = '유효한 휴대폰 번호 (예: 010-1234-5678)를 입력해주세요.';
            }

            if (!formValues.verificationCode) {
                tempErrors.verificationCode = '인증 코드는 필수 항목입니다.';
            }
        }

        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormValues((prev) => ({
            ...prev,
            [name]: value,
        }));
        if (errors[name]) {
            setErrors((prev) => ({
                ...prev,
                [name]: undefined,
            }));
        }
    };

    const handleSendVerificationCode = async () => {
        if (validateForm()) {
            try {
                await authService.sendVerificationCode(formValues.email);
                alert('인증 코드가 이메일로 발송되었습니다.');
                setIsVerificationCodeSent(true);
            } catch (error) {
                if (error.response && error.response.data && error.response.data.message) {
                    alert(error.response.data.message);
                } else {
                    alert('인증 코드 발송 중 오류가 발생했습니다.');
                }
            }
        }
    };

    const handleSignUp = async (e) => {
        e.preventDefault();

        if (validateForm(true)) {
            const requestData = {
                userName: formValues.name,
                email: formValues.email,
                userNumber: formValues.phone,
                password: formValues.password,
                verificationCode: formValues.verificationCode,
            };
            try {
                await authService.signup(requestData);
                alert('회원가입 성공! 로그인 페이지로 이동합니다.');
                navigate('/login');
            } catch (error) {
                if (error.response && error.response.data && error.response.data.message) {
                    alert(error.response.data.message);
                } else {
                    alert('회원가입 중 오류가 발생했습니다.');
                }
            }
        } else {
            alert('입력 항목을 다시 확인해주세요.');
        }
    };

    return (
        <FormLayout title="회원가입" onSubmit={handleSignUp}>
            <Box
                autoComplete="off"
                noValidate
                sx={{
                    width: '400px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '15px'
                }}
            >
                <Box
                    sx={{
                        margin: 0,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '15px',
                        width: '100%'
                    }}
                >
                    <CommonTextField
                        required
                        id="email"
                        label="이메일 주소"
                        name="email"
                        value={formValues.email}
                        onChange={handleChange}
                        autoComplete="email"
                        error={!!errors.email}
                        helperText={errors.email}
                        disabled={isVerificationCodeSent}
                    />
                    <CommonButton
                        type="button"
                        variant="contained"
                        onClick={handleSendVerificationCode}
                        disabled={isVerificationCodeSent}
                        sx={{
                            height: '58px',
                        }}
                    >
                        인증
                    </CommonButton>
                </Box>
                {isVerificationCodeSent && (
                    <CommonTextField
                        required
                        name="verificationCode"
                        label="인증 코드"
                        value={formValues.verificationCode}
                        onChange={handleChange}
                        error={!!errors.verificationCode}
                        helperText={errors.verificationCode}
                    />
                )}
                <CommonTextField
                    required
                    name="password"
                    label="비밀번호"
                    type="password"
                    id="password"
                    autoComplete="new-password"
                    value={formValues.password}
                    onChange={handleChange}
                    error={!!errors.password}
                    helperText={errors.password}
                />
                <CommonTextField
                    required
                    name="confirmPassword"
                    label="비밀번호 확인"
                    type="password"
                    id="confirmPassword"
                    value={formValues.confirmPassword}
                    onChange={handleChange}
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword}
                />
                <CommonTextField
                    required
                    name="name"
                    label="이름"
                    value={formValues.name}
                    onChange={handleChange}
                    error={!!errors.name}
                    helperText={errors.name}
                />
                <CommonTextField
                    required
                    name="phone"
                    label="휴대폰번호"
                    value={formValues.phone}
                    onChange={handleChange}
                    error={!!errors.phone}
                    helperText={errors.phone}
                />
                <CommonButton
                    type="submit"
                    fullWidth
                    variant="contained"
                    sx={{mt: 3, mb: 2}}
                >
                    가입하기
                </CommonButton>
            </Box>
            <Grid container justifyContent="flex-end">
                <Grid>
                    <Link component={RouterLink} to="/login" variant="body2">
                        이미 계정이 있으신가요? 로그인
                    </Link>
                </Grid>
            </Grid>

        </FormLayout>
    );
};

export default SignUp;