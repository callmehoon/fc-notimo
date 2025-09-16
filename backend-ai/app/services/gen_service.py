from ..schemas.template import TemplateRequest, TemplateResponse
from ..core.model_loader import model_loader
from typing import List

def gen_template(request: TemplateRequest) -> TemplateResponse:
    models = model_loader.models
    gen_model, gen_tokenizer = models.get("gen")
    if gen_model == "dummy_gen_model" or gen_tokenizer == "dummy_gen_tokenizer":
        template = generate_dummy_template(request.user_input, request.related_policy)
    else:
        template = generate_template(request.user_input, request.related_policy)

    # TODO LangChain 적용
    return TemplateResponse(
        template=template, # TODO LangChain 의 Agent 에게 parsing tool 을 제공
        chat_response="None" # TODO LangChain 의 Agent 로 chat_response 생성
    )

def generate_dummy_template(user_input: str, related_policy: List[str]) -> str:
    return '{"title": "가짜 템플릿", "text": "GPU 가 없는 개발용 로컬 환경에서 반환되는 결과입니다.\n변수들을 테스트 해보세요.\n#{변수1}, #{변수2}\n감사합니다.", "button_name": "버튼 이름"}'

def generate_template(user_input: str, related_policy: List[str]) -> str:
    import torch

    models = model_loader.models
    gen_model, gen_tokenizer = models.get("gen")

    prompt = f"user_input: {user_input}\npolicy: {related_policy}\ntemplate: "

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
    return response.split("template: ")[-1].strip()
