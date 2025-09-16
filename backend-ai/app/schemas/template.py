from pydantic import BaseModel
from typing import List, Optional

class TemplateRequest(BaseModel):
    """템플릿 생성 요청 객체 정의"""
    user_input: str
    related_policy: Optional[List[str]] = None

class TemplateResponse(BaseModel):
    """템플릿 생성 응답 객체 정의"""
    template: str
    chat_response: str