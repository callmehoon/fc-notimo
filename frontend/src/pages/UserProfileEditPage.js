import React, {useState, useEffect} from 'react';
import {Box, TextField, Button, Typography, Paper, CssBaseline} from '@mui/material';
import Sidebar from '../components/layout/Sidebar';
import {useNavigate} from 'react-router-dom';
import MainLayout from "../components/layout/MainLayout";

// 목업 데이터 (실제로는 API 호출 등으로 가져올 사용자 정보)
const mockUserProfile = {
    name: '홍길동',
    email: 'hong.gildong@example.com',
    // password: 'password123', // 비밀번호는 보통 수정 시 현재 비밀번호 확인 후 새 비밀번호 입력
    phone: '010-1234-5678',
};

export default function UserProfileEditPage() {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(mockUserProfile);
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [newPasswordError, setNewPasswordError] = useState('');
    const [confirmNewPasswordError, setConfirmNewPasswordError] = useState('');

    useEffect(() => {
        // ... (unchanged)
    }, []);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setUserProfile(prevProfile => ({
            ...prevProfile,
            [name]: value,
        }));
    };

    const handlePasswordChange = (e) => {
        const {name, value} = e.target;
        if (name === 'currentPassword') {
            setCurrentPassword(value);
        } else if (name === 'newPassword') {
            setNewPassword(value);
            if (value && confirmNewPassword && value !== confirmNewPassword) {
                setConfirmNewPasswordError('새 비밀번호와 일치하지 않습니다.');
            } else {
                setConfirmNewPasswordError('');
            }
            setNewPasswordError(value.length < 6 && value.length > 0 ? '비밀번호는 6자 이상이어야 합니다.' : ''); // Example validation
        } else if (name === 'confirmNewPassword') {
            setConfirmNewPassword(value);
            if (value && newPassword && value !== newPassword) {
                setConfirmNewPasswordError('새 비밀번호와 일치하지 않습니다.');
            } else {
                setConfirmNewPasswordError('');
            }
        }
    };

    const validatePasswordFields = () => {
        let isValid = true;
        if (newPassword || confirmNewPassword) { // Only validate if user intends to change password
            if (newPassword.length < 6) {
                setNewPasswordError('비밀번호는 6자 이상이어야 합니다.');
                isValid = false;
            } else {
                setNewPasswordError('');
            }

            if (newPassword !== confirmNewPassword) {
                setConfirmNewPasswordError('새 비밀번호와 일치하지 않습니다.');
                isValid = false;
            } else {
                setConfirmNewPasswordError('');
            }
        }
        return isValid;
    };

    const handleSave = () => {
        if (!validatePasswordFields()) {
            return;
        }

        console.log('사용자 정보 저장:', userProfile);
        if (newPassword) {
            // 실제로는 현재 비밀번호 확인 로직 필요
            console.log('비밀번호 변경 시도:', {currentPassword, newPassword});
        }
        alert('회원정보가 저장되었습니다! (실제 저장 로직은 구현되지 않았습니다.)');
        navigate('/'); // 저장 후 메인 페이지 또는 대시보드로 이동
    };

    const handleCancel = () => {
        navigate('/'); // 취소 시 메인 페이지 또는 대시보드로 이동
    };

    const isPasswordFormInvalid = newPasswordError || confirmNewPasswordError;

    return (
        <MainLayout>
            <Box sx={{display: 'flex', height: '100vh', overflow: 'hidden'}}>
                <Box component="main" sx={{
                    flexGrow: 1,
                    p: 3,
                    display: 'flex',
                    flexDirection: 'column',
                    height: '100vh',
                    overflow: 'auto'
                }}>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        minHeight: '100%',
                    }}>
                        <Paper elevation={3} sx={{p: 4, maxWidth: 800, width: '100%', borderRadius: '8px'}}>
                            <Typography variant="h5" component="h1" sx={{mb: 2, textAlign: 'center'}}>
                                회원정보 수정
                            </Typography>
                            <TextField
                                label="이름"
                                name="name"
                                variant="outlined"
                                fullWidth
                                value={userProfile.name}
                                onChange={handleChange}
                                sx={{mb: 2}}
                            />
                            <TextField
                                label="이메일"
                                name="email"
                                variant="outlined"
                                fullWidth
                                value={userProfile.email}
                                onChange={handleChange}
                                sx={{mb: 2}}
                                disabled // 이메일은 보통 수정 불가
                            />
                            <TextField
                                label="전화번호"
                                name="phone"
                                variant="outlined"
                                fullWidth
                                value={userProfile.phone}
                                onChange={handleChange}
                                sx={{mb: 3}}
                            />

                            <Typography variant="h6" sx={{mb: 2}}>비밀번호 변경</Typography>
                            <TextField
                                label="현재 비밀번호"
                                name="currentPassword"
                                type="password"
                                variant="outlined"
                                fullWidth
                                value={currentPassword}
                                onChange={handlePasswordChange}
                                sx={{mb: 2}}
                            />
                            <TextField
                                label="새 비밀번호"
                                name="newPassword"
                                type="password"
                                variant="outlined"
                                fullWidth
                                value={newPassword}
                                onChange={handlePasswordChange}
                                error={!!newPasswordError}
                                helperText={newPasswordError}
                                sx={{mb: 2}}
                            />
                            <TextField
                                label="새 비밀번호 확인"
                                name="confirmNewPassword"
                                type="password"
                                variant="outlined"
                                fullWidth
                                value={confirmNewPassword}
                                onChange={handlePasswordChange}
                                error={!!confirmNewPasswordError}
                                helperText={confirmNewPasswordError}
                                sx={{mb: 3}}
                            />

                            <Box sx={{display: 'flex', justifyContent: 'space-between', gap: 2}}>
                                <Button variant="contained" color="primary" onClick={handleSave} fullWidth
                                        disabled={isPasswordFormInvalid && (newPassword || confirmNewPassword)}>
                                    저장
                                </Button>
                                <Button variant="outlined" color="secondary" onClick={handleCancel} fullWidth>
                                    취소
                                </Button>
                            </Box>
                        </Paper>
                    </Box>
                </Box>
            </Box>
        </MainLayout>
    );
}
