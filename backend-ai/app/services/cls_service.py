import os
from dotenv import load_dotenv

from ..core.model_loader import model_loader
from ..schemas.validate import ValidateRequest, ValidateResponse, ValidateResult
from ..schemas.template import Template

async def cls_template(request: ValidateRequest) -> ValidateResponse:
    # 템플릿 승인/반려 분류 함수
    load_dotenv()
    if os.getenv("IS_GPU_AVAILABLE") == "TRUE":
        validate_result = classify_template_with_finetuning(request.template)
    else:
        validate_result = await classify_template_with_llm(request.template)

        # probabilities 딕셔너리가 비어있는지 확인합니다.
        if validate_result.probabilities:
            # 딕셔너리에 값이 있으면, 기존 로직대로 최댓값을 사용합니다.
            max_prob = max(validate_result.probabilities.values())
        else:
            # 딕셔너리가 비어있으면, 대신 confidence 값을 사용합니다.
            max_prob = validate_result.confidence

        probability_str = str(round(max_prob * 100, 2)) + "%"

        return ValidateResponse(
            result="approve" if validate_result.prediction == "Approved" else "reject",
            probability=probability_str
        )

def classify_template_with_finetuning(template: Template) -> ValidateResult:
    import torch
    from transformers import AutoTokenizer, AutoModelForSequenceClassification

    cls_model, cls_tokenizer = model_loader.models.get("cls")
    inputs = cls_tokenizer(
        text=template.model_dump_json(),
        padding=True,
        truncation=True,
        max_length=512,
        return_tensors="pt"
    )

    with torch.no_grad():
        outputs = cls_model(**inputs)
        predictions = torch.nn.functional.softmax(outputs.logits, dim=-1)
        predicted_class = torch.argmax(predictions, dim=-1).item()
        confidence = predictions[0][predicted_class].item()

    return ValidateResult(
        prediction="Approved" if predicted_class == 1 else "Not Approved",
        confidence=confidence,
        probabilities={
            "Not Approved": predictions[0][0].item(),
            "Approved": predictions[0][1].item()
        }
    )

async def classify_template_with_llm(template: Template) -> ValidateResult:
    from ..core.agent_initializer import AgentInitializer
    from ..services.guidelines_service import retrieve

    template_json = template.model_dump_json()
    related_policies = await retrieve({
        "question":template_json
    })
    docs_content = [doc.page_content for doc, score in related_policies["context"]]
    related_policies_str = "\n".join(docs_content)
    llm = AgentInitializer.llm

    prompt =  f"""
    당신은 카카오 알림톡 템플릿의 승인 여부를 결정하는 숙련된 심사 전문가입니다.
    당신의 임무는 주어진 '알림톡 템플릿'이 함께 제공된 '심사 정책'을 준수하는지 꼼꼼하게 검토하고, 최종적으로 승인 여부를 판단하는 것입니다.
    
    ### 심사할 알림톡 템플릿 (JSON 형식)
    
    ```json
    {template_json}
    ```
    
    적용할 심사 정책
    
    {related_policies_str}
    
    작업 지시
    
    먼저, '심사할 알림톡 템플릿'의 모든 내용(템플릿 이름, 내용, 버튼 등)을 주의 깊게 분석하십시오.
    다음으로, '적용할 심사 정책'의 각 조항을 하나씩 확인하며 템플릿 내용과 비교하십시오.
    정책 위반 사항이 있는지, 오해의 소지가 있는 문구는 없는지, 광고성/홍보성 내용이 포함되어 있는지 등을 종합적으로 고려해야 합니다.
    분석을 바탕으로 최종 'Approved' 또는 'Not Approved' 결정을 내리고, 그에 대한 신뢰도 점수와 각 확률을 계산하십시오.
    최종 판단 결과를 ValidateResult 형식에 맞춰 반드시 제공해야 합니다. prediction, confidence, probabilities 필드를 포함해야 합니다.
    별도의 설명 없이, ValidateResult 객체에 해당하는 JSON만 출력해주십시오.
    """

    return llm.invoke(prompt)