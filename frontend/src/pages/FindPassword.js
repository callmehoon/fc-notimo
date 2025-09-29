import React, { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Container, Typography, Box } from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import authService from '../services/authService';

const FindPassword = () => {
    // 1. 상태 관리 변경: 단계를 관리하는 'step'과 새로운 입력 필드 상태 추가
    const [step, setStep] = useState(1); // 1: 이메일 입력, 2: 인증번호 입력, 3: 비밀번호 재설정, 4: 완료
    const [email, setEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState(''); // 오류 메시지 상태
    const [loading, setLoading] = useState(false); // 로딩 상태

    // 2. 각 단계별 핸들러 함수 구현
    const handleRequestCode = async (e) => {
        e.preventDefault();
        // 이메일 유효성 검사
        if (!email) {
            setError('이메일을 입력해주세요.');
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setError('유효한 이메일 주소를 입력해주세요.');
            return;
        }

        try {
            setLoading(true);
            setError('');
            console.log('인증번호 요청 이메일:', email);

            // 백엔드에 이메일로 인증번호 전송 요청 API 호출
            await authService.sendPasswordResetCode(email);
            alert('인증번호가 이메일로 발송되었습니다. 이메일을 확인해주세요.');
            setStep(2);
        } catch (error) {
            console.error('인증번호 발송 실패:', error);
            if (error.response?.status === 404) {
                setError('등록되지 않은 이메일입니다.');
            } else if (error.response?.data?.message) {
                setError(error.response.data.message);
            } else {
                setError('인증번호 발송에 실패했습니다. 다시 시도해주세요.');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyCode = (e) => {
        e.preventDefault();
        if (!verificationCode) {
            setError('인증번호를 입력해주세요.');
            return;
        }

        // 인증번호 검증은 비밀번호 재설정 단계에서 함께 처리
        setError('');
        setStep(3);
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();

        // 비밀번호 유효성 검사
        if (!newPassword) {
            setError('새 비밀번호를 입력해주세요.');
            return;
        }
        if (!confirmPassword) {
            setError('비밀번호 확인을 입력해주세요.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 백엔드 요구사항에 맞는 비밀번호 유효성 검증
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,20}$/;
        if (!passwordRegex.test(newPassword)) {
            setError('비밀번호는 6-20자의 대소문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다.');
            return;
        }

        try {
            setLoading(true);
            setError('');
            console.log('비밀번호 재설정 요청:', {
                email,
                verificationCode,
                passwordLength: newPassword.length,
                passwordPattern: passwordRegex.test(newPassword)
            });

            // 백엔드에 비밀번호 재설정 API 호출
            await authService.resetPassword(email, verificationCode, newPassword);
            setStep(4);
        } catch (error) {
            console.error('비밀번호 재설정 실패:', error);
            console.error('응답 데이터:', error.response?.data);

            if (error.response?.status === 400) {
                const errorMessage = error.response?.data?.message;
                if (errorMessage) {
                    setError(errorMessage);
                } else {
                    setError('인증번호가 올바르지 않거나 비밀번호 형식이 맞지 않습니다.');
                }
            } else if (error.response?.data?.message) {
                setError(error.response.data.message);
            } else {
                setError('비밀번호 재설정에 실패했습니다. 다시 시도해주세요.');
            }
        } finally {
            setLoading(false);
        }
    };

    // 3. 단계(step)에 따른 UI를 렌더링하는 함수
    const renderStep = () => {
        switch (step) {
            case 1: // 이메일 입력 단계
                return (
                    <>
                        <Typography component="p" align="center" sx={{ mb: 3 }}>
                            가입 시 사용한 이메일 주소를 입력해주세요.
                        </Typography>
                        <CommonTextField
                            label="이메일"
                            name="email"
                            type="email"
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                            if (error) setError('');//입력 시작 시 에러 초기화
                            }}
                            required
                            fullWidth
                            autoFocus
                            error={!!error}
                            helperText={error}
                        />
                        <CommonButton
                            onClick={handleRequestCode}
                            fullWidth
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? '발송 중...' : '인증번호 받기'}
                        </CommonButton>
                    </>
                );
            case 2: // 인증번호 입력 단계
                return (
                    <>
                        <Typography component="p" align="center" sx={{ mb: 3 }}>
                            {email} 주소로 발송된
                            <br/>
                            인증번호를 입력해주세요.
                        </Typography>
                        <CommonTextField
                            label="이메일"
                            value={email}
                            fullWidth
                            disabled // 이메일 수정 불가
                            sx={{ mb: 2 }}
                        />
                        <CommonTextField
                            label="인증번호"
                            name="verificationCode"
                            value={verificationCode}
                            onChange={(e) => setVerificationCode(e.target.value)}
                            required
                            fullWidth
                            autoFocus
                            error={!!error}
                            helperText={error}
                        />
                        {/* TODO: 인증번호 유효시간 타이머 구현 위치 */}
                        <CommonButton onClick={handleVerifyCode} fullWidth sx={{ mt: 3, mb: 2 }}>
                            확인
                        </CommonButton>
                    </>
                );
            case 3: // 비밀번호 재설정 단계
                return (
                    <>
                        <Typography component="p" align="center" sx={{ mb: 3 }}>
                            새로운 비밀번호를 설정해주세요.
                        </Typography>
                        <CommonTextField
                            label="새 비밀번호"
                            name="newPassword"
                            type="password"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                            fullWidth
                            autoFocus
                            sx={{ mb: 2 }}
                            helperText="6-20자, 대소문자, 숫자, 특수문자(@$!%*?&) 포함"
                        />
                        <CommonTextField
                            label="새 비밀번호 확인"
                            name="confirmPassword"
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                            fullWidth
                            error={!!error}
                            helperText={error}
                        />
                        <CommonButton
                            onClick={handleResetPassword}
                            fullWidth
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? '변경 중...' : '비밀번호 변경'}
                        </CommonButton>
                    </>
                );
            case 4: // 완료 단계
                return (
                    <Box sx={{ textAlign: 'center' }}>
                        <Typography component="p" sx={{ mb: 4 }}>
                            비밀번호가 성공적으로 변경되었습니다.
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <CommonButton component={RouterLink} to="/login" fullWidth variant="outlined">
                                로그인 화면으로 돌아가기
                            </CommonButton>
                        </Box>
                    </Box>
                );
            default:
                return null;
        }
    };

    // 단계별 form 제출 핸들러
    const getCurrentStepHandler = () => {
        switch (step) {
            case 1:
                return handleRequestCode;
            case 2:
                return handleVerifyCode;
            case 3:
                return handleResetPassword;
            default:
                return (e) => e.preventDefault();
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <FormLayout title="비밀번호 찾기" onSubmit={getCurrentStepHandler()}>
                {renderStep()}
            </FormLayout>
        </Container>
    );
};

export default FindPassword;