import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { Box, Typography, Alert } from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import authService from '../services/authService';

const SocialSignupPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();
    const [formValues, setFormValues] = useState({
        userNumber: '',
    });
    const [errors, setErrors] = useState({});
    const [socialInfo, setSocialInfo] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // URL 파라미터에서 소셜 로그인 정보 읽기
        const provider = searchParams.get('provider');
        const email = searchParams.get('email');
        const name = searchParams.get('name');
        const socialId = searchParams.get('socialId');
        const success = searchParams.get('success');
        const isNewUser = searchParams.get('isNewUser');

        // 필수 정보가 없거나 성공하지 않은 경우 로그인 페이지로 리다이렉트
        if (!provider || !email || !name || !socialId || success !== 'true' || isNewUser !== 'true') {
            console.error('소셜 로그인 정보가 불완전합니다:', {
                provider, email, name, socialId, success, isNewUser
            });
            navigate('/login');
            return;
        }

        // URL에서 디코딩하여 socialInfo 설정
        setSocialInfo({
            provider: provider,
            email: decodeURIComponent(email),
            name: decodeURIComponent(name),
            socialId: socialId
        });
    }, [searchParams, navigate]);

    const validate = () => {
        let tempErrors = {};
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        tempErrors.userNumber = !formValues.userNumber
            ? '핸드폰 번호를 입력해주세요.'
            : phoneRegex.test(formValues.userNumber)
            ? ""
            : "010-XXXX-XXXX 형식으로 입력해주세요.";
        setErrors(tempErrors);
        return Object.values(tempErrors).every(x => x === "");
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'userNumber') {
            const onlyNums = value.replace(/[^0-9]/g, '');
            if (onlyNums.length <= 11) {
                let formatted = onlyNums;
                if (onlyNums.length > 3 && onlyNums.length <= 7) {
                    formatted = `${onlyNums.slice(0, 3)}-${onlyNums.slice(3)}`;
                } else if (onlyNums.length > 7) {
                    formatted = `${onlyNums.slice(0, 3)}-${onlyNums.slice(3, 7)}-${onlyNums.slice(7)}`;
                }
                setFormValues(prev => ({ ...prev, userNumber: formatted }));
            }
        } else {
            setFormValues(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate() || !socialInfo) return;

        setLoading(true);
        try {
            const socialSignupData = {
                provider: socialInfo.provider.toUpperCase(),
                email: socialInfo.email,
                name: socialInfo.name,
                phoneNumber: formValues.userNumber,
                socialId: socialInfo.socialId,
                agreedToTerms: true, // 소셜 로그인 시 기본 동의로 처리
                agreedToPrivacyPolicy: true,
                agreedToMarketing: false
            };

            await authService.completeSocialSignup(socialSignupData);
            navigate('/workspace');
        } catch (error) {
            console.error('소셜 회원가입 오류:', error);
            if (error.response?.data?.message) {
                alert(error.response.data.message);
            } else {
                alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
            }
        } finally {
            setLoading(false);
        }
    };

    if (!socialInfo) {
        return null;
    }

    return (
        <FormLayout title="소셜 회원가입 완료" onSubmit={handleSubmit}>
            <Box
                sx={{
                    width: '400px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '15px'
                }}
            >
                <Alert severity="info" sx={{ width: '100%', mb: 2 }}>
                    {socialInfo.provider} 계정 정보가 확인되었습니다.
                    추가 정보를 입력하여 회원가입을 완료해주세요.
                </Alert>

                <CommonTextField
                    disabled
                    id="email"
                    label="이메일 주소"
                    value={socialInfo.email}
                    InputLabelProps={{ shrink: true }}
                />

                <CommonTextField
                    disabled
                    id="userName"
                    label="이름"
                    value={socialInfo.name}
                    InputLabelProps={{ shrink: true }}
                />

                <CommonTextField
                    required
                    id="userNumber"
                    label="핸드폰 번호"
                    name="userNumber"
                    placeholder="010-1234-5678"
                    value={formValues.userNumber}
                    onChange={handleChange}
                    error={!!errors.userNumber}
                    helperText={errors.userNumber}
                    InputLabelProps={{ shrink: !!formValues.userNumber }}
                />

                <CommonButton
                    type="submit"
                    fullWidth
                    variant="contained"
                    sx={{ mt: 3, mb: 2 }}
                    disabled={!formValues.userNumber || loading}
                >
                    {loading ? '처리 중...' : '회원가입 완료'}
                </CommonButton>

                <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', mt: 2 }}>
                    {socialInfo.provider} 로그인을 통해 가입하시면
                    이용약관 및 개인정보 처리방침에 동의한 것으로 간주됩니다.
                </Typography>
            </Box>
        </FormLayout>
    );
};

export default SocialSignupPage;