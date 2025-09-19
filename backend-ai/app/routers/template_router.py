from fastapi import APIRouter
from ..services.gen_service import gen_template
from ..schemas.template import TemplateRequest, TemplateResponse

template_router = APIRouter(prefix="/template", tags=["AI generates template"])

@template_router.post("/template")
async def generate_template(template_request: TemplateRequest)-> TemplateResponse:
    # 요청을 통해 템플릿 생성 service 함수를 호출하고 결과를 응답
    template_response = await gen_template(template_request)
    return template_response