import React, { useState } from 'react';
import {
    Box,
    Typography,
    Grid,
    Paper,
    Alert,
    CircularProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import MainLayout from '../components/layout/MainLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';
import workspaceService from '../services/workspaceService';

const CreateWorkspace = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        workspaceName: '',
        workspaceSubname: '',
        workspaceAddress: '',
        workspaceDetailAddress: '',
        workspaceUrl: '',
        representerName: '',
        representerPhoneNumber: '',
        representerEmail: '',
        companyName: '',
        companyRegisterNumber: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await workspaceService.createWorkspace(formData);
            alert('워크스페이스가 성공적으로 생성되었습니다.');
            navigate('/workspace');
        } catch (err) {
            setError(err.response?.data?.message || '워크스페이스 생성에 실패했습니다.');
            console.error('Error creating workspace:', err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <MainLayout>
            <Grid container justifyContent="center" sx={{ mt: 4 }}>
                <Grid item xs={12} sm={8} md={8}>
                    <Paper elevation={3} sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <Box
                            sx={{
                                width: 80,
                                height: 80,
                                backgroundColor: 'grey.200',
                                mb: 2,
                            }}
                        />
                        <Typography component="h1" variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
                            워크스페이스 생성
                        </Typography>
                        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
                            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                            <CommonTextField
                                name="workspaceName"
                                label="워크스페이스 명"
                                value={formData.workspaceName}
                                onChange={handleChange}
                                required
                                fullWidth
                                autoFocus
                            />
                            <CommonTextField
                                name="workspaceSubname"
                                label="워크스페이스 별칭"
                                value={formData.workspaceSubname}
                                onChange={handleChange}
                                fullWidth
                            />
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <CommonTextField
                                        name="workspaceAddress"
                                        label="주소"
                                        value={formData.workspaceAddress}
                                        onChange={handleChange}
                                        fullWidth
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <CommonTextField
                                        name="workspaceDetailAddress"
                                        label="상세 주소"
                                        value={formData.workspaceDetailAddress}
                                        onChange={handleChange}
                                        fullWidth
                                    />
                                </Grid>
                            </Grid>
                            <CommonTextField
                                name="workspaceUrl"
                                label="워크스페이스 고유 URL"
                                value={formData.workspaceUrl}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representerName"
                                label="대표자 이름"
                                value={formData.representerName}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representerPhoneNumber"
                                label="대표 전화번호"
                                value={formData.representerPhoneNumber}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representerEmail"
                                label="대표 이메일"
                                type="email"
                                value={formData.representerEmail}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="companyName"
                                label="사업자 명"
                                value={formData.companyName}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="companyRegisterNumber"
                                label="사업자 등록 번호"
                                value={formData.companyRegisterNumber}
                                onChange={handleChange}
                                fullWidth
                            />
                            <CommonButton
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                                disabled={loading}
                            >
                                {loading ? <CircularProgress size={24} /> : '생성'}
                            </CommonButton>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
        </MainLayout>
    );
};

export default CreateWorkspace;