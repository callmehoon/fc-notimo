import React, { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Container, Typography, Box } from '@mui/material';
import FormLayout from '../components/layout/FormLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';

const FindPassword = () => {
    // 1. 상태 관리 변경: 단계를 관리하는 'step'과 새로운 입력 필드 상태 추가
    const [step, setStep] = useState(1); // 1: 이메일 입력, 2: 인증번호 입력, 3: 비밀번호 재설정, 4: 완료
    const [email, setEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState(''); // 오류 메시지 상태

    // 2. 각 단계별 핸들러 함수 구현
    const handleRequestCode = (e) => {
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
        setError(''); // 에러 초기화
        console.log('인증번호 요청 이메일:', email);
        setStep(2);

        // TODO: 백엔드에 이메일로 인증번호 전송 요청 API 호출
        //console.log('인증번호 요청 이메일:', email);
        // API 호출 성공 시 다음 단계로 이동
        //setStep(2);
    };

    const handleVerifyCode = (e) => {
        e.preventDefault();
        // TODO: 백엔드에 이메일과 인증번호를 보내 유효성 검증 API 호출
        //임시 하드코딩된 인증번호 '123456'으로 확인
        // console.log('입력한 인증번호:', verificationCode);
        // API 호출 성공 및 인증번호 일치 시
        if (verificationCode === '123456') {
        setStep(3);
        setError(3);
        } else {
            setError('인증번호가 올바르지 않습니다.');
        }
    };
    const handleResetPassword = (e) => {
        e.preventDefault();
        if (newPassword !== confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }
        setError('');
        // TODO: 백엔드에 새 비밀번호를 전송하여 업데이트하는 API 호출
        console.log('새 비밀번호:', newPassword);
        // API 호출 성공 시
        setStep(4);
    };

    // 3. 단계(step)에 따른 UI를 렌더링하는 함수
    const renderStep = () => {
        switch (step) {
            case 1: // 이메일 입력 단계
                return (
                    <Box component="form" onSubmit={handleRequestCode} noValidate>
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
                        <CommonButton onClick={handleRequestCode}
                                      fullWidth sx={{ mt: 3, mb: 2 }}>
                            인증번호 받기
                        </CommonButton>
                    </Box>
                );
            case 2: // 인증번호 입력 단계
                return (
                    <Box component="form" onSubmit={handleVerifyCode} noValidate>
                        <Typography component="p" align="center" sx={{ mb: 3 }}>
                            {email} 주소로 발송된
                            <br/>
                            인증번호를 입력해주세요.
                            <br/>
                            <Typography component="span" sx={{ color: 'red', fontSize:'0.9rem' }}>
                                임시 인증번호 : 123456
                            </Typography>
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
                        <CommonButton type="submit" fullWidth sx={{ mt: 3, mb: 2 }}>
                            확인
                        </CommonButton>
                    </Box>
                );
            case 3: // 비밀번호 재설정 단계
                return (
                    <Box component="form" onSubmit={handleResetPassword} noValidate>
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
                        <CommonButton type="submit" fullWidth sx={{ mt: 3, mb: 2 }}>
                            비밀번호 변경
                        </CommonButton>
                    </Box>
                );
            case 4: // 완료 단계
                return (
                    <Box sx={{ textAlign: 'center' }}>
                        <Typography component="p" sx={{ mb: 4 }}>
                            비밀번호가 성공적으로 변경되었습니다.
                        </Typography>
                        <CommonButton component={RouterLink} to="/login" fullWidth>
                            로그인 화면으로 돌아가기
                        </CommonButton>
                    </Box>
                );
            default:
                return null;
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <FormLayout title="비밀번호 찾기">
                {renderStep()}
            </FormLayout>
        </Container>
    );
};

export default FindPassword;