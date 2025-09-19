import React, { useState } from "react";
import { Link as RouterLink} from "react-router-dom";
import {Link, Grid, FormGroup, Box} from "@mui/material";
import FormLayout from "../components/layout/FormLayout";
import CommonTextField from "../components/form/CommonTextField";
import CommonButton from "../components/button/CommonButton";

const SignUp = () => {
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        name: '',
        phone: '',
    });

    const [errors, setErrors] = useState({});

    const validateForm = () => {
        let tempErrors = {};
        const emailRegex = /^[\w-.]+@[\w-]+\.[\w-]{2,4}$/;
        // Password must be at least 8 characters long, contain at least one letter, one number, and one special character
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        const phoneRegex = /^010-\d{4}-\d{4}$/;

        if (!formValues.email) {
            tempErrors.email = '이메일은 필수 항목입니다.';
        } else if (!emailRegex.test(formValues.email)) {
            tempErrors.email = '유효한 이메일 주소를 입력해주세요.';
        }

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

        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormValues((prev) => ({
            ...prev,
            [name]: value,
        }));
        // Clear error for the field being changed
        if (errors[name]) {
            setErrors((prev) => ({
                ...prev,
                [name]: undefined,
            }));
        }
    };

    const handleSignUp = (e) => {
        e.preventDefault();

        if (validateForm()) {
            // 여기에 백엔드로 데이터를 전송하는 회원가입 로직을 구현합니다.
            alert('회원가입 성공! (실제 백엔드 연동 필요)');
            console.log('Form Values:', formValues);
        } else {
            console.log('Validation Errors:', errors);
        }
    };


    return (
        <FormLayout title="회원가입">
            <FormGroup
                onSubmit={handleSignUp}
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
                    />
                    <CommonButton
                        type="button"
                        variant="contained"
                        sx={{
                            height: '58px',
                        }}
                    >
                        인증
                    </CommonButton>
                </Box>
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
                    onClick={handleSignUp}
                    sx={{mt: 3, mb: 2}}
                >
                    가입하기
                </CommonButton>
            </FormGroup>
            <Grid container justifyContent="flex-end">
                <Grid item>
                    <Link component={RouterLink} to="/login" variant="body2">
                        이미 계정이 있으신가요? 로그인
                    </Link>
                </Grid>
            </Grid>

        </FormLayout>
    );
};

export default SignUp;