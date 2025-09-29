// pages/WorkspaceSelection.js
import React, { useState, useEffect } from 'react';
import { Box, Typography, Container, IconButton, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate, useSearchParams } from 'react-router-dom';
import CommonButton from '../components/button/CommonButton';
import WorkspaceList from '../components/layout/WorkspaceList';
import workspaceService from '../services/workspaceService';
import logo from "../assets/logo.png";

const WorkspaceSelection = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [workspaces, setWorkspaces] = useState([]);
    const [selectedWorkspace, setSelectedWorkspace] = useState(null);
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 소셜 로그인 성공 처리
    useEffect(() => {
        const success = searchParams.get('success');
        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');
        const isNewUser = searchParams.get('isNewUser');
        const accountIntegrated = searchParams.get('accountIntegrated');
        const provider = searchParams.get('provider');

        // 소셜 로그인 관련 파라미터가 있으면 처리
        if (success || accountIntegrated) {
            // sessionStorage로 중복 처리 방지
            const socialLoginKey = `social_login_${Date.now()}`;
            const processed = sessionStorage.getItem('socialLoginProcessed');

            if (processed) {
                navigate('/workspace', { replace: true });
                return;
            }

            sessionStorage.setItem('socialLoginProcessed', socialLoginKey);

            // URL에서 모든 파라미터 제거
            navigate('/workspace', { replace: true });

            if (success === 'true' && accessToken && refreshToken && isNewUser === 'false') {
                // 기존 사용자 로그인 성공 - 토큰 정리 후 저장
                const cleanAccessToken = accessToken.split('&')[0].trim();
                const cleanRefreshToken = refreshToken.split('&')[0].trim();
                localStorage.setItem('accessToken', cleanAccessToken);
                localStorage.setItem('refreshToken', cleanRefreshToken);

                if (accountIntegrated === 'true') {
                    setTimeout(() => {
                        alert(`${provider} 계정이 기존 계정과 자동으로 연동되었습니다.`);
                        // 메시지 표시 후 sessionStorage 정리
                        sessionStorage.removeItem('socialLoginProcessed');
                    }, 100);
                } else {
                    // 메시지가 없는 경우에도 sessionStorage 정리
                    sessionStorage.removeItem('socialLoginProcessed');
                }
            } else {
                sessionStorage.removeItem('socialLoginProcessed');
            }
        }
    }, [searchParams, navigate]);

    useEffect(() => {
        const fetchWorkspaces = async () => {
            try {
                setLoading(true);
                const data = await workspaceService.getWorkspaces();
                setWorkspaces(data);
                if (data.length > 0) {
                    setSelectedWorkspace(data[0]);
                }
                setError(null);
            } catch (err) {
                setError('워크스페이스를 불러오는데 실패했습니다.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchWorkspaces();
    }, []);

    const handleCreateWorkspace = () => {
        navigate('/createWorkspace');
    };

    const handleConfirmSelection = () => {
        if (selectedWorkspace) {
            localStorage.setItem('selectedWorkspaceId', selectedWorkspace.workspaceId.toString());
            navigate('/publicTemplate');
        } else {
            alert('워크스페이스를 선택해주세요.');
        }
    };

    return (
        <Container component="main" maxWidth="xs" sx={{ mt: 8, mb: 4 }}>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 2,
                }}
            >
                <Box
                    sx={{
                        width: '150px',
                        height: '80px',

                    }}
                >
                    <img src={logo} alt="Logo" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </Box>

                <Typography component="h1" variant="h5">
                    워크스페이스 선택
                </Typography>

                <Box sx={{ width: '100%', my: 2 }}>
                    {loading ? (
                        <CircularProgress />
                    ) : error ? (
                        <Alert severity="error">{error}</Alert>
                    ) : workspaces.length > 0 && selectedWorkspace ? (
                        <WorkspaceList
                            allWorkspaces={workspaces}
                            selectedWorkspace={selectedWorkspace}
                            open={open}
                            onToggle={() => setOpen(!open)}
                            onSelect={(workspace) => {
                                setSelectedWorkspace(workspace);
                                setOpen(false);
                            }}
                        />
                    ) : (
                        <Typography>사용 가능한 워크스페이스가 없습니다. 새로 생성해주세요.</Typography>
                    )}
                </Box>

                <IconButton
                    onClick={handleCreateWorkspace}
                    sx={{ border: '1px solid #e0e0e0' }}
                >
                    <AddIcon />
                </IconButton>

                <CommonButton
                    fullWidth
                    variant="contained"
                    onClick={handleConfirmSelection}
                    sx={{ mt: 2 }}
                    disabled={!selectedWorkspace}
                >
                    선택
                </CommonButton>
            </Box>
        </Container>
    );
};

export default WorkspaceSelection;
