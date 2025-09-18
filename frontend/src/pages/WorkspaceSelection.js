// pages/WorkspaceSelection.js

import React from 'react';
import { Box, Typography, Container, IconButton } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import CommonButton from '../components/button/CommonButton';

// 기존 WorkspaceList 컴포넌트는 수정 없이 그대로 가져옵니다.
import WorkspaceList from '../components/layout/WorkspaceList';

// 목업 데이터 (원래 WorkspaceList에 있던 데이터)
const allWorkspaces = ['워크스페이스 1', '워크스페이스 2', '워크스페이스 3'];

const WorkspaceSelection = () => {
    const navigate = useNavigate();
    // 부모가 상태를 관리합니다.
    const [selectedWorkspace, setSelectedWorkspace] = React.useState(allWorkspaces[0]);
    const [open, setOpen] = React.useState(false);


    const handleCreateWorkspace = () => {
        navigate('/createWorkspace');
    };

    const handleConfirmSelection = () => {
        // 이제 선택된 워크스페이스 값을 정확히 알 수 있습니다.
        alert(`${selectedWorkspace}가 선택되었습니다.`);
        // 예: navigate(`/dashboard`);
    };

    return (
        <Container component="main" maxWidth="xs" sx={{ mt: 8, mb: 4 }}>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 2, // 컴포넌트 간의 수직 간격
                }}
            >
                {/* 로고 placeholder */}
                <Box
                    sx={{
                        width: 80,
                        height: 80,
                        backgroundColor: '#e0e0e0',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mb: 1,
                    }}
                >
                    <Typography variant="body2" color="text.secondary">로고</Typography>
                </Box>

                <Typography component="h1" variant="h5">
                    워크스페이스 선택
                </Typography>

                {/* 기존 WorkspaceList (드롭다운) 컴포넌트 */}
                <Box sx={{ width: '100%', my: 2 }}>
                    <WorkspaceList
                        allWorkspaces={allWorkspaces}
                        selectedWorkspace={selectedWorkspace}
                        open={open}
                        onToggle={() => setOpen(!open)}
                        onSelect={(workspace) => {
                            setSelectedWorkspace(workspace);
                            setOpen(false);
                        }}
                    />
                </Box>

                {/* 워크스페이스 추가 버튼 */}
                <IconButton
                    onClick={handleCreateWorkspace}
                    sx={{ border: '1px solid #e0e0e0' }}
                >
                    <AddIcon />
                </IconButton>

                {/* 최종 선택 버튼 */}
                <CommonButton
                    fullWidth
                    onClick={handleConfirmSelection}
                    sx={{ mt: 2 }}
                >
                    선택
                </CommonButton>
            </Box>
        </Container>
    );
};

export default WorkspaceSelection;
