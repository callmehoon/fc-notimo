import React from 'react';
import ChatArea from '../components/TemplateGenerator/ChatArea';
import TemplatePreviewArea from '../components/TemplateGenerator/TemplatePreviewArea';
import '../styles/TemplateGenerator.css';

function TemplateGeneratorPage() {
  return (
    <div className="template-generator-container">
      <div className="chat-area-wrapper">
        <ChatArea />
      </div>
      <div className="preview-area-wrapper">
        <TemplatePreviewArea />
      </div>
    </div>
  );
}

export default TemplateGeneratorPage;
