from pgvector.sqlalchemy import Vector
from sqlalchemy import BigInteger, String, DateTime, Boolean, Column

from database import Base


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


class StockMaster(Base):
    __tablename__ = "stock_master"

    isin_code = Column(String, primary_key=True)
    stock_code = Column(String)
    name_kr = Column(String)
    name_kr_short = Column(String)


class CompanyNameMapping(Base):
    __tablename__ = "company_name_mapping"

    id = Column(BigInteger, primary_key=True)
    news_id = Column(BigInteger)
    extracted_name = Column(String)
    matched_stock_code = Column(String)
    match_type = Column(String)
    verified = Column(Boolean)
    created_at = Column(DateTime)
    updated_at = Column(DateTime)
