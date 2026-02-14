from pgvector.sqlalchemy import Vector
from sqlalchemy import BigInteger, String, DateTime, Boolean, Column, Date, Numeric, Integer
from sqlalchemy.dialects.postgresql import JSONB

from database import Base


class NewsRaw(Base):
    __tablename__ = "news_raw"

    id = Column(BigInteger, primary_key=True)
    title = Column(String)
    published_at = Column(DateTime)
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


class StockAlias(Base):
    __tablename__ = "stock_alias"

    alias = Column(String, primary_key=True)
    stock_code = Column(String, nullable=False)
    stock_name = Column(String, nullable=False)
    created_at = Column(DateTime, nullable=False)


class SubsidiaryMapping(Base):
    __tablename__ = "subsidiary_mapping"

    subsidiary_name = Column(String, primary_key=True)
    stock_code = Column(String, nullable=False)
    stock_name = Column(String, nullable=False)
    created_at = Column(DateTime, nullable=False)


class StockPriceDailyRaw(Base):
    __tablename__ = "stock_price_daily_raw"

    trd_dd = Column(Date, primary_key=True)
    stock_code = Column(String, primary_key=True)
    stock_name = Column(String, nullable=False)
    close = Column(BigInteger)
    diff_rate = Column(Numeric(10, 2))


class StockClusterResult(Base):
    __tablename__ = "stock_cluster_result"

    stock_code = Column(String, primary_key=True)
    clustered_at = Column(DateTime, primary_key=True)
    stock_name = Column(String, nullable=False)
    total_count = Column(Integer, nullable=False)
    input_hash = Column(String, nullable=False)
    result = Column(JSONB, nullable=False)


class NewsExtraction(Base):
    __tablename__ = "news_extraction"

    extraction_id = Column(BigInteger, primary_key=True)
    news_id = Column(BigInteger, nullable=False)
    keywords = Column(JSONB, nullable=False, server_default='[]')
    llm_response = Column(String)
    llm_model = Column(String(100), nullable=False)
    prompt_version = Column(String(50), nullable=False)
    published_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, nullable=False)
