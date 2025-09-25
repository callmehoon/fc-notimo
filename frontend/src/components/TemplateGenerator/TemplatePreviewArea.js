import React from 'react';
import { Box, Typography, Button, Paper, Chip, CircularProgress } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import ErrorIcon from '@mui/icons-material/Error';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';

function TemplatePreviewArea({ template, validationResult, validationLoading }) {
  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate('/mytemplate');
  };

  const renderValidationResult = () => {
    if (validationLoading) {
      return (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
          <CircularProgress size={20} />
          <Typography variant="body2">승인 검증 중...</Typography>
        </Box>
      );
    }

    if (!validationResult) return null;

    const { result, probability } = validationResult;

    if (result === 'approve') {
      return (
        <Chip
          icon={<CheckCircleIcon />}
          label={`승인 예상 (${probability})`}
          color="success"
          variant="outlined"
          sx={{ mt: 2 }}
        />
      );
    } else if (result === 'reject') {
      return (
        <Chip
          icon={<CancelIcon />}
          label={`거부 예상 (${probability})`}
          color="error"
          variant="outlined"
          sx={{ mt: 2 }}
        />
      );
    } else if (result === 'error') {
      return (
        <Chip
          icon={<ErrorIcon />}
          label={probability}
          color="warning"
          variant="outlined"
          sx={{ mt: 2 }}
        />
      );
    }

    return null;
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '10px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h6">템플릿 미리보기</Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary', opacity: 0.7 }}>
            템플릿은 자동 저장됩니다.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<ArrowBackIcon />}
          onClick={handleGoBack}
          size="small"
          sx={{ color: 'text.secondary', borderColor: 'text.secondary' }}
        >
          돌아가기
        </Button>
      </div>
      <Paper elevation={2} style={{
        flexGrow: 1,
        padding: '20px',
        backgroundColor: 'white',
        overflowY: 'auto',
        whiteSpace: 'pre-wrap',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
      }}>
        {template ? (
          <>
            <Typography variant="h5" gutterBottom>
              {template.title || template.individualTemplateTitle || '제목 없음'}
            </Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>
              {template.text || template.content || template.individualTemplateContent || '내용 없음'}
            </Typography>
            {(template.button_name || template.buttonTitle) && (
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <Button variant="contained" sx={{ minWidth: '200px' }}>
                  {template.button_name || template.buttonTitle}
                </Button>
              </Box>
            )}
            
            {/* 검증 결과 표시 */}
            <Box sx={{ mt: 3, textAlign: 'center' }}>
              {renderValidationResult()}
            </Box>
          </>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
            AI와 채팅을 시작하면 여기에 템플릿 미리보기가 표시됩니다.
          </Typography>
        )}
      </Paper>
    </div>
  );
}

export default TemplatePreviewArea;
