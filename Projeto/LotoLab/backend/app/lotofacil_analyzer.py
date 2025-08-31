import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from typing import List, Dict, Tuple
import json
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import warnings
warnings.filterwarnings('ignore')

class LotofacilAnalyzer:
    def __init__(self):
        self.numeros = list(range(1, 26))
        self.ultimos_resultados = []
        self.frequencias = {}
        self.estatisticas = {}
        
    def coletar_dados_lotofacil(self, num_concursos: int = 100) -> List[Dict]:
        """
        Coleta dados dos últimos concursos da Lotofácil usando web scraping
        """
        try:
            # Configuração do Chrome em modo headless
            chrome_options = Options()
            chrome_options.add_argument("--headless")
            chrome_options.add_argument("--no-sandbox")
            chrome_options.add_argument("--disable-dev-shm-usage")
            
            driver = webdriver.Chrome(options=chrome_options)
            
            resultados = []
            url_base = "https://www.loteriasonline.caixa.gov.br/silce-web/documentacao/manuais/html/manual_lotofacil_sorteio.html"
            
            driver.get(url_base)
            time.sleep(2)
            
            # Aqui você implementaria a lógica específica de scraping
            # Por enquanto, vamos usar dados simulados para demonstração
            
            for i in range(num_concursos):
                concurso = {
                    'concurso': 3000 - i,
                    'data': (datetime.now() - timedelta(days=i*3)).strftime('%Y-%m-%d'),
                    'dezenas': sorted(np.random.choice(self.numeros, 15, replace=False).tolist()),
                    'premio_15': np.random.randint(100000, 1000000),
                    'premio_14': np.random.randint(1000, 10000),
                    'premio_13': np.random.randint(100, 1000),
                    'premio_12': np.random.randint(10, 100),
                    'premio_11': np.random.randint(1, 10)
                }
                resultados.append(concurso)
            
            driver.quit()
            self.ultimos_resultados = resultados
            return resultados
            
        except Exception as e:
            print(f"Erro ao coletar dados: {e}")
            # Retorna dados simulados em caso de erro
            return self._gerar_dados_simulados(num_concursos)
    
    def _gerar_dados_simulados(self, num_concursos: int) -> List[Dict]:
        """Gera dados simulados para desenvolvimento e teste"""
        resultados = []
        for i in range(num_concursos):
            concurso = {
                'concurso': 3000 - i,
                'data': (datetime.now() - timedelta(days=i*3)).strftime('%Y-%m-%d'),
                'dezenas': sorted(np.random.choice(self.numeros, 15, replace=False).tolist()),
                'premio_15': np.random.randint(100000, 1000000),
                'premio_14': np.random.randint(1000, 10000),
                'premio_13': np.random.randint(100, 1000),
                'premio_12': np.random.randint(10, 100),
                'premio_11': np.random.randint(1, 10)
            }
            resultados.append(concurso)
        self.ultimos_resultados = resultados
        return resultados
    
    def calcular_frequencias(self) -> Dict[int, int]:
        """Calcula a frequência de cada número nos sorteios"""
        if not self.ultimos_resultados:
            self.coletar_dados_lotofacil()
        
        frequencias = {i: 0 for i in self.numeros}
        
        for resultado in self.ultimos_resultados:
            for dezena in resultado['dezenas']:
                frequencias[dezena] += 1
        
        self.frequencias = frequencias
        return frequencias
    
    def calcular_estatisticas(self) -> Dict:
        """Calcula estatísticas gerais dos sorteios"""
        if not self.ultimos_resultados:
            self.coletar_dados_lotofacil()
        
        df = pd.DataFrame(self.ultimos_resultados)
        
        # Estatísticas de premiação
        estatisticas = {
            'total_concursos': len(self.ultimos_resultados),
            'media_premio_15': df['premio_15'].mean(),
            'media_premio_14': df['premio_14'].mean(),
            'media_premio_13': df['premio_13'].mean(),
            'media_premio_12': df['premio_12'].mean(),
            'media_premio_11': df['premio_11'].mean(),
            'maior_premio_15': df['premio_15'].max(),
            'menor_premio_15': df['premio_15'].min(),
            'numeros_mais_frequentes': self._get_numeros_mais_frequentes(),
            'numeros_menos_frequentes': self._get_numeros_menos_frequentes(),
            'padroes_temporais': self._analisar_padroes_temporais()
        }
        
        self.estatisticas = estatisticas
        return estatisticas
    
    def _get_numeros_mais_frequentes(self, top_n: int = 5) -> List[Tuple[int, int]]:
        """Retorna os números mais frequentes"""
        if not self.frequencias:
            self.calcular_frequencias()
        
        sorted_freq = sorted(self.frequencias.items(), key=lambda x: x[1], reverse=True)
        return sorted_freq[:top_n]
    
    def _get_numeros_menos_frequentes(self, top_n: int = 5) -> List[Tuple[int, int]]:
        """Retorna os números menos frequentes"""
        if not self.frequencias:
            self.calcular_frequencias()
        
        sorted_freq = sorted(self.frequencias.items(), key=lambda x: x[1])
        return sorted_freq[:top_n]
    
    def _analisar_padroes_temporais(self) -> Dict:
        """Analisa padrões temporais nos sorteios"""
        if not self.ultimos_resultados:
            return {}
        
        df = pd.DataFrame(self.ultimos_resultados)
        df['data'] = pd.to_datetime(df['data'])
        df['dia_semana'] = df['data'].dt.day_name()
        df['mes'] = df['data'].dt.month
        
        padroes = {
            'concursos_por_dia_semana': df['dia_semana'].value_counts().to_dict(),
            'concursos_por_mes': df['mes'].value_counts().to_dict(),
            'tendencia_temporal': self._calcular_tendencia_temporal(df)
        }
        
        return padroes
    
    def _calcular_tendencia_temporal(self, df: pd.DataFrame) -> Dict:
        """Calcula tendências temporais nos números sorteados"""
        # Análise de tendência dos últimos 20 concursos
        ultimos_20 = df.tail(20)
        
        tendencias = {}
        for num in self.numeros:
            # Conta quantas vezes o número apareceu nos últimos 20
            count_ultimos_20 = sum(1 for _, row in ultimos_20.iterrows() if num in row['dezenas'])
            tendencias[num] = {
                'frequencia_ultimos_20': count_ultimos_20,
                'tendencia': 'alta' if count_ultimos_20 > 4 else 'media' if count_ultimos_20 > 2 else 'baixa'
            }
        
        return tendencias
    
    def gerar_combinacoes_inteligentes(self, num_combinacoes: int = 5) -> List[List[int]]:
        """Gera combinações inteligentes baseadas na análise estatística"""
        if not self.estatisticas:
            self.calcular_estatisticas()
        
        if not self.frequencias:
            self.calcular_frequencias()
        
        combinacoes = []
        
        for _ in range(num_combinacoes):
            # Estratégia: 60% números frequentes, 30% médios, 10% baixos
            numeros_frequentes = [num for num, freq in self._get_numeros_mais_frequentes(8)]
            numeros_medios = [num for num, freq in self.frequencias.items() 
                             if num not in numeros_frequentes and freq > 0]
            numeros_baixos = [num for num, freq in self._get_numeros_menos_frequentes(5)]
            
            # Seleção balanceada
            selecao = []
            selecao.extend(np.random.choice(numeros_frequentes, 9, replace=False))
            selecao.extend(np.random.choice(numeros_medios, 4, replace=False))
            selecao.extend(np.random.choice(numeros_baixos, 2, replace=False))
            
            combinacoes.append(sorted(selecao))
        
        return combinacoes
    
    def calcular_probabilidade_combinacao(self, combinacao: List[int]) -> Dict:
        """Calcula a probabilidade de uma combinação específica"""
        if not self.frequencias:
            self.calcular_frequencias()
        
        if len(combinacao) != 15:
            return {"erro": "Combinação deve ter exatamente 15 números"}
        
        # Probabilidade baseada na frequência histórica
        prob_individual = 1.0
        for num in combinacao:
            if num in self.frequencias:
                freq = self.frequencias[num]
                prob_individual *= (freq / len(self.ultimos_resultados)) if self.ultimos_resultados else 0
        
        # Probabilidade combinada (simplificada)
        prob_combinada = prob_individual * (1 / 3268760)  # Total de combinações possíveis
        
        return {
            "combinacao": combinacao,
            "probabilidade_individual": prob_individual,
            "probabilidade_combinada": prob_combinada,
            "ranking_frequencia": self._calcular_ranking_frequencia(combinacao)
        }
    
    def _calcular_ranking_frequencia(self, combinacao: List[int]) -> Dict:
        """Calcula o ranking de frequência de uma combinação"""
        if not self.frequencias:
            return {}
        
        ranking = {}
        for num in combinacao:
            if num in self.frequencias:
                ranking[num] = {
                    'frequencia': self.frequencias[num],
                    'posicao': sorted(self.frequencias.values(), reverse=True).index(self.frequencias[num]) + 1
                }
        
        return ranking
    
    def gerar_relatorio_completo(self) -> Dict:
        """Gera um relatório completo com todas as análises"""
        if not self.ultimos_resultados:
            self.coletar_dados_lotofacil()
        
        return {
            'data_geracao': datetime.now().isoformat(),
            'estatisticas': self.calcular_estatisticas(),
            'frequencias': self.calcular_frequencias(),
            'combinacoes_recomendadas': self.gerar_combinacoes_inteligentes(),
            'resumo': {
                'total_concursos_analisados': len(self.ultimos_resultados),
                'periodo_analise': f"{self.ultimos_resultados[-1]['data']} até {self.ultimos_resultados[0]['data']}",
                'numeros_mais_sorteados': self._get_numeros_mais_frequentes(3),
                'numeros_menos_sorteados': self._get_numeros_menos_frequentes(3)
            }
        }

# Função de utilidade para criar instância
def criar_analisador_lotofacil() -> LotofacilAnalyzer:
    """Cria e configura uma instância do analisador"""
    return LotofacilAnalyzer()
