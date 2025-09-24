import React from 'react';
import { Box, Typography, Button, Paper } from '@mui/material';

function TemplatePreviewArea({ template }) {
  const handleSaveTemplate = () => {
    // TODO: Implement save functionality. This should likely be lifted up
    // to the parent component and passed down as a prop.
    console.log('Saving template:', template);
    alert('템플릿 저장 기능은 아직 구현되지 않았습니다.');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '10px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <Typography variant="h6">템플릿 미리보기</Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={handleSaveTemplate}
        >
          저장하기
        </Button>
      </div>
      <Paper elevation={2} style={{
        flexGrow: 1,
        padding: '20px',
        backgroundColor: 'white',
        overflowY: 'auto',
        whiteSpace: 'pre-wrap',
      }}>
        <Typography variant="h5" gutterBottom>
          {template.title}
        </Typography>
        <Typography variant="body1">
          {template.text}
        </Typography>
        {template.button_name && (
          <Box sx={{ mt: 3, textAlign: 'center' }}>
            <Button variant="contained" sx={{ minWidth: '200px' }}>
              {template.button_name}
            </Button>
          </Box>
        )}
      </Paper>
    </div>
  );
}

export default TemplatePreviewArea;
