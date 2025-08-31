import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from typing import Dict, List, Tuple
import os
from datetime import datetime
import io
import base64

# Configurar estilo dos gráficos
plt.style.use('seaborn-v0_8')
sns.set_palette("husl")

class GeradorGraficos:
    def __init__(self, output_dir: str = "static/graficos"):
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
        
    def gerar_grafico_frequencias(self, frequencias: Dict[int, int], 
                                 save_path: str = None) -> str:
        """Gera gráfico de barras das frequências dos números"""
        plt.figure(figsize=(15, 8))
        
        # Preparar dados
        numeros = list(frequencias.keys())
        valores = list(frequencias.values())
        
        # Criar gráfico
        bars = plt.bar(numeros, valores, color='skyblue', edgecolor='navy', alpha=0.7)
        
        # Destacar números mais frequentes
        max_freq = max(valores)
        for i, (num, freq) in enumerate(zip(numeros, valores)):
            if freq == max_freq:
                bars[i].set_color('red')
                bars[i].set_alpha(0.9)
        
        plt.title('Frequência dos Números na Lotofácil', fontsize=16, fontweight='bold')
        plt.xlabel('Números', fontsize=12)
        plt.ylabel('Frequência', fontsize=12)
        plt.grid(True, alpha=0.3)
        plt.xticks(numeros)
        
        # Adicionar valores nas barras
        for i, v in enumerate(valores):
            plt.text(i, v + 0.5, str(v), ha='center', va='bottom', fontweight='bold')
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
        
        # Converter para base64 para exibição web
        img_buffer = io.BytesIO()
        plt.savefig(img_buffer, format='png', dpi=300, bbox_inches='tight')
        img_buffer.seek(0)
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode()
        plt.close()
        
        return f"data:image/png;base64,{img_base64}"
    
    def gerar_grafico_evolucao_temporal(self, resultados: List[Dict], 
                                       save_path: str = None) -> str:
        """Gera gráfico de evolução temporal dos números mais frequentes"""
        if not resultados:
            return ""
        
        df = pd.DataFrame(resultados)
        df['data'] = pd.to_datetime(df['data'])
        
        # Calcular frequências por período
        frequencias_por_periodo = {}
        for _, row in df.iterrows():
            periodo = row['data'].strftime('%Y-%m')
            if periodo not in frequencias_por_periodo:
                frequencias_por_periodo[periodo] = {i: 0 for i in range(1, 26)}
            
            for dezena in row['dezenas']:
                frequencias_por_periodo[periodo][dezena] += 1
        
        # Selecionar top 5 números mais frequentes no geral
        todas_freq = {i: 0 for i in range(1, 26)}
        for resultado in resultados:
            for dezena in resultado['dezenas']:
                todas_freq[dezena] += 1
        
        top_5 = sorted(todas_freq.items(), key=lambda x: x[1], reverse=True)[:5]
        top_numeros = [num for num, _ in top_5]
        
        # Criar gráfico
        plt.figure(figsize=(15, 8))
        
        periodos = sorted(frequencias_por_periodo.keys())
        for num in top_numeros:
            valores = [frequencias_por_periodo[periodo][num] for periodo in periodos]
            plt.plot(periodos, valores, marker='o', linewidth=2, label=f'Número {num}')
        
        plt.title('Evolução Temporal dos Números Mais Frequentes', fontsize=16, fontweight='bold')
        plt.xlabel('Período', fontsize=12)
        plt.ylabel('Frequência', fontsize=12)
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.xticks(rotation=45)
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
        
        # Converter para base64
        img_buffer = io.BytesIO()
        plt.savefig(img_buffer, format='png', dpi=300, bbox_inches='tight')
        img_buffer.seek(0)
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode()
        plt.close()
        
        return f"data:image/png;base64,{img_base64}"
    
    def gerar_grafico_distribuicao_premios(self, resultados: List[Dict], 
                                          save_path: str = None) -> str:
        """Gera gráfico de distribuição dos prêmios"""
        if not resultados:
            return ""
        
        df = pd.DataFrame(resultados)
        
        # Criar subplots
        fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(16, 12))
        
        # Prêmio 15 acertos
        ax1.hist(df['premio_15'], bins=20, color='gold', alpha=0.7, edgecolor='black')
        ax1.set_title('Distribuição - 15 Acertos', fontweight='bold')
        ax1.set_xlabel('Valor do Prêmio')
        ax1.set_ylabel('Frequência')
        ax1.grid(True, alpha=0.3)
        
        # Prêmio 14 acertos
        ax2.hist(df['premio_14'], bins=20, color='silver', alpha=0.7, edgecolor='black')
        ax2.set_title('Distribuição - 14 Acertos', fontweight='bold')
        ax2.set_xlabel('Valor do Prêmio')
        ax2.set_ylabel('Frequência')
        ax2.grid(True, alpha=0.3)
        
        # Prêmio 13 acertos
        ax3.hist(df['premio_13'], bins=20, color='brown', alpha=0.7, edgecolor='black')
        ax3.set_title('Distribuição - 13 Acertos', fontweight='bold')
        ax3.set_xlabel('Valor do Prêmio')
        ax3.set_ylabel('Frequência')
        ax3.grid(True, alpha=0.3)
        
        # Box plot comparativo
        premios_data = [df['premio_15'], df['premio_14'], df['premio_13']]
        ax4.boxplot(premios_data, labels=['15 acertos', '14 acertos', '13 acertos'])
        ax4.set_title('Comparação dos Prêmios', fontweight='bold')
        ax4.set_ylabel('Valor do Prêmio')
        ax4.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.suptitle('Análise de Distribuição dos Prêmios', fontsize=18, fontweight='bold', y=1.02)
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
        
        # Converter para base64
        img_buffer = io.BytesIO()
        plt.savefig(img_buffer, format='png', dpi=300, bbox_inches='tight')
        img_buffer.seek(0)
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode()
        plt.close()
        
        return f"data:image/png;base64,{img_base64}"
    
    def gerar_grafico_padroes_temporais(self, resultados: List[Dict], 
                                       save_path: str = None) -> str:
        """Gera gráfico de padrões temporais"""
        if not resultados:
            return ""
        
        df = pd.DataFrame(resultados)
        df['data'] = pd.to_datetime(df['data'])
        df['dia_semana'] = df['data'].dt.day_name()
        df['mes'] = df['data'].dt.month
        
        # Criar subplots
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        
        # Concursos por dia da semana
        dias_semana = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
        dias_pt = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo']
        
        contagem_dias = df['dia_semana'].value_counts()
        contagem_dias = contagem_dias.reindex(dias_semana)
        
        bars1 = ax1.bar(dias_pt, contagem_dias.values, color='lightcoral', alpha=0.7)
        ax1.set_title('Concursos por Dia da Semana', fontweight='bold')
        ax1.set_xlabel('Dia da Semana')
        ax1.set_ylabel('Número de Concursos')
        ax1.grid(True, alpha=0.3)
        
        # Concursos por mês
        meses = list(range(1, 13))
        meses_nomes = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 
                      'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
        
        contagem_meses = df['mes'].value_counts().reindex(meses, fill_value=0)
        
        bars2 = ax2.bar(meses_nomes, contagem_meses.values, color='lightblue', alpha=0.7)
        ax2.set_title('Concursos por Mês', fontweight='bold')
        ax2.set_xlabel('Mês')
        ax2.set_ylabel('Número de Concursos')
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
        
        # Converter para base64
        img_buffer = io.BytesIO()
        plt.savefig(img_buffer, format='png', dpi=300, bbox_inches='tight')
        img_buffer.seek(0)
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode()
        plt.close()
        
        return f"data:image/png;base64,{img_base64}"
    
    def gerar_grafico_heatmap_correlacao(self, resultados: List[Dict], 
                                        save_path: str = None) -> str:
        """Gera heatmap de correlação entre números"""
        if not resultados:
            return ""
        
        # Criar matriz de correlação
        matriz_correlacao = np.zeros((25, 25))
        
        for resultado in resultados:
            dezenas = resultado['dezenas']
            for i, num1 in enumerate(dezenas):
                for j, num2 in enumerate(dezenas):
                    if i != j:
                        matriz_correlacao[num1-1][num2-1] += 1
        
        # Normalizar
        matriz_correlacao = matriz_correlacao / len(resultados)
        
        # Criar gráfico
        plt.figure(figsize=(12, 10))
        
        sns.heatmap(matriz_correlacao, 
                   annot=True, 
                   fmt='.3f', 
                   cmap='YlOrRd',
                   xticklabels=range(1, 26),
                   yticklabels=range(1, 26))
        
        plt.title('Matriz de Correlação entre Números', fontsize=16, fontweight='bold')
        plt.xlabel('Números')
        plt.ylabel('Números')
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
        
        # Converter para base64
        img_buffer = io.BytesIO()
        plt.savefig(img_buffer, format='png', dpi=300, bbox_inches='tight')
        img_buffer.seek(0)
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode()
        plt.close()
        
        return f"data:image/png;base64,{img_base64}"
    
    def gerar_relatorio_visual_completo(self, resultados: List[Dict], 
                                       frequencias: Dict[int, int]) -> Dict[str, str]:
        """Gera todos os gráficos e retorna URLs base64"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        graficos = {}
        
        try:
            # Frequências
            graficos['frequencias'] = self.gerar_grafico_frequencias(
                frequencias, 
                f"{self.output_dir}/frequencias_{timestamp}.png"
            )
            
            # Evolução temporal
            graficos['evolucao_temporal'] = self.gerar_grafico_evolucao_temporal(
                resultados, 
                f"{self.output_dir}/evolucao_{timestamp}.png"
            )
            
            # Distribuição de prêmios
            graficos['distribuicao_premios'] = self.gerar_grafico_distribuicao_premios(
                resultados, 
                f"{self.output_dir}/premios_{timestamp}.png"
            )
            
            # Padrões temporais
            graficos['padroes_temporais'] = self.gerar_grafico_padroes_temporais(
                resultados, 
                f"{self.output_dir}/padroes_{timestamp}.png"
            )
            
            # Heatmap de correlação
            graficos['correlacao'] = self.gerar_grafico_heatmap_correlacao(
                resultados, 
                f"{self.output_dir}/correlacao_{timestamp}.png"
            )
            
        except Exception as e:
            print(f"Erro ao gerar gráficos: {e}")
            graficos['erro'] = str(e)
        
        return graficos

# Função de utilidade
def criar_gerador_graficos(output_dir: str = "static/graficos") -> GeradorGraficos:
    """Cria uma instância do gerador de gráficos"""
    return GeradorGraficos(output_dir)
