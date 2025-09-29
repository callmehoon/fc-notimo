import asyncio

from services.guidelines_service import retrieve, generate, reset_index_soft

async def test():
    state = {"question": "회원가입 완료 템플릿에 광고 문구 하나를 넣고 싶어", "context": [], "answer": ""}
    state |= await retrieve(state)
    state |= await generate(state)
    print("question : " + state["question"])
    print(state["answer"])


if __name__ == "__main__":
    asyncio.run(test())
    # asyncio.run(reset_index_soft())