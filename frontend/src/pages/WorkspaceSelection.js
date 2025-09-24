// pages/WorkspaceSelection.js
import React, { useState, useEffect } from 'react';
import { Box, Typography, Container, IconButton, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import CommonButton from '../components/button/CommonButton';
import WorkspaceList from '../components/layout/WorkspaceList';
import workspaceService from '../services/workspaceService';

const WorkspaceSelection = () => {
    const navigate = useNavigate();
    const [workspaces, setWorkspaces] = useState([]);
    const [selectedWorkspace, setSelectedWorkspace] = useState(null);
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchWorkspaces = async () => {
            try {
                setLoading(true);
                const data = await workspaceService.getWorkspaces();
                const mappedData = data.map(w => ({ id: w.workspaceId, name: w.workspaceName, subname: w.workspaceSubname }));
                setWorkspaces(mappedData);
                if (mappedData.length > 0) {
                    setSelectedWorkspace(mappedData[0]);
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
            // alert(`${selectedWorkspace.name}가 선택되었습니다.`);
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
