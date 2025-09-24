import React, { useState } from 'react';
import ChatArea from '../components/TemplateGenerator/ChatArea';
import TemplatePreviewArea from '../components/TemplateGenerator/TemplatePreviewArea';
import '../styles/TemplateGenerator.css';
import apiAi from '../services/apiAi';

function TemplateGeneratorPage() {
  // State lifted up to the parent component
  const [template, setTemplate] = useState({
    title: "기본 템플릿 제목",
    text: `쿠폰이 곧 만료됨을 알립니다.\n\n쿠폰 이름 : (쿠폰이름)\n만료 일자 : (만료일자)`,
    button_name: "쿠폰 사용하기"
  });
  const [chatHistory, setChatHistory] = useState([
    { type: 'bot', text: '안녕하세요. 템플릿 수정을 도와드릴게요. 어떤 변경을 원하시나요?' },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // API call handler
  const handleSendMessage = async (userInput) => {
    if (!userInput.trim()) return;

    const newUserMessage = { type: 'user', text: userInput };
    setChatHistory(prev => [...prev, newUserMessage]);
    setLoading(true);
    setError(null);

    const requestData = {
      original_template: template,
      user_input: userInput,
    };

    try {
      const response = await apiAi.post('/template/template', requestData);
      const { template: newTemplate, chat_response: chatResponse } = response.data;

      setTemplate(newTemplate);
      setChatHistory(prev => [...prev, { type: 'bot', text: chatResponse }]);
    } catch (err) {
      const errorMessage = 'API 호출에 실패했습니다. 다시 시도해주세요.';
      setError(errorMessage);
      setChatHistory(prev => [...prev, { type: 'bot', text: errorMessage }]);
      console.error('API call failed:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="template-generator-container">
      <div className="chat-area-wrapper">
        <ChatArea
          chatHistory={chatHistory}
          onSendMessage={handleSendMessage}
          loading={loading}
        />
      </div>
      <div className="preview-area-wrapper">
        <TemplatePreviewArea template={template} />
      </div>
    </div>
  );
}

export default TemplateGeneratorPage;
