import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ChatArea from '../components/TemplateGenerator/ChatArea';
import TemplatePreviewArea from '../components/TemplateGenerator/TemplatePreviewArea';
import '../styles/TemplateGenerator.css';
import apiAi from '../services/apiAi';
import { getIndividualTemplate } from '../services/api';

function TemplateGeneratorPage() {
  const { workspaceId, templateId } = useParams();
  const [template, setTemplate] = useState(null);
  const [chatHistory, setChatHistory] = useState([
    { type: 'bot', text: '안녕하세요. 템플릿 수정을 도와드릴게요. 어떤 변경을 원하시나요?' },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        const response = await getIndividualTemplate(workspaceId, templateId);
        setTemplate(response.data);
      } catch (err) {
        setError('템플릿을 불러오는 데 실패했습니다.');
        console.error('Failed to fetch template:', err);
      }
    };

    if (workspaceId && templateId) {
      fetchTemplate();
    }
  }, [workspaceId, templateId]);

  const handleSendMessage = async (userInput) => {
    if (!userInput.trim() || !template) return;

    const newUserMessage = { type: 'user', text: userInput };
    setChatHistory(prev => [...prev, newUserMessage]);
    setLoading(true);
    setError(null);

    const requestData = {
      original_template: template,
      user_input: userInput,
    };

    try {
      const response = await apiAi.post('/template', requestData);
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

  if (!template) {
    return <div>{error || '템플릿을 불러오는 중입니다...'}</div>;
  }

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
