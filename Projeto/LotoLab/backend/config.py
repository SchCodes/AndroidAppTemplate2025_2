import os
from dotenv import load_dotenv

# Carregar variáveis de ambiente
load_dotenv()

class Settings:
    # Configurações da API
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "LotoLab API"
    
    # Configurações do banco de dados
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./lotolab.db")
    
    # Configurações de segurança
    SECRET_KEY: str = os.getenv("SECRET_KEY", "sua_chave_secreta_aqui_mude_em_producao")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # Configurações do Selenium
    CHROME_DRIVER_PATH: str = os.getenv("CHROME_DRIVER_PATH", "")
    SELENIUM_HEADLESS: bool = True
    
    # Configurações de análise
    DEFAULT_NUM_CONCURSOS: int = 100
    MAX_NUM_CONCURSOS: int = 1000
    MAX_COMBINACOES: int = 20
    
    # Configurações de cache
    CACHE_TTL: int = 3600  # 1 hora
    
    # Configurações de logging
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")
    
    # Configurações CORS
    BACKEND_CORS_ORIGINS: list = [
        "http://localhost:3000",
        "http://localhost:8080",
        "http://localhost:8000",
        "*"  # Em desenvolvimento, permitir todas as origens
    ]

settings = Settings()
