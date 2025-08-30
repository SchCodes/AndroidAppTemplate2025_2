"""
Configuração do Chaquopy para o projeto Lotofácil
"""

# Configurações de importação
import sys
import os

# Adicionar o diretório Python ao path
python_dir = os.path.dirname(os.path.abspath(__file__))
if python_dir not in sys.path:
    sys.path.insert(0, python_dir)

# Configurações de logging
import logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# Configurações de banco de dados
DATABASE_PATH = "lotofacil.db"

# Configurações de web scraping
SCRAPING_TIMEOUT = 30
SCRAPING_HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

# URLs para web scraping
LOTOFACIL_URL = "https://www.lotofacil.com.br/resultados"

# Configurações de cache
CACHE_DURATION_HOURS = 24

# Configurações de admin
ADMIN_EMAILS = [
    "admin@lotofacil.com",
    "admin@example.com"
]

def is_admin_email(email: str) -> bool:
    """Verifica se um email é de admin"""
    return email in ADMIN_EMAILS

def get_database_path() -> str:
    """Retorna o caminho do banco de dados"""
    return DATABASE_PATH

def get_scraping_config() -> dict:
    """Retorna configurações de web scraping"""
    return {
        'timeout': SCRAPING_TIMEOUT,
        'headers': SCRAPING_HEADERS,
        'url': LOTOFACIL_URL
    }
