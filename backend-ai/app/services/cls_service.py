import os
from dotenv import load_dotenv

from ..core.model_loader import model_loader
from ..schemas.validate import ValidateRequest, ValidateResponse, ValidateResult
from ..schemas.template import Template

def cls_template(request: ValidateRequest) -> ValidateResponse:
    # 템플릿 승인/반려 분류 함수
    load_dotenv()
    if os.getenv("IS_GPU_AVAILABLE") == "TRUE":
        validate_result = classify_template_with_finetuning(request.template)
    else:
        validate_result = dummy_validate_template(request.template)

    return ValidateResponse(
        result="approve" if validate_result.prediction == "Approved" else "reject",
        probability=str(round(max(validate_result.probabilities.values()) * 100, 2)) + "%"
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

def dummy_validate_template(template: Template) -> ValidateResult:
    return ValidateResult(
        prediction="Not Approved",
        confidence=1.0,
        probabilities={
            "Not Approved": 1.0,
            "Approved": 0.0,
        }
    )