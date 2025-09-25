import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Typography, Paper, Grid, CircularProgress, Alert } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import workspaceService from '../services/workspaceService';

export default function WorkspaceEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [workspace, setWorkspace] = useState(null);
    const [allWorkspaces, setAllWorkspaces] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchWorkspaceData = async () => {
            try {
                setLoading(true);
                const [workspaceData, allWorkspacesData] = await Promise.all([
                    workspaceService.getWorkspaceById(id),
                    workspaceService.getWorkspaces(),
                ]);
                setWorkspace(workspaceData);
                setAllWorkspaces(allWorkspacesData);
                setError(null);
            } catch (err) {
                setError('워크스페이스 정보를 불러오는 데 실패했습니다.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchWorkspaceData();
    }, [id]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setWorkspace(prevWorkspace => ({
            ...prevWorkspace,
            [name]: value,
        }));
    };

    const handleSave = async () => {
        setLoading(true);
        setError(null);

        // Backend UpdateDTO format requires fields to be prefixed with 'new'
        const updateData = {
            newWorkspaceName: workspace.workspaceName,
            newWorkspaceSubname: workspace.workspaceSubname,
            newWorkspaceAddress: workspace.workspaceAddress,
            newWorkspaceDetailAddress: workspace.workspaceDetailAddress,
            newWorkspaceUrl: workspace.workspaceUrl,
            newRepresenterName: workspace.representerName,
            newRepresenterPhoneNumber: workspace.representerPhoneNumber,
            newRepresenterEmail: workspace.representerEmail,
            newCompanyName: workspace.companyName,
            newCompanyRegisterNumber: workspace.companyRegisterNumber,
        };

        try {
            await workspaceService.updateWorkspace(id, updateData);
            alert('워크스페이스 정보가 성공적으로 저장되었습니다.');
            navigate(-1);
        } catch (err) {
            setError(err.response?.data?.message || '워크스페이스 정보 저장에 실패했습니다.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate(-1);
    };

    const handleDelete = async () => {
        if (window.confirm('정말로 이 워크스페이스를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            setLoading(true);
            setError(null);
            try {
                await workspaceService.deleteWorkspace(id);
                alert('워크스페이스가 삭제되었습니다.');
                if (allWorkspaces.length <= 1) {
                    navigate('/workspace');
                } else {
                    navigate(-1);
                }
            } catch (err) {
                setError(err.response?.data?.message || '워크스페이스 삭제에 실패했습니다.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        }
    };

    if (loading && !workspace) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error && !workspace) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    if (!workspace) {
        return null; // Or a not found component
    }

    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', bgcolor: '#f0f2f5' }}>
            <Paper elevation={3} sx={{ p: 4, maxWidth: 600, width: '100%', borderRadius: '8px', position: 'relative' }}>
                <Typography variant="h5" component="h1" sx={{ mb: 3, textAlign: 'center' }}>
                    워크스페이스 관리
                </Typography>
                <Button
                    variant="contained"
                    color="error"
                    onClick={handleDelete}
                    disabled={loading}
                    sx={{
                        position: 'absolute',
                        top: 16,
                        right: 16,
                    }}
                >
                    삭제
                </Button>
                <Box component="form" onSubmit={(e) => e.preventDefault()} noValidate sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}
                    <TextField
                        label="워크스페이스 명"
                        name="workspaceName"
                        variant="outlined"
                        fullWidth
                        value={workspace.workspaceName}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        label="워크스페이스 별칭"
                        name="workspaceSubname"
                        variant="outlined"
                        fullWidth
                        value={workspace.workspaceSubname}
                        onChange={handleChange}
                    />
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="주소"
                                name="workspaceAddress"
                                variant="outlined"
                                fullWidth
                                value={workspace.workspaceAddress}
                                onChange={handleChange}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="상세 주소"
                                name="workspaceDetailAddress"
                                variant="outlined"
                                fullWidth
                                value={workspace.workspaceDetailAddress}
                                onChange={handleChange}
                            />
                        </Grid>
                    </Grid>
                    <TextField
                        label="워크스페이스 고유 URL"
                        name="workspaceUrl"
                        variant="outlined"
                        fullWidth
                        value={workspace.workspaceUrl}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        label="대표자 이름"
                        name="representerName"
                        variant="outlined"
                        fullWidth
                        value={workspace.representerName}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        label="대표 전화번호"
                        name="representerPhoneNumber"
                        variant="outlined"
                        fullWidth
                        value={workspace.representerPhoneNumber}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        label="대표 이메일"
                        name="representerEmail"
                        type="email"
                        variant="outlined"
                        fullWidth
                        value={workspace.representerEmail}
                        onChange={handleChange}
                    />
                    <TextField
                        label="사업자 명"
                        name="companyName"
                        variant="outlined"
                        fullWidth
                        value={workspace.companyName}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        label="사업자 등록 번호"
                        name="companyRegisterNumber"
                        variant="outlined"
                        fullWidth
                        value={workspace.companyRegisterNumber}
                        onChange={handleChange}
                    />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, mt: 3 }}>
                    <Button variant="contained" color="primary" onClick={handleSave} disabled={loading} fullWidth>
                        {loading ? <CircularProgress size={24} /> : '저장'}
                    </Button>
                    <Button variant="outlined" color="secondary" onClick={handleCancel} disabled={loading} fullWidth>
                        취소
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
}
