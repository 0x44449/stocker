import logging

from langchain_ollama import ChatOllama

from config import OLLAMA_BASE_URL

logger = logging.getLogger(__name__)

PROMPT_TEMPLATE = """텍스트에서 회사/기업 이름만 추출해. 다른 설명 없이 이름만 쉼표로 나열해.

예시1)
텍스트: 삼성전자가 반도체 투자를 발표했고 SK하이닉스도 동참했다.
회사 이름: 삼성전자, SK하이닉스

예시2)
텍스트: 넷플릭스에서 새 드라마가 공개됐고 교보문고 베스트셀러에 올랐다.
회사 이름: 넷플릭스, 교보문고

본문)
텍스트: {text}
회사 이름:"""


def extract_companies(text: str) -> list[str]:
    """뉴스 텍스트에서 회사/기업 이름을 추출한다."""
    llm = ChatOllama(model="qwen3:8b", base_url=OLLAMA_BASE_URL, reasoning=False)
    response = llm.invoke(PROMPT_TEMPLATE.format(text=text))
    raw = response.content.strip()
    logger.info(f"LLM raw response: {raw}")
    if not raw:
        return []
    return [name.strip() for name in raw.split(",") if name.strip()]
