from typing import Literal, Dict
from pydantic import BaseModel, Field
from .template import Template

class ValidateRequest(BaseModel):
    # 템플릿 검증 요청 객체 정의
    template: Template

class ValidateResponse(BaseModel):
    #템플릿 검증 응답 객체 정의
    result: Literal["approve", "reject"]
    probability: str

class ValidateResult(BaseModel):
    """
    제공된 정책에 따라 카카오 알림톡 템플릿의 승인 여부 검증 결과를 나타냅니다.
    """
    prediction: Literal["Approved", "Not Approved"] = Field(
        description="템플릿의 최종 승인 또는 비승인 예측 결과입니다."
    )
    confidence: float = Field(
        description="예측에 대한 모델의 신뢰도 점수이며, 0.0과 1.0 사이의 값입니다."
    )
    probabilities: Dict[Literal["Approved", "Not Approved"], float] = Field(
        description="각 분류('Approved', 'Not Approved')에 대한 예측 확률 값입니다. 두 확률의 합은 반드시 1.0이어야 합니다."
    )