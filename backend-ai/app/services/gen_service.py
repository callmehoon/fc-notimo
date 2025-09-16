from ..schemas.template import TemplateRequest, TemplateResponse
from ..core.model_loader import model_loader

def gen_template(request: TemplateRequest) -> TemplateResponse:
    models = model_loader.models
    gen_model, gen_tokenizer = models.get("gen")
    if gen_model == "dummy_gen_model" or gen_tokenizer == "dummy_gen_tokenizer":
        return generate_dummy_template(request)
    else:
        return generate_template(request)

def generate_dummy_template(request: TemplateRequest) -> TemplateResponse:
    return TemplateResponse(
        template="{\"title\": \"회사소개서 발송\", \"text\": \"안녕하세요 #{수신자명}님,\n\n#{회사명}은 #{업종} 분야에서 활동하는 #{회사명}입니다.\n\n▶ 회사명 : #{회사명}\n▶ 업종 : #{업종}\n▶ 연락처 : #{연락처}\n\n감사합니다.\", \"button_name\": \"자세히 보기\"}",
        chat_response="개발용 로컬 서버의 데모입니다. GPU 가 탑제된 추론서버로 배포하면 정상적으로 기능이 동작할 것입니다."
    )

def generate_template(request: TemplateRequest) -> TemplateResponse:
    import torch

    models = model_loader.models
    gen_model, gen_tokenizer = models.get("gen")

    prompt = f"user_input: {request.user_input}\npolicy: {request.related_policy}\ntemplate: "

    inputs = gen_tokenizer(prompt, return_tensors="pt", max_length=512, truncation=True).to(gen_model.device)

    with torch.no_grad():
        outputs = gen_model.generate(
            **inputs,
            max_new_tokens=150,
            temperature=0.3,
            do_sample=True,
            pad_token_id=gen_tokenizer.eos_token_id,
            early_stopping=True
        )

    response = gen_tokenizer.decode(outputs[0], skip_special_tokens=True)
    template_txt = response.split("template: ")[-1].strip()

    # TODO LangChain Agent 사용해서 template parsing & chat_response 생성

    return TemplateResponse(
        template = template_txt,
        chat_response = "None"
    )
