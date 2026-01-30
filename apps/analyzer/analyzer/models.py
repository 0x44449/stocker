from pgvector.sqlalchemy import Vector
from sqlalchemy import BigInteger, String, DateTime, Column

from analyzer.database import Base


class NewsRaw(Base):
    __tablename__ = "news_raw"

    id = Column(BigInteger, primary_key=True)
    title = Column(String)
    raw_text = Column(String)


class NewsCompanyExtraction(Base):
    __tablename__ = "news_company_extraction"

    id = Column(BigInteger, primary_key=True)
    news_id = Column(BigInteger)
    status = Column(String)
    created_at = Column(DateTime)
    processed_at = Column(DateTime)


class NewsCompanyExtractionResult(Base):
    __tablename__ = "news_company_extraction_result"

    id = Column(BigInteger, primary_key=True)
    extraction_id = Column(BigInteger)
    company_name = Column(String)


class NewsEmbedding(Base):
    __tablename__ = "news_embedding"

    id = Column(BigInteger, primary_key=True)
    news_id = Column(BigInteger)
    embedding = Column(Vector(1024))
    created_at = Column(DateTime)
