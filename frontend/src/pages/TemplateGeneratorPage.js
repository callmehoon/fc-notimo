// src/pages/TemplateGeneratorPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ChatArea from '../components/TemplateGenerator/ChatArea';
import TemplatePreviewArea from '../components/TemplateGenerator/TemplatePreviewArea';
import '../styles/TemplateGenerator.css';
import apiAi from '../services/apiAi';
import { getMyTemplate, createMyTemplate } from '../services/individualTemplateService';

function TemplateGeneratorPage() {
  const { workspaceId, templateId } = useParams();
  const [template, setTemplate] = useState(null);
  const [chatHistory, setChatHistory] = useState([
    { type: 'bot', text: '안녕하세요. 템플릿 수정을 도와드릴게요. 어떤 변경을 원하시나요?' },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [validationResult, setValidationResult] = useState(null);
  const [validationLoading, setValidationLoading] = useState(false);

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        // 새 템플릿 생성인 경우 기존 템플릿을 가져오지 않음
        if (!workspaceId || !templateId || templateId === 'new') return;
        const res = await getMyTemplate(workspaceId, templateId);
        setTemplate(res.data);
      } catch (err) {
        setError('템플릿을 불러오는 데 실패했습니다.');
        console.error('Failed to fetch template:', err);
      }
    };
    fetchTemplate();
  }, [workspaceId, templateId]);

  // 템플릿 검증 함수
  const validateTemplate = async (templateToValidate) => {
    setValidationLoading(true);
    try {
      const validateRequest = {
        template: {
          title: templateToValidate.title || '',
          text: templateToValidate.text || templateToValidate.content || '',
          button_name: templateToValidate.button_name || templateToValidate.buttonTitle || null
        }
      };
      
      const response = await apiAi.post('/validate', validateRequest);
      setValidationResult(response.data);
      return response.data;
    } catch (error) {
      console.error('템플릿 검증 실패:', error);
      setValidationResult({ result: 'error', probability: '검증 중 오류가 발생했습니다.' });
    } finally {
      setValidationLoading(false);
    }
  };

  const handleSendMessage = async (userInput) => {
    if (!userInput.trim()) return;

    setChatHistory(prev => [...prev, { type: 'user', text: userInput }]);
    setLoading(true);
    setError(null);

    // AI 서버에서 요구하는 형식에 맞게 데이터 구성
    const originalTemplate = template || {
      title: "",
      text: "",
      button_name: null
    };
    
    const requestData = { 
      original_template: originalTemplate, 
      user_input: userInput 
    };

    try {
      // 1) AI 호출
      const response = await apiAi.post('/template', requestData);
      const { template: newTemplate, chat_response: chatResponse } = response.data;

      // 2) 프리뷰 반영
      setTemplate(newTemplate);
      setChatHistory(prev => [...prev, { type: 'bot', text: chatResponse }]);

      // 3) 템플릿 검증
      await validateTemplate(newTemplate);

      // 4) 백엔드에 저장(create)
      const payload = {
        individualTemplateTitle: newTemplate.title ?? newTemplate.individualTemplateTitle ?? '제목 없음',
        individualTemplateContent: newTemplate.text ?? newTemplate.content ?? newTemplate.individualTemplateContent ?? '',
        buttonTitle: newTemplate.button_name ?? newTemplate.buttonTitle ?? null,
        status: newTemplate.status ?? 'DRAFT',
      };
      
      await createMyTemplate(workspaceId, payload);

    } catch (err) {
      const errorMessage = 'API 호출에 실패했습니다. 다시 시도해주세요.';
      setError(errorMessage);
      setChatHistory(prev => [...prev, { type: 'bot', text: errorMessage }]);
      console.error('API call failed:', err);
    } finally {
      setLoading(false);
    }
  };

  // 기존 템플릿을 불러오는 경우에만 로딩 화면 표시 (새 템플릿 생성인 경우는 제외)
  if (!template && templateId !== 'new') {
    return <div>{error || '템플릿을 불러오는 중입니다...'}</div>;
  }

  return (
      <div className="template-generator-container">
        <div className="chat-area-wrapper">
          <ChatArea chatHistory={chatHistory} onSendMessage={handleSendMessage} loading={loading} />
        </div>
        <div className="preview-area-wrapper">
          <TemplatePreviewArea 
            template={template} 
            validationResult={validationResult}
            validationLoading={validationLoading}
          />
        </div>
      </div>
  );
}

export default TemplateGeneratorPage;
