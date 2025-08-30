"""
Backend Python para análise da Lotofácil
Baseado no repositório: https://github.com/SchCodes/Probabilidade_LotoFacil
"""

import json
import sqlite3
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import pandas as pd
import numpy as np
from typing import Dict, List, Tuple, Optional
import logging

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class LotofacilBackend:
    """Backend principal para análise da Lotofácil"""
    
    def __init__(self, db_path: str = "lotofacil.db"):
        """Inicializa o backend com conexão ao banco"""
        self.db_path = db_path
        self.init_database()
    
    def init_database(self):
        """Inicializa o banco de dados SQLite"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            # Tabela de sorteios
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS sorteios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    concurso INTEGER UNIQUE NOT NULL,
                    data_sorteio TEXT NOT NULL,
                    numeros TEXT NOT NULL,
                    data_atualizacao TEXT NOT NULL
                )
            ''')
            
            # Tabela de estatísticas
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS estatisticas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero INTEGER NOT NULL,
                    frequencia INTEGER DEFAULT 0,
                    ultima_atualizacao TEXT NOT NULL
                )
            ''')
            
            # Tabela de usuários
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT UNIQUE NOT NULL,
                    nome TEXT NOT NULL,
                    is_admin BOOLEAN DEFAULT FALSE,
                    data_criacao TEXT NOT NULL
                )
            ''')
            
            conn.commit()
            conn.close()
            logger.info("Banco de dados inicializado com sucesso")
            
        except Exception as e:
            logger.error(f"Erro ao inicializar banco: {e}")
    
    def web_scraping_lotofacil(self) -> Dict:
        """
        Realiza web scraping do site da Lotofácil
        Apenas para usuários admin
        """
        try:
            # URL do site da Lotofácil
            url = "https://www.lotofacil.com.br/resultados"
            
            # Headers para simular navegador
            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            }
            
            response = requests.get(url, headers=headers, timeout=30)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # Extrair último resultado (implementação específica baseada no site)
            # Esta é uma implementação genérica - pode precisar de ajustes
            resultados = []
            
            # Buscar por elementos que contenham os números
            numeros_elements = soup.find_all('span', class_='numero')
            
            if numeros_elements:
                for elemento in numeros_elements:
                    numero = elemento.get_text().strip()
                    if numero.isdigit() and 1 <= int(numero) <= 25:
                        resultados.append(int(numero))
            
            if len(resultados) == 15:
                return {
                    'success': True,
                    'numeros': sorted(resultados),
                    'data_atualizacao': datetime.now().isoformat()
                }
            else:
                return {
                    'success': False,
                    'error': f'Quantidade inválida de números: {len(resultados)}'
                }
                
        except Exception as e:
            logger.error(f"Erro no web scraping: {e}")
            return {
                'success': False,
                'error': str(e)
            }
    
    def atualizar_dados_lotofacil(self, concurso: int, numeros: List[int], data_sorteio: str) -> Dict:
        """
        Atualiza os dados da Lotofácil no banco
        Apenas para usuários admin
        """
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            # Verificar se o concurso já existe
            cursor.execute('SELECT id FROM sorteios WHERE concurso = ?', (concurso,))
            if cursor.fetchone():
                return {'success': False, 'error': 'Concurso já existe'}
            
            # Inserir novo sorteio
            numeros_str = ','.join(map(str, sorted(numeros)))
            cursor.execute('''
                INSERT INTO sorteios (concurso, data_sorteio, numeros, data_atualizacao)
                VALUES (?, ?, ?, ?)
            ''', (concurso, data_sorteio, numeros_str, datetime.now().isoformat()))
            
            # Atualizar estatísticas
            self._atualizar_estatisticas(numeros)
            
            conn.commit()
            conn.close()
            
            return {'success': True, 'message': 'Dados atualizados com sucesso'}
            
        except Exception as e:
            logger.error(f"Erro ao atualizar dados: {e}")
            return {'success': False, 'error': str(e)}
    
    def _atualizar_estatisticas(self, numeros: List[int]):
        """Atualiza as estatísticas dos números"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            for numero in numeros:
                # Verificar se o número já existe nas estatísticas
                cursor.execute('SELECT id, frequencia FROM estatisticas WHERE numero = ?', (numero,))
                resultado = cursor.fetchone()
                
                if resultado:
                    # Atualizar frequência
                    nova_frequencia = resultado[1] + 1
                    cursor.execute('''
                        UPDATE estatisticas 
                        SET frequencia = ?, ultima_atualizacao = ?
                        WHERE numero = ?
                    ''', (nova_frequencia, datetime.now().isoformat(), numero))
                else:
                    # Inserir novo número
                    cursor.execute('''
                        INSERT INTO estatisticas (numero, frequencia, ultima_atualizacao)
                        VALUES (?, 1, ?)
                    ''', (numero, datetime.now().isoformat()))
            
            conn.commit()
            conn.close()
            
        except Exception as e:
            logger.error(f"Erro ao atualizar estatísticas: {e}")
    
    def obter_ultimo_resultado(self) -> Dict:
        """Retorna o último resultado da Lotofácil"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            cursor.execute('''
                SELECT concurso, data_sorteio, numeros, data_atualizacao
                FROM sorteios 
                ORDER BY concurso DESC 
                LIMIT 1
            ''')
            
            resultado = cursor.fetchone()
            conn.close()
            
            if resultado:
                concurso, data_sorteio, numeros_str, data_atualizacao = resultado
                numeros = [int(n) for n in numeros_str.split(',')]
                
                return {
                    'success': True,
                    'concurso': concurso,
                    'data_sorteio': data_sorteio,
                    'numeros': numeros,
                    'data_atualizacao': data_atualizacao
                }
            else:
                return {
                    'success': False,
                    'error': 'Nenhum resultado encontrado'
                }
                
        except Exception as e:
            logger.error(f"Erro ao obter último resultado: {e}")
            return {'success': False, 'error': str(e)}
    
    def obter_estatisticas(self) -> Dict:
        """Retorna estatísticas completas da Lotofácil"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            # Estatísticas por número
            cursor.execute('''
                SELECT numero, frequencia 
                FROM estatisticas 
                ORDER BY frequencia DESC
            ''')
            
            estatisticas_numeros = []
            for row in cursor.fetchall():
                estatisticas_numeros.append({
                    'numero': row[0],
                    'frequencia': row[1]
                })
            
            # Estatísticas gerais
            cursor.execute('SELECT COUNT(*) FROM sorteios')
            total_sorteios = cursor.fetchone()[0]
            
            # Números mais sorteados
            cursor.execute('''
                SELECT numero, frequencia 
                FROM estatisticas 
                ORDER BY frequencia DESC 
                LIMIT 10
            ''')
            
            mais_sorteados = []
            for row in cursor.fetchall():
                mais_sorteados.append({
                    'numero': row[0],
                    'frequencia': row[1]
                })
            
            # Números menos sorteados
            cursor.execute('''
                SELECT numero, frequencia 
                FROM estatisticas 
                ORDER BY frequencia ASC 
                LIMIT 10
            ''')
            
            menos_sorteados = []
            for row in cursor.fetchall():
                menos_sorteados.append({
                    'numero': row[0],
                    'frequencia': row[1]
                })
            
            conn.close()
            
            return {
                'success': True,
                'total_sorteios': total_sorteios,
                'estatisticas_numeros': estatisticas_numeros,
                'mais_sorteados': mais_sorteados,
                'menos_sorteados': menos_sorteados,
                'data_atualizacao': datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"Erro ao obter estatísticas: {e}")
            return {'success': False, 'error': str(e)}
    
    def obter_historico(self, limite: int = 50) -> Dict:
        """Retorna histórico de sorteios"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            cursor.execute('''
                SELECT concurso, data_sorteio, numeros, data_atualizacao
                FROM sorteios 
                ORDER BY concurso DESC 
                LIMIT ?
            ''', (limite,))
            
            historico = []
            for row in cursor.fetchall():
                concurso, data_sorteio, numeros_str, data_atualizacao = row
                numeros = [int(n) for n in numeros_str.split(',')]
                
                historico.append({
                    'concurso': concurso,
                    'data_sorteio': data_sorteio,
                    'numeros': numeros,
                    'data_atualizacao': data_atualizacao
                })
            
            conn.close()
            
            return {
                'success': True,
                'historico': historico,
                'total': len(historico)
            }
            
        except Exception as e:
            logger.error(f"Erro ao obter histórico: {e}")
            return {'success': False, 'error': str(e)}
    
    def verificar_admin(self, email: str) -> bool:
        """Verifica se um usuário é admin"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            cursor.execute('SELECT is_admin FROM usuarios WHERE email = ?', (email,))
            resultado = cursor.fetchone()
            
            conn.close()
            
            return resultado and resultado[0]
            
        except Exception as e:
            logger.error(f"Erro ao verificar admin: {e}")
            return False
    
    def criar_usuario(self, email: str, nome: str, is_admin: bool = False) -> Dict:
        """Cria um novo usuário"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            cursor.execute('''
                INSERT INTO usuarios (email, nome, is_admin, data_criacao)
                VALUES (?, ?, ?, ?)
            ''', (email, nome, is_admin, datetime.now().isoformat()))
            
            conn.commit()
            conn.close()
            
            return {'success': True, 'message': 'Usuário criado com sucesso'}
            
        except sqlite3.IntegrityError:
            return {'success': False, 'error': 'Email já existe'}
        except Exception as e:
            logger.error(f"Erro ao criar usuário: {e}")
            return {'success': False, 'error': str(e)}

# Funções de conveniência para uso no Android
def get_backend():
    """Retorna uma instância do backend"""
    return LotofacilBackend()

def web_scraping_lotofacil():
    """Função para web scraping (admin only)"""
    backend = get_backend()
    return backend.web_scraping_lotofacil()

def obter_ultimo_resultado():
    """Função para obter último resultado"""
    backend = get_backend()
    return backend.obter_ultimo_resultado()

def obter_estatisticas():
    """Função para obter estatísticas"""
    backend = get_backend()
    return backend.obter_estatisticas()

def obter_historico(limite=50):
    """Função para obter histórico"""
    backend = get_backend()
    return backend.obter_historico(limite)

def verificar_admin(email):
    """Função para verificar se é admin"""
    backend = get_backend()
    return backend.verificar_admin(email)
