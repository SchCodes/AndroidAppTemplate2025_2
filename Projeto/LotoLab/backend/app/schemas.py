from pydantic import BaseModel, Field, validator
from typing import List, Optional, Dict, Any
from datetime import datetime

class UsuarioBase(BaseModel):
    nome: str = Field(..., min_length=2, max_length=100)
    email: str = Field(..., regex=r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$")
    premium: bool = False
    assinatura_ativa: bool = False
    limite_dia: int = Field(default=3, ge=1, le=10)

class UsuarioCreate(UsuarioBase):
    senha: str = Field(..., min_length=6)

class UsuarioResponse(UsuarioBase):
    id: int
    
    class Config:
        from_attributes = True

class ConcursoBase(BaseModel):
    concurso_id: int
    data: datetime
    dezenas: List[int] = Field(..., min_items=15, max_items=15)
    premio_15: Optional[float] = None
    premio_14: Optional[float] = None
    premio_13: Optional[float] = None
    premio_12: Optional[float] = None
    premio_11: Optional[float] = None

    @validator('dezenas')
    def validar_dezenas(cls, v):
        if not all(1 <= num <= 25 for num in v):
            raise ValueError('Todas as dezenas devem estar entre 1 e 25')
        if len(set(v)) != 15:
            raise ValueError('As dezenas devem ser únicas')
        return sorted(v)

class ConcursoCreate(ConcursoBase):
    pass

class ConcursoResponse(ConcursoBase):
    class Config:
        from_attributes = True

class HistoricoBase(BaseModel):
    usuario_id: int
    tipo_calculo: str
    resultado: str
    data_execucao: datetime

class HistoricoCreate(HistoricoBase):
    pass

class HistoricoResponse(HistoricoBase):
    id: int
    
    class Config:
        from_attributes = True

class CombinacaoRequest(BaseModel):
    numeros: List[int] = Field(..., min_items=15, max_items=15)
    
    @validator('numeros')
    def validar_numeros(cls, v):
        if not all(1 <= num <= 25 for num in v):
            raise ValueError('Todos os números devem estar entre 1 e 25')
        if len(set(v)) != 15:
            raise ValueError('Os números devem ser únicos')
        return sorted(v)

class CombinacaoResponse(BaseModel):
    combinacao: List[int]
    probabilidade_individual: float
    probabilidade_combinada: float
    ranking_frequencia: Dict[int, Dict[str, Any]]

class AnaliseRequest(BaseModel):
    num_concursos: int = Field(default=100, ge=10, le=1000)
    tipo_analise: str = Field(default="completa", regex="^(completa|frequencias|estatisticas|combinacoes)$")

class AnaliseResponse(BaseModel):
    data_geracao: str
    estatisticas: Dict[str, Any]
    frequencias: Dict[int, int]
    combinacoes_recomendadas: List[List[int]]
    resumo: Dict[str, Any]

class LoginRequest(BaseModel):
    email: str
    senha: str

class LoginResponse(BaseModel):
    access_token: str
    token_type: str
    usuario: UsuarioResponse

class TokenData(BaseModel):
    email: Optional[str] = None

class NotificacaoBase(BaseModel):
    titulo: str
    mensagem: str
    tipo: str = Field(default="info", regex="^(info|warning|success|error)$")
    lida: bool = False

class NotificacaoCreate(NotificacaoBase):
    usuario_id: int

class NotificacaoResponse(NotificacaoBase):
    id: int
    data_criacao: datetime
    
    class Config:
        from_attributes = True

class RelatorioRequest(BaseModel):
    periodo_inicio: Optional[datetime] = None
    periodo_fim: Optional[datetime] = None
    incluir_graficos: bool = True
    formato: str = Field(default="json", regex="^(json|pdf|csv)$")

class RelatorioResponse(BaseModel):
    data_geracao: str
    periodo_analisado: Dict[str, str]
    estatisticas_gerais: Dict[str, Any]
    top_numeros: Dict[str, List[Dict[str, Any]]]
    padroes_temporais: Dict[str, Any]
    combinacoes_otimizadas: List[List[int]]
    graficos_urls: Optional[List[str]] = None
