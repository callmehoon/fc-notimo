// src/pages/TemplateGeneratorPage.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import ChatArea from '../components/TemplateGenerator/ChatArea';
import TemplatePreviewArea from '../components/TemplateGenerator/TemplatePreviewArea';
import '../styles/TemplateGenerator.css';
import apiAi from '../services/apiAi';
import {getMyTemplate, createMyTemplate, updateMyTemplate} from '../services/individualTemplateService';

function TemplateGeneratorPage() {
  const { workspaceId, templateId } = useParams();
  const [template, setTemplate] = useState(null);
  const templateIdRef = useRef(templateId !== 'new' ? parseInt(templateId) : null);
  const hasInitialized = useRef(false);
  const validationAbortControllerRef = useRef(null); // 검증 요청 취소를 위한 AbortController
  const [chatHistory, setChatHistory] = useState([
    { type: 'bot', text: '안녕하세요. 템플릿 수정을 도와드릴게요. 어떤 변경을 원하시나요?' },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [validationResult, setValidationResult] = useState(null);
  const [validationLoading, setValidationLoading] = useState(false);

  useEffect(() => {
    const initializeTemplate = async () => {
      if (hasInitialized.current) return; // 이미 초기화되었으면 중단
      hasInitialized.current = true;
      
      try {
        if (templateId === 'new') {
          // 새 템플릿인 경우 빈 템플릿을 즉시 생성
          const emptyPayload = {
            individualTemplateTitle: '',
            individualTemplateContent: '',
            buttonTitle: null,
            status: 'DRAFT',
          };
          
          const createResponse = await createMyTemplate(workspaceId, emptyPayload);
          const newId = createResponse.data.individualTemplateId;
          templateIdRef.current = newId;
          
          // 빈 템플릿을 미리보기에 설정
          setTemplate({
            title: '',
            text: '',
            button_name: null
          });
        } else {
          // 기존 템플릿 불러오기
          console.log('기존 템플릿 불러오기 시작:', { workspaceId, templateId });
          if (!workspaceId || !templateId) return;
          const res = await getMyTemplate(workspaceId, templateId);
          console.log('기존 템플릿 불러오기 응답:', res.data);
          setTemplate(res.data);
          templateIdRef.current = parseInt(templateId);
          console.log('templateIdRef 설정 완료:', templateIdRef.current);
        }
      } catch (err) {
        hasInitialized.current = false; // 실패하면 다시 시도할 수 있도록
        setError('템플릿을 불러오는 데 실패했습니다.');
        console.error('템플릿 초기화 실패:', err);
      }
    };
    
    initializeTemplate();
  }, [workspaceId, templateId]);

  // 컴포넌트 언마운트 시 진행 중인 검증 요청 정리
  useEffect(() => {
    return () => {
      if (validationAbortControllerRef.current) {
        validationAbortControllerRef.current.abort();
        console.log('컴포넌트 언마운트로 인한 검증 요청 취소');
      }
    };
  }, []);

  // 템플릿 검증 함수
  const validateTemplate = async (templateToValidate) => {
    // 이전 검증 요청이 있으면 취소
    if (validationAbortControllerRef.current) {
      validationAbortControllerRef.current.abort();
      console.log('이전 검증 요청 취소됨');
    }

    // 새로운 AbortController 생성
    const abortController = new AbortController();
    validationAbortControllerRef.current = abortController;

    setValidationLoading(true);
    try {
      const validateRequest = {
        template: {
          title: templateToValidate.title || '',
          text: templateToValidate.text || templateToValidate.content || '',
          button_name: templateToValidate.button_name || templateToValidate.buttonTitle || null
        }
      };

      const response = await apiAi.post('/validate', validateRequest, {
        signal: abortController.signal // AbortController signal 추가
      });

      // 요청이 취소되지 않았을 때만 결과 설정
      if (!abortController.signal.aborted) {
        setValidationResult(response.data);
        validationAbortControllerRef.current = null; // 완료된 요청 정리
        return response.data;
      }
    } catch (error) {
      if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
        console.log('검증 요청이 취소되었습니다.');
        return null; // 취소된 경우 null 반환
      }
      console.error('템플릿 검증 실패:', error);
      if (!abortController.signal.aborted) {
        setValidationResult({ result: 'error', probability: '검증 중 오류가 발생했습니다.' });
      }
    } finally {
      if (!abortController.signal.aborted) {
        setValidationLoading(false);
      }
    }
  };

  const handleSendMessage = async (userInput) => {
    if (!userInput.trim()) return;

    // 새로운 사용자 요청이 들어오면 진행 중인 FastAPI 검증 즉시 취소
    if (validationAbortControllerRef.current) {
      validationAbortControllerRef.current.abort();
      console.log('새로운 사용자 요청으로 인한 검증 취소');
      setValidationLoading(false); // 검증 로딩 상태도 초기화
    }

    setChatHistory(prev => [...prev, { type: 'user', text: userInput }]);
    setLoading(true);
    setError(null);

    // AI 서버에서 요구하는 형식에 맞게 데이터 구성
    const originalTemplate = template ? {
      title: template.individualTemplateTitle || template.title || "",
      text: template.individualTemplateContent || template.text || "",
      button_name: template.buttonTitle || template.button_name || null
    } : {
      title: "",
      text: "",
      button_name: null
    };
    
    const requestData = { 
      original_template: originalTemplate, 
      user_input: userInput 
    };

    console.log('AI API 요청 데이터:', requestData); // 디버깅용

    try {
      // 1) AI 호출
      const response = await apiAi.post('/template', requestData);
      const { template: newTemplate, chat_response: chatResponse } = response.data;

      // 2) 프리뷰 반영
      setTemplate(newTemplate);
      setChatHistory(prev => [...prev, { type: 'bot', text: chatResponse }]);

      // 3) Spring 백엔드에 업데이트 먼저 수행
      console.log('업데이트 시작:', { workspaceId, templateId: templateIdRef.current });
      if (templateIdRef.current) {
        const payload = {
          individualTemplateTitle: newTemplate.title || '',
          individualTemplateContent: newTemplate.text || newTemplate.content || '',
          buttonTitle: newTemplate.button_name || newTemplate.buttonTitle || null,
          status: newTemplate.status ?? 'DRAFT',
        };

        console.log('업데이트 페이로드:', payload);
        await updateMyTemplate(workspaceId, templateIdRef.current, payload);
        console.log('Spring 백엔드 업데이트 완료');
      } else {
        console.error('templateIdRef.current가 없음:', templateIdRef.current);
        throw new Error('템플릿 ID가 없습니다. 페이지를 새로고침해주세요.');
      }

      // 4) FastAPI 검증을 비동기로 처리 (채팅 진행을 방해하지 않음)
      validateTemplate(newTemplate).catch(error => {
        console.error('백그라운드 검증 실패:', error);
        // 검증 실패해도 사용자 경험에는 영향 없음
      });

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
