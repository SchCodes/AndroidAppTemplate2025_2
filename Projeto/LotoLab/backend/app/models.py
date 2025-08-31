from sqlalchemy import Column, Integer, String, Boolean, Date, JSON
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Usuario(Base):
    __tablename__ = "usuarios"
    id = Column(Integer, primary_key=True, index=True)
    nome = Column(String)
    email = Column(String, unique=True, index=True)
    premium = Column(Boolean, default=False)
    assinatura_ativa = Column(Boolean, default=False)
    limite_dia = Column(Integer, default=3)

class Concurso(Base):
    __tablename__ = "concursos"
    concurso_id = Column(Integer, primary_key=True, index=True)
    data = Column(Date)
    dezenas = Column(JSON)  # lista de 15 números

class Historico(Base):
    __tablename__ = "historico"
    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer)
    tipo_calculo = Column(String)
    resultado = Column(String)
    data_execucao = Column(String)
