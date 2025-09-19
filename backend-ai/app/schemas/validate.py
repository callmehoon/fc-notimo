from typing import Literal, Dict
from pydantic import BaseModel
from .template import Template

class ValidateRequest(BaseModel):
    # 템플릿 검증 요청 객체 정의
    template: Template

class ValidateResponse(BaseModel):
    #템플릿 검증 응답 객체 정의
    result: Literal["approve", "reject"]
    probability: str

class ValidateResult(BaseModel):
    prediction: Literal["Approved", "Not Approved"]
    confidence: float
    probabilities: Dict[Literal["Approved", "Not Approved"], float]