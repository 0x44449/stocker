import logging
import time

from langchain_ollama import OllamaLLM

from config import OLLAMA_BASE_URL

logger = logging.getLogger(__name__)

PROMPT_TEMPLATE = """아래 텍스트에서 회사/기업 이름만 추출해서 쉼표로 구분해 나열해.
회사가 없으면 "없음"이라고 답해.
다른 설명 없이 결과만 출력해.

예시1)
입력: 삼성전자가 반도체 투자를 발표했고 SK하이닉스도 동참했다.
출력: 삼성전자, SK하이닉스

예시2)
입력: 넷플릭스에서 새 드라마가 공개됐고 교보문고 베스트셀러에 올랐다.
출력: 넷플릭스, 교보문고

예시3)
입력: 오늘 날씨가 좋다.
출력: 없음

입력: {text}
출력:"""


def extract_companies(text: str) -> tuple[list[str], str]:
    """뉴스 텍스트에서 회사/기업 이름을 추출한다. (keywords, llm_response) 튜플 반환."""
    text_length = len(text)
    logger.info(f"LLM 호출 시작 - 텍스트 길이: {text_length}")

    start_time = time.time()
    llm = OllamaLLM(model="qwen2.5:7b", base_url=OLLAMA_BASE_URL)
    prompt = PROMPT_TEMPLATE.format(text=text)
    raw = llm.invoke(prompt).strip()
    elapsed = time.time() - start_time
    logger.info(f"LLM 호출 완료 - 소요시간: {elapsed:.2f}초, 응답: {raw}")
    if not raw or raw == "없음":
        return [], raw
    keywords = [name.strip() for name in raw.split(",") if name.strip()]
    return keywords, raw
