from ..schemas.template import TemplateRequest, TemplateResponse
from ..core.model_loader import model_loader

def gen_template(request: TemplateRequest) -> TemplateResponse:
    # TODO 템플릿 생성 함수 구현
    pass
    gen_model, gen_tokenizer = model_loader.models.get("gen")
    return TemplateResponse()