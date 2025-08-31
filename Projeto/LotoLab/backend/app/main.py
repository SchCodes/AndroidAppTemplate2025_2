from fastapi import FastAPI, HTTPException, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.cors import CORSMiddleware
from app import crud, models, database, schemas
from app.lotofacil_analyzer import criar_analisador_lotofacil
import json
from datetime import datetime

app = FastAPI(title="LotoLab API", version="2.0.0")

# Configuração CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configuração de segurança
security = HTTPBearer()

# Inicializar analisador
analisador = criar_analisador_lotofacil()

@app.get("/usuarios/{usuario_id}")
def get_usuario(usuario_id: int):
    usuario = crud.get_usuario(usuario_id)
    return usuario

@app.get("/concursos/latest")
def get_latest_concurso():
    concurso = crud.get_latest_concurso()
    return concurso

@app.get("/concursos/all")
def get_all_concursos():
    concursos = crud.get_all_concursos()
    return concursos

@app.get("/historico/{usuario_id}")
def get_historico(usuario_id: int):
    historico = crud.get_historico(usuario_id)
    return historico

@app.get("/notificacoes/{usuario_id}")
def get_notificacoes(usuario_id: int):
    notificacoes = crud.get_notificacoes(usuario_id)
    return notificacoes

# ===== NOVAS ENDPOINTS PARA ANÁLISE ESTATÍSTICA =====

@app.post("/analise/estatisticas", response_model=schemas.AnaliseResponse)
async def analisar_estatisticas(request: schemas.AnaliseRequest):
    """Analisa estatísticas dos sorteios da Lotofácil"""
    try:
        if request.tipo_analise == "completa":
            resultado = analisador.gerar_relatorio_completo()
        elif request.tipo_analise == "frequencias":
            resultado = {
                'data_geracao': datetime.now().isoformat(),
                'estatisticas': {},
                'frequencias': analisador.calcular_frequencias(),
                'combinacoes_recomendadas': [],
                'resumo': {}
            }
        elif request.tipo_analise == "estatisticas":
            resultado = {
                'data_geracao': datetime.now().isoformat(),
                'estatisticas': analisador.calcular_estatisticas(),
                'frequencias': {},
                'combinacoes_recomendadas': [],
                'resumo': {}
            }
        elif request.tipo_analise == "combinacoes":
            resultado = {
                'data_geracao': datetime.now().isoformat(),
                'estatisticas': {},
                'frequencias': {},
                'combinacoes_recomendadas': analisador.gerar_combinacoes_inteligentes(),
                'resumo': {}
            }
        
        return resultado
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro na análise: {str(e)}")

@app.post("/analise/combinacao", response_model=schemas.CombinacaoResponse)
async def analisar_combinacao(request: schemas.CombinacaoRequest):
    """Analisa uma combinação específica de números"""
    try:
        resultado = analisador.calcular_probabilidade_combinacao(request.numeros)
        if "erro" in resultado:
            raise HTTPException(status_code=400, detail=resultado["erro"])
        return resultado
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro na análise da combinação: {str(e)}")

@app.get("/analise/frequencias")
async def obter_frequencias():
    """Retorna as frequências de todos os números"""
    try:
        return analisador.calcular_frequencias()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao obter frequências: {str(e)}")

@app.get("/analise/combinacoes-recomendadas")
async def obter_combinacoes_recomendadas(num_combinacoes: int = 5):
    """Gera combinações recomendadas baseadas na análise estatística"""
    try:
        if num_combinacoes < 1 or num_combinacoes > 20:
            raise HTTPException(status_code=400, detail="Número de combinações deve estar entre 1 e 20")
        
        combinacoes = analisador.gerar_combinacoes_inteligentes(num_combinacoes)
        return {
            "combinacoes": combinacoes,
            "total": len(combinacoes),
            "data_geracao": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao gerar combinações: {str(e)}")

@app.get("/analise/relatorio-completo")
async def obter_relatorio_completo():
    """Gera um relatório completo com todas as análises"""
    try:
        return analisador.gerar_relatorio_completo()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao gerar relatório: {str(e)}")

@app.post("/dados/atualizar")
async def atualizar_dados_lotofacil(num_concursos: int = 100):
    """Atualiza os dados dos sorteios da Lotofácil"""
    try:
        if num_concursos < 10 or num_concursos > 1000:
            raise HTTPException(status_code=400, detail="Número de concursos deve estar entre 10 e 1000")
        
        resultados = analisador.coletar_dados_lotofacil(num_concursos)
        return {
            "mensagem": f"Dados atualizados com sucesso",
            "total_concursos": len(resultados),
            "data_atualizacao": datetime.now().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao atualizar dados: {str(e)}")

@app.get("/health")
async def health_check():
    """Endpoint de verificação de saúde da API"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "version": "2.0.0",
        "servico": "LotoLab API"
    }
