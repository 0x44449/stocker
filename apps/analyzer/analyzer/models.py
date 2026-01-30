from sqlalchemy import BigInteger, String, Column

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
