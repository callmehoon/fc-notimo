import React, { useState } from 'react';
import {
    Box,
    Typography,
    Grid,
    Paper
} from '@mui/material';
import MainLayout from '../components/layout/MainLayout';
import CommonTextField from '../components/form/CommonTextField';
import CommonButton from '../components/button/CommonButton';

const CreateWorkspace = () => {
    const [formData, setFormData] = useState({
        workspaceName: '',
        workspaceAlias: '',
        address: '',
        addressDetail: '',
        addressUrl: '',
        representativeName: '',
        representativePhone: '',
        representativeEmail: '',
        businessName: '',
        businessRegNumber: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value,
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        // TODO: 폼 제출 로직 (예: API 호출)
        console.log('워크스페이스 생성 데이터:', formData);
        alert('워크스페이스가 생성되었습니다.');
    };

    return (
        <MainLayout>
            <Grid container justifyContent="center" sx={{ mt: 4 }}>
                {/* 이 Grid item의 md 값을 조절하여 너비를 변경할 수 있습니다. (예: md={8} 로 하면 더 넓어짐) */}
                <Grid item xs={12} sm={8} md={8}>
                    <Paper elevation={3} sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        {/* 로고 영역 */}
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
                                name="workspaceAlias"
                                label="워크스페이스 별칭"
                                value={formData.workspaceAlias}
                                onChange={handleChange}
                                fullWidth
                            />
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <CommonTextField
                                        name="address"
                                        label="주소"
                                        value={formData.address}
                                        onChange={handleChange}
                                        fullWidth
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <CommonTextField
                                        name="addressDetail"
                                        label="상세 주소"
                                        value={formData.addressDetail}
                                        onChange={handleChange}
                                        fullWidth
                                    />
                                </Grid>
                            </Grid>
                            <CommonTextField
                                name="workspaceUrl"
                                label="워크스페이스 고유 URL"
                                value={formData.representativeName}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representativeName"
                                label="대표자 이름"
                                value={formData.representativeName}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representativePhone"
                                label="대표 전화번호"
                                value={formData.representativePhone}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="representativeEmail"
                                label="대표 이메일"
                                type="email"
                                value={formData.representativeEmail}
                                onChange={handleChange}
                                fullWidth
                            />
                            <CommonTextField
                                name="businessName"
                                label="사업자 명"
                                value={formData.businessName}
                                onChange={handleChange}
                                required
                                fullWidth
                            />
                            <CommonTextField
                                name="businessRegNumber"
                                label="사업자 등록 번호"
                                value={formData.businessRegNumber}
                                onChange={handleChange}
                                fullWidth
                            />
                            <CommonButton
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                            >
                                생성
                            </CommonButton>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
        </MainLayout>
    );
};

export default CreateWorkspace;