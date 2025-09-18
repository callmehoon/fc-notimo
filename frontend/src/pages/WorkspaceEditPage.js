import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Typography, Paper, Grid } from '@mui/material'; // Added Grid
import { useParams, useNavigate } from 'react-router-dom';

// 목업 데이터 (실제로는 API 호출 등으로 가져올 워크스페이스 정보)
const mockWorkspaces = [
  {
    id: '1',
    workspaceName: '기본 워크스페이스',
    workspaceAlias: '기본',
    address: '서울시 강남구 테헤란로 123',
    addressDetail: 'ABC빌딩 10층',
    workspaceUrl: 'default-ws',
    representativeName: '홍길동',
    representativePhone: '010-1111-2222',
    representativeEmail: 'hong@example.com',
    businessName: '기본회사',
    businessRegNumber: '123-45-67890',
  },
  {
    id: '2',
    workspaceName: '팀 프로젝트 A',
    workspaceAlias: '팀A',
    address: '서울시 서초구 서초대로 456',
    addressDetail: 'DEF빌딩 5층',
    workspaceUrl: 'team-a-project',
    representativeName: '김철수',
    representativePhone: '010-3333-4444',
    representativeEmail: 'kim@example.com',
    businessName: 'A프로젝트',
    businessRegNumber: '987-65-43210',
  },
];

export default function WorkspaceEditPage() {
  const { id } = useParams(); // URL 파라미터에서 워크스페이스 ID 가져오기
  const navigate = useNavigate();
  const [workspace, setWorkspace] = useState({
    workspaceName: '',
    workspaceAlias: '',
    address: '',
    addressDetail: '',
    workspaceUrl: '',
    representativeName: '',
    representativePhone: '',
    representativeEmail: '',
    businessName: '',
    businessRegNumber: '',
  });

  useEffect(() => {
    // ID를 사용하여 워크스페이스 데이터 로드 (목업 데이터에서 찾기)
    const foundWorkspace = mockWorkspaces.find(ws => ws.id === id);
    if (foundWorkspace) {
      setWorkspace(foundWorkspace);
    } else {
      console.error(`Workspace with ID ${id} not found.`);
      // navigate('/workspace'); // 워크스페이스 선택 페이지로 돌아가기
    }
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setWorkspace(prevWorkspace => ({
      ...prevWorkspace,
      [name]: value,
    }));
  };

  const handleSave = () => {
    console.log('워크스페이스 저장:', workspace);
    alert('워크스페이스 정보가 저장되었습니다! (실제 저장 로직은 구현되지 않았습니다.)');
    navigate(-1); // 저장 후 이전 페이지로 돌아가기
  };

  const handleCancel = () => {
    navigate(-1); // 취소 시 이전 페이지로 돌아가기
  };

  const handleDelete = () => {
    console.log('워크스페이스 삭제:', id);
    alert('워크스페이스가 삭제되었습니다! (실제 삭제 로직은 구현되지 않았습니다.)');
    navigate(-1); // 삭제 후 이전 페이지로 돌아가기
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', bgcolor: '#f0f2f5' }}>
      <Paper elevation={3} sx={{ p: 4, maxWidth: 600, width: '100%', borderRadius: '8px', position: 'relative' }}> {/* Increased maxWidth */}
        <Typography variant="h5" component="h1" sx={{ mb: 3, textAlign: 'center' }}>
          워크스페이스 관리
        </Typography>
        <Button
          variant="contained"
          color="error"
          onClick={handleDelete}
          sx={{
            position: 'absolute',
            top: 16,
            right: 16,
          }}
        >
          삭제
        </Button>
        <Box component="form" onSubmit={(e) => e.preventDefault()} noValidate sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
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
            name="workspaceAlias"
            variant="outlined"
            fullWidth
            value={workspace.workspaceAlias}
            onChange={handleChange}
          />
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField
                label="주소"
                name="address"
                variant="outlined"
                fullWidth
                value={workspace.address}
                onChange={handleChange}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                label="상세 주소"
                name="addressDetail"
                variant="outlined"
                fullWidth
                value={workspace.addressDetail}
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
            name="representativeName"
            variant="outlined"
            fullWidth
            value={workspace.representativeName}
            onChange={handleChange}
            required
          />
          <TextField
            label="대표 전화번호"
            name="representativePhone"
            variant="outlined"
            fullWidth
            value={workspace.representativePhone}
            onChange={handleChange}
            required
          />
          <TextField
            label="대표 이메일"
            name="representativeEmail"
            type="email"
            variant="outlined"
            fullWidth
            value={workspace.representativeEmail}
            onChange={handleChange}
          />
          <TextField
            label="사업자 명"
            name="businessName"
            variant="outlined"
            fullWidth
            value={workspace.businessName}
            onChange={handleChange}
            required
          />
          <TextField
            label="사업자 등록 번호"
            name="businessRegNumber"
            variant="outlined"
            fullWidth
            value={workspace.businessRegNumber}
            onChange={handleChange}
          />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, mt: 3 }}>
          <Button variant="contained" color="primary" onClick={handleSave} fullWidth>
            저장
          </Button>
          <Button variant="outlined" color="secondary" onClick={handleCancel} fullWidth>
            취소
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
