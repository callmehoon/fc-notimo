// pages/WorkspaceSelection.js
import React, { useState, useEffect } from 'react';
import { Box, Typography, Container, IconButton, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import CommonButton from '../components/button/CommonButton';
import WorkspaceList from '../components/layout/WorkspaceList';
import workspaceService from '../services/workspaceService';
import logo from "../assets/logo.png";

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
