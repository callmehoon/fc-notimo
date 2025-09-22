import React from 'react';

function TemplatePreviewArea() {
  const handleSaveTemplate = () => {
    console.log('템플릿 저장하기 버튼 클릭됨');
    alert('템플릿이 저장되었습니다! (실제 저장 로직은 구현되지 않았습니다.)');
  };

  const templateContent = `
쿠폰이 곧 만료됨을 알립니다.

쿠폰 이름 : (쿠폰이름)
만료 일자 : (만료일자)
`;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
        <button
          onClick={handleSaveTemplate}
          style={{ padding: '8px 15px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          저장하기
        </button>
      </div>
      <div style={{
        flexGrow: 1,
        border: '1px solid #ddd',
        borderRadius: '8px',
        padding: '20px',
        backgroundColor: 'white',
        whiteSpace: 'pre-wrap', // Preserve whitespace and line breaks
        overflowY: 'auto'
      }}>
        {templateContent}
      </div>
    </div>
  );
}

export default TemplatePreviewArea;
