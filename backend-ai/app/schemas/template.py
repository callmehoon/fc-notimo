from pydantic import BaseModel
from typing import List, Optional

class Template(BaseModel):
    """템플릿 객체"""
    title: str
    text: str
    button_name: Optional[str] = None

class AgentOutput(BaseModel):
    """에이전트의 최종 출력 객체"""
    template: Template
    chat_message: str

class TemplateRequest(BaseModel):
    """템플릿 생성 요청 객체 정의"""
    original_template: Template
    user_input: str

class TemplateResponse(BaseModel):
    """템플릿 생성 응답 객체 정의"""
    template: Template
    chat_response: str