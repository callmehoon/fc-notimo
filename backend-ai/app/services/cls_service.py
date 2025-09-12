from ..core.model_loader import model_loader
from ..schemas.validate import ValidateRequest, ValidateResponse

def cls_template(request: ValidateRequest) -> ValidateResponse:
    # TODO 템플릿 승인/반려 분류 함수 구현
    pass
    cls_model, cls_tokenizer = model_loader.models.get("cls_model")
    return ValidateResponse()