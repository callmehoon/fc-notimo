import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';

// 목업 데이터 (실제로는 API 호출 등으로 가져올 데이터)
const mockContacts = [
  { id: '1', name: '수신인1', phone: '010-1111-1111', memo: '팀장님' },
  { id: '2', name: '수신인2', phone: '010-2222-2222', memo: '개인 연락처' },
  { id: '3', name: '수신인3', phone: '010-3333-3333', memo: '거래처' },
];

export default function ContactEditPage() {
  const { id } = useParams(); // URL 파라미터에서 연락처 ID 가져오기
  const navigate = useNavigate();
  const [contact, setContact] = useState({ name: '', phone: '', memo: '' });

  useEffect(() => {
    // ID를 사용하여 연락처 데이터 로드 (목업 데이터에서 찾기)
    const foundContact = mockContacts.find(c => c.id === id);
    if (foundContact) {
      setContact(foundContact);
    } else {
      // 연락처를 찾을 수 없을 경우 처리 (예: 404 페이지로 리다이렉트)
      console.error(`Contact with ID ${id} not found.`);
      // navigate('/contact-management'); // 연락처 관리 페이지로 돌아가기
    }
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setContact(prevContact => ({
      ...prevContact,
      [name]: value,
    }));
  };

  const handleSave = () => {
    console.log('연락처 저장:', contact);
    alert('연락처가 저장되었습니다! (실제 저장 로직은 구현되지 않았습니다.)');
    navigate('/contact-management'); // 저장 후 연락처 관리 페이지로 돌아가기
  };

  const handleCancel = () => {
    navigate('/contact-management'); // 취소 시 연락처 관리 페이지로 돌아가기
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', bgcolor: '#f0f2f5' }}>
      <Paper elevation={3} sx={{ p: 4, maxWidth: 500, width: '100%', borderRadius: '8px' }}>
        <Typography variant="h5" component="h1" sx={{ mb: 3, textAlign: 'center' }}>
          연락처 수정
        </Typography>
        <TextField
          label="수신인 이름"
          name="name"
          variant="outlined"
          fullWidth
          value={contact.name}
          onChange={handleChange}
          sx={{ mb: 2 }}
        />
        <TextField
          label="수신인 전화번호"
          name="phone"
          variant="outlined"
          fullWidth
          value={contact.phone}
          onChange={handleChange}
          sx={{ mb: 2 }}
        />
        <TextField
          label="수신인 메모"
          name="memo"
          variant="outlined"
          fullWidth
          multiline
          rows={3}
          value={contact.memo}
          onChange={handleChange}
          sx={{ mb: 3 }}
        />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
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
