import React, {useState, useEffect} from 'react';
import {
    Box,
    TextField,
    Button,
    Typography,
    Paper,
    Divider,
    Card,
    CardContent,
    CardHeader,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Alert,
    Chip,
    CircularProgress
} from '@mui/material';
import {useNavigate} from 'react-router-dom';
import MainLayout from "../components/layout/MainLayout";
import userService from '../services/userService';

export default function UserProfileEditPage() {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // 계정 통합 관련
    const [showAccountIntegration, setShowAccountIntegration] = useState(false);
    const [integrationEmail, setIntegrationEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [integrationPassword, setIntegrationPassword] = useState('');
    const [isCodeSent, setIsCodeSent] = useState(false);

    // 계정 통합 처리
    const handleSendCode = async () => {
        if (!integrationEmail) {
            alert('이메일을 입력해주세요.');
            return;
        }
        try {
            await userService.sendAccountIntegrationCode(integrationEmail);
            alert('인증 코드가 발송되었습니다.');
            setIsCodeSent(true);
        } catch (error) {
            console.error('인증 코드 발송 실패:', error);
            alert('인증 코드 발송에 실패했습니다.');
        }
    };

    const handleCompleteIntegration = async () => {
        if (!integrationPassword || !verificationCode) {
            alert('인증코드와 비밀번호를 모두 입력해주세요.');
            return;
        }
        try {
            await userService.completeAccountIntegration(integrationEmail, verificationCode, integrationPassword);
            alert('계정 통합이 완료되었습니다. 보안을 위해 재로그인합니다.');
            localStorage.clear();
            navigate('/login');
        } catch (error) {
            console.error('계정 통합 실패:', error);
            alert(error.response?.data?.message || '계정 통합에 실패했습니다.');
        }
    };

    // 비밀번호 변경 관련
    const [showPasswordChange, setShowPasswordChange] = useState(false);
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');

    // 회원탈퇴 관련
    const [showDeleteAccount, setShowDeleteAccount] = useState(false);
    const [deletePassword, setDeletePassword] = useState('');
    const [deleteConfirmation, setDeleteConfirmation] = useState('');

    useEffect(() => {
        fetchUserProfile();
    }, []);

    // 로그인 상태 확인
    useEffect(() => {
        const accessToken = localStorage.getItem('accessToken');
        if (!accessToken) {
            alert('로그인이 필요합니다.');
            navigate('/login');
        }
    }, [navigate]);

    const fetchUserProfile = async () => {
        try {
            setLoading(true);
            const profile = await userService.getUserProfile();
            setUserProfile(profile);
            setLoading(false);
        } catch (error) {
            console.error('사용자 정보 로딩 실패:', error);
            setLoading(false);
            alert('사용자 정보를 불러오는데 실패했습니다.');
        }
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
                setUserProfile(prevProfile => ({
                    ...prevProfile,
                    userNumber: formatted,
                }));
            }
        } else {
            setUserProfile(prevProfile => ({
                ...prevProfile,
                [name]: value,
            }));
        }
    };

    // 비밀번호 변경 처리
    const handlePasswordChange = async () => {
        // 유효성 검증
        if (!currentPassword) {
            alert('현재 비밀번호를 입력해주세요.');
            return;
        }
        if (!newPassword) {
            alert('새 비밀번호를 입력해주세요.');
            return;
        }
        if (newPassword !== confirmNewPassword) {
            alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return;
        }
        // 백엔드 요구사항에 맞는 비밀번호 유효성 검증
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,20}$/;
        if (!passwordRegex.test(newPassword)) {
            alert('새 비밀번호는 6-20자의 대소문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다.');
            return;
        }
        if (currentPassword === newPassword) {
            alert('새 비밀번호는 현재 비밀번호와 달라야 합니다.');
            return;
        }

        // 로그인 상태 확인
        const accessToken = localStorage.getItem('accessToken');
        if (!accessToken) {
            alert('로그인이 필요합니다. 다시 로그인해주세요.');
            navigate('/login');
            return;
        }

        console.log('비밀번호 변경 요청 시작...', {
            currentPasswordLength: currentPassword.length,
            newPasswordLength: newPassword.length,
            passwordPattern: passwordRegex.test(newPassword)
        });

        try {
            await userService.changePassword(currentPassword, newPassword);
            alert('비밀번호가 성공적으로 변경되었습니다. 보안을 위해 다시 로그인해주세요.');

            // 비밀번호 변경 후 로그아웃 처리
            localStorage.clear();
            navigate('/login');
        } catch (error) {
            console.error('비밀번호 변경 실패:', error);
            console.error('응답 데이터:', error.response?.data);

            if (error.response?.status === 401) {
                alert('인증이 만료되었습니다. 다시 로그인해주세요.');
                localStorage.clear();
                navigate('/login');
            } else if (error.response?.status === 400) {
                const errorMessage = error.response?.data?.message;
                if (errorMessage) {
                    alert(errorMessage);
                } else {
                    alert('현재 비밀번호가 올바르지 않거나 새 비밀번호 형식이 맞지 않습니다.');
                }
            } else {
                alert('비밀번호 변경에 실패했습니다. 다시 시도해주세요.');
            }
        }
    };

    const resetPasswordChangeForm = () => {
        setCurrentPassword('');
        setNewPassword('');
        setConfirmNewPassword('');
        setShowPasswordChange(false);
    };

    // 회원탈퇴 처리
    const handleDeleteAccount = async () => {
        if (!deletePassword) {
            alert('비밀번호를 입력해주세요.');
            return;
        }

        if (deleteConfirmation !== '회원탈퇴') {
            alert('"회원탈퇴"를 정확히 입력해주세요.');
            return;
        }

        try {
            await userService.deleteUserAccount(deletePassword, deleteConfirmation);
            alert('회원탈퇴가 처리되었습니다.');
            localStorage.clear();
            navigate('/login');
        } catch (error) {
            console.error('회원탈퇴 실패:', error);
            console.error('응답 데이터:', error.response?.data);

            if (error.response?.status === 400) {
                const errorMessage = error.response?.data?.message;
                if (errorMessage) {
                    alert(errorMessage);
                } else {
                    alert('입력한 정보를 다시 확인해주세요. (비밀번호 또는 확인 문구 오류)');
                }
            } else if (error.response?.status === 401) {
                alert('비밀번호가 올바르지 않습니다.');
            } else {
                alert('회원탈퇴에 실패했습니다. 다시 시도해주세요.');
            }
        }
    };

    // 사용자 입력 검증
    const validateProfile = () => {
        const errors = {};

        // 사용자 이름 검증
        if (!userProfile.userName || userProfile.userName.length < 2 || userProfile.userName.length > 50) {
            errors.userName = '사용자 이름은 2자 이상 50자 이하여야 합니다.';
        }

        // 핸드폰 번호 검증
        const phoneRegex = /^010-[0-9]{4}-[0-9]{4}$/;
        if (!userProfile.userNumber || !phoneRegex.test(userProfile.userNumber)) {
            errors.userNumber = '핸드폰 번호는 010-XXXX-XXXX 형식이어야 합니다.';
        }

        return errors;
    };

    const handleSaveProfile = async () => {
        // 유효성 검증
        const validationErrors = validateProfile();
        if (Object.keys(validationErrors).length > 0) {
            const errorMessages = Object.values(validationErrors).join('\n');
            alert('입력 오류:\n' + errorMessages);
            return;
        }

        try {
            setSaving(true);
            await userService.updateUserProfile({
                userName: userProfile.userName,
                userNumber: userProfile.userNumber
            });
            setSaving(false);
            alert('프로필이 저장되었습니다.');
        } catch (error) {
            setSaving(false);
            console.error('프로필 저장 실패:', error);
            alert('프로필 저장에 실패했습니다.');
        }
    };

    if (loading) {
        return (
            <MainLayout>
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '70vh' }}>
                    <CircularProgress />
                </Box>
            </MainLayout>
        );
    }

    if (!userProfile) {
        return (
            <MainLayout>
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '70vh' }}>
                    <Typography variant="h6" color="error">
                        사용자 정보를 불러올 수 없습니다.
                    </Typography>
                </Box>
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <Box sx={{ p: 3, maxWidth: 1000, mx: 'auto' }}>
                <Typography variant="h4" sx={{ mb: 4, textAlign: 'center' }}>
                    프로필 관리
                </Typography>

                {/* 기본 정보 섹션 */}
                <Card sx={{ mb: 3 }}>
                    <CardHeader title="기본 정보" />
                    <CardContent>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            {/* 사용자 ID (읽기 전용) */}
                            <TextField
                                label="사용자 ID"
                                name="userId"
                                value={userProfile.userId}
                                fullWidth
                                disabled
                                size="small"
                                InputLabelProps={{ shrink: !!userProfile.userId }}
                            />

                            {/* 사용자 이름 (수정 가능) */}
                            <TextField
                                label="사용자 이름"
                                name="userName"
                                value={userProfile.userName}
                                onChange={handleChange}
                                fullWidth
                                helperText="2자 이상 50자 이하로 입력해주세요."
                                InputLabelProps={{ shrink: !!userProfile.userName }}
                            />

                            {/* 이메일 (수정 불가) */}
                            <TextField
                                label="이메일"
                                name="userEmail"
                                value={userProfile.userEmail}
                                onChange={handleChange}
                                fullWidth
                                disabled
                                helperText="이메일은 변경할 수 없습니다."
                                InputLabelProps={{ shrink: !!userProfile.userEmail }}
                            />

                            {/* 핸드폰 번호 (수정 가능) */}
                            <TextField
                                label="핸드폰 번호"
                                name="userNumber"
                                value={userProfile.userNumber}
                                onChange={handleChange}
                                fullWidth
                                placeholder="010-XXXX-XXXX"
                                helperText="010-XXXX-XXXX 형식으로 입력해주세요."
                                InputLabelProps={{ shrink: !!userProfile.userNumber }}
                            />

                            {/* 사용자 권한 (읽기 전용) */}
                            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                                <Typography variant="body2">사용자 권한:</Typography>
                                <Chip
                                    label={userProfile.userRole === 'ADMIN' ? '관리자' : '일반 사용자'}
                                    color={userProfile.userRole === 'ADMIN' ? 'error' : 'primary'}
                                    size="small"
                                />
                            </Box>

                            {/* 계정 유형 (읽기 전용) */}
                            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                                <Typography variant="body2">계정 유형:</Typography>
                                <Chip
                                    label={userProfile.accountType === 'local' ? '로컬 계정' :
                                           userProfile.accountType === 'social' ? '소셜 계정' : '통합 계정'}
                                    color={userProfile.accountType === 'integrated' ? 'success' : 'default'}
                                    size="small"
                                />
                            </Box>

                            {/* 가입일 (읽기 전용) */}
                            <TextField
                                label="가입일"
                                name="createdAt"
                                value={new Date(userProfile.createdAt).toLocaleDateString('ko-KR', {
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit'
                                })}
                                fullWidth
                                disabled
                                size="small"
                                InputLabelProps={{ shrink: !!userProfile.createdAt }}
                            />

                            {/* 최근 로그인 (읽기 전용) */}
                            <TextField
                                label="최근 로그인"
                                name="lastLoginAt"
                                value={new Date(userProfile.lastLoginAt).toLocaleDateString('ko-KR', {
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit'
                                })}
                                fullWidth
                                disabled
                                size="small"
                                InputLabelProps={{ shrink: !!userProfile.lastLoginAt }}
                            />
                            <Button
                                variant="contained"
                                onClick={handleSaveProfile}
                                disabled={saving}
                                sx={{ mt: 2, alignSelf: 'flex-start' }}
                            >
                                {saving ? '저장 중...' : '기본 정보 저장'}
                            </Button>
                        </Box>
                    </CardContent>
                </Card>

                {/* 비밀번호 변경 섹션 */}
                {(userProfile.accountType === 'local' || userProfile.accountType === 'integrated') && (
                    <Card sx={{ mb: 3 }}>
                        <CardHeader title="비밀번호 변경" />
                        <CardContent>
                            <Alert severity="info" sx={{ mb: 2 }}>
                                현재 비밀번호를 확인 후 새 비밀번호로 변경합니다. 변경 후 보안을 위해 재로그인이 필요합니다.
                            </Alert>
                            {!showPasswordChange ? (
                                <Button variant="outlined" onClick={() => setShowPasswordChange(true)}>
                                    비밀번호 변경하기
                                </Button>
                            ) : (
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                    <TextField
                                        label="현재 비밀번호"
                                        type="password"
                                        value={currentPassword}
                                        onChange={(e) => setCurrentPassword(e.target.value)}
                                        fullWidth
                                        placeholder="현재 비밀번호를 입력하세요"
                                    />
                                    <TextField
                                        label="새 비밀번호"
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        fullWidth
                                        placeholder="6-20자, 대소문자/숫자/특수문자 포함"
                                        helperText="6-20자의 대소문자, 숫자, 특수문자(@$!%*?&) 포함 필수"
                                    />
                                    <TextField
                                        label="새 비밀번호 확인"
                                        type="password"
                                        value={confirmNewPassword}
                                        onChange={(e) => setConfirmNewPassword(e.target.value)}
                                        fullWidth
                                        placeholder="새 비밀번호를 다시 입력하세요"
                                        error={newPassword !== '' && confirmNewPassword !== '' && newPassword !== confirmNewPassword}
                                        helperText={newPassword !== '' && confirmNewPassword !== '' && newPassword !== confirmNewPassword ? '비밀번호가 일치하지 않습니다' : ''}
                                    />
                                    <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                                        <Button
                                            variant="contained"
                                            onClick={handlePasswordChange}
                                            disabled={!currentPassword || !newPassword || !confirmNewPassword || newPassword !== confirmNewPassword}
                                        >
                                            비밀번호 변경
                                        </Button>
                                        <Button variant="outlined" onClick={resetPasswordChangeForm}>
                                            취소
                                        </Button>
                                    </Box>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                )}

                {/* 계정 통합 섹션 */}
                {userProfile.accountType === 'social' && (
                    <Card sx={{ mb: 3 }}>
                        <CardHeader title="계정 통합" />
                        <CardContent>
                            <Alert severity="info" sx={{ mb: 2 }}>
                                소셜 계정을 로컬 계정과 통합하여 이메일로도 로그인할 수 있습니다.
                            </Alert>
                            {!showAccountIntegration ? (
                                <Button variant="outlined" onClick={() => setShowAccountIntegration(true)}>
                                    계정 통합하기
                                </Button>
                            ) : (
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                    {!isCodeSent ? (
                                        <>
                                            <TextField
                                                label="통합할 이메일"
                                                type="email"
                                                value={integrationEmail}
                                                onChange={(e) => setIntegrationEmail(e.target.value)}
                                                fullWidth
                                                helperText="계정 통합에 사용할 이메일을 입력하세요."
                                            />
                                            <Box sx={{ display: 'flex', gap: 2 }}>
                                                <Button variant="contained" onClick={handleSendCode}>
                                                    인증 코드 발송
                                                </Button>
                                                <Button variant="outlined" onClick={() => setShowAccountIntegration(false)}>
                                                    취소
                                                </Button>
                                            </Box>
                                        </>
                                    ) : (
                                        <>
                                            <TextField
                                                label="통합할 이메일"
                                                type="email"
                                                value={integrationEmail}
                                                fullWidth
                                                disabled
                                            />
                                            <TextField
                                                label="인증 코드"
                                                value={verificationCode}
                                                onChange={(e) => setVerificationCode(e.target.value)}
                                                fullWidth
                                                helperText="이메일로 발송된 인증 코드를 입력하세요."
                                            />
                                            <TextField
                                                label="새 비밀번호 설정"
                                                type="password"
                                                value={integrationPassword}
                                                onChange={(e) => setIntegrationPassword(e.target.value)}
                                                fullWidth
                                                helperText="6-20자의 대소문자, 숫자, 특수문자(@$!%*?&) 포함"
                                            />
                                            <Box sx={{ display: 'flex', gap: 2 }}>
                                                <Button variant="contained" onClick={handleCompleteIntegration}>
                                                    통합 완료
                                                </Button>
                                                <Button variant="outlined" onClick={() => setIsCodeSent(false)}>
                                                    이전
                                                </Button>
                                            </Box>
                                        </>
                                    )}
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                )}

                {/* 회원탈퇴 섹션 */}
                <Card sx={{ mb: 3, borderColor: 'error.main' }}>
                    <CardHeader title="회원탈퇴" sx={{ color: 'error.main' }} />
                    <CardContent>
                        <Alert severity="warning" sx={{ mb: 2 }}>
                            회원탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
                        </Alert>
                        {!showDeleteAccount ? (
                            <Button
                                variant="outlined"
                                color="error"
                                onClick={() => setShowDeleteAccount(true)}
                            >
                                회원탈퇴
                            </Button>
                        ) : (
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <TextField
                                    label="비밀번호 확인"
                                    type="password"
                                    value={deletePassword}
                                    onChange={(e) => setDeletePassword(e.target.value)}
                                    fullWidth
                                />
                                <TextField
                                    label='탈퇴 확인 (정확히 "회원탈퇴"를 입력하세요)'
                                    value={deleteConfirmation}
                                    onChange={(e) => setDeleteConfirmation(e.target.value)}
                                    fullWidth
                                />
                                <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                                    <Button
                                        variant="contained"
                                        color="error"
                                        onClick={handleDeleteAccount}
                                        disabled={deleteConfirmation !== '회원탈퇴'}
                                    >
                                        탈퇴하기
                                    </Button>
                                    <Button variant="outlined" onClick={() => setShowDeleteAccount(false)}>
                                        취소
                                    </Button>
                                </Box>
                            </Box>
                        )}
                    </CardContent>
                </Card>
            </Box>
        </MainLayout>
    );
}
