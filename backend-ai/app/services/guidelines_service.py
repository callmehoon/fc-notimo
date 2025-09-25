# 입력으로 사용자 요청이 들어오면 출력으로 검색된 정책들을 출력
import re

from langchain.chat_models import init_chat_model
from langchain_core.documents import Document

from ..config.config import COLLECTION_NAME, s3_text, vector_store

# --- LLM/Vector 설정 (필요시 LLM은 사용 안 함) ---
_llm = init_chat_model("gpt-4o-mini", model_provider="openai")

# --- 인덱싱 유틸 ---
async def _collection_empty() -> bool:
    try:
        col = vector_store._collection
        return (col.count() or 0) == 0
    except Exception:
        return True

async def _load_paragraph_docs_from_s3():
    text = s3_text() # S3에서 원문 읽기
    text = text.replace("\r\n", "\n").replace("\r", "\n")

    paragraphs = [p.strip() for p in re.split(r"\n\s*\n", text) if p.strip()]

    docs = [Document(page_content=p, metadata={"para_idx": i}) for i, p in enumerate(paragraphs)]
    return docs

async def _ensure_index():
    if await _collection_empty():
        print("컬렉션이 비어있어 생성 및 인덱스를 진행합니다.")
        docs = await _load_paragraph_docs_from_s3()
        await vector_store.aadd_documents(docs)
        # Chroma(persistent)라 별도 persist() 호출 없이 자동 반영됨
    else:
        print("컬렉션이 이미 존재하여 인덱싱을 건너뜁니다.")
        print("count =", vector_store._collection.count())

async def reset_index_soft() -> int:
    """
    컬렉션은 유지하고, 내부 문서만 전부 삭제한 뒤 새로 인덱싱.
    반환: 업로드한 문서 개수
    """
    # 1) 전체 삭제
    try:
        vector_store._collection.delete(ids=vector_store.get()['ids'])
        print("[OK] cleared all docs in collection (soft reset)")
    except Exception as e:
        print(f"[WARN] soft reset delete failed: {e}")

    # 2) 재업로드
    docs = await _load_paragraph_docs_from_s3()
    await vector_store.aadd_documents(docs)
    print(f"[OK] re-indexed {len(docs)} docs")
    return len(docs)

# --- 여기부터 'state 파이프라인' 전용 함수 두 개만 사용 ---
async def retrieve(state: dict) -> dict:
    """
    입력: state = {"question": str, "k": int(옵션), "keyword": str(옵션)}
    출력: {"context": List[Document]}
    """
    await _ensure_index()

    k = state.get("k", 10)
    # keyword = state.get("keyword")

    results = await vector_store.asimilarity_search_with_score(state["question"], k=k)
    # if keyword:
    #     # results = [d for d in results if keyword in d.page_content]
    #     results = [(doc, score) for (doc, score) in results if keyword in doc.page_content]
    return {"context": results}

async def generate(state: dict, max_chars: int = 1000) -> dict:
    """
    입력: state에 "context"가 있어야 함
    출력: {"answer": str}
    """
    chunks = state.get("context", [])
    lines = [f"[DEBUG] retrieved {len(chunks)} chunks"]

    for i, (doc, score) in enumerate(chunks, 1):
        lines.append(f"--- Chunk #{i} --- (score={score:.4f})\n{doc.page_content[:max_chars]}\n")
    return {"answer": "\n".join(lines)}
