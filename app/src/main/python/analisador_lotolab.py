'''
Este módulo fornece a classe AnaliseLotoFacil para realizar análises estatísticas
sobre dados de concursos da Lotofácil.
'''
import pandas as pd
import numpy as np
from collections import Counter

class AnaliseLotoFacil:
    '''
    Realiza análises estatísticas em um conjunto de dados de concursos da Lotofácil.
    Os dados são esperados como um DataFrame do Pandas.
    '''
    def __init__(self, dados_concursos: pd.DataFrame, tamanho_amostra: int = 10):
        '''
        Inicializa a classe com os dados dos concursos e o tamanho da amostra para análise.

        Args:
            dados_concursos (pd.DataFrame): DataFrame contendo os dados dos concursos.
                                           Esperado ter colunas como 'Concurso', 'Data',
                                           e 'Numero_1', 'Numero_2', ..., 'Numero_15'.
            tamanho_amostra (int): Número de concursos mais recentes a serem usados em
                                   algumas análises de amostra.
        '''
        if dados_concursos is None or dados_concursos.empty:
            raise ValueError("O DataFrame de concursos não pode ser vazio.")

        self.dados = dados_concursos.copy()
        self.tamanho_amostra = tamanho_amostra
        self._validar_estrutura_dados()
        self.dados_numeros = self.dados[[f'Numero_{i+1}' for i in range(15)]]

    def _validar_estrutura_dados(self):
        '''Valida se as colunas esperadas de números estão presentes.'''
        colunas_esperadas = [f'Numero_{i+1}' for i in range(15)]
        for col in colunas_esperadas:
            if col not in self.dados.columns:
                raise ValueError(f"Coluna esperada '{col}' não encontrada no DataFrame.")
            # Garantir que os números sejam inteiros
            self.dados[col] = self.dados[col].astype(int)

    def _obter_amostra(self) -> pd.DataFrame:
        '''Retorna uma amostra dos concursos mais recentes.'''
        return self.dados.head(self.tamanho_amostra)

    def _calcular_max_sequencia(self, linha) -> int:
        '''Calcula a maior sequência de números consecutivos em uma linha (aposta).'''
        max_seq = 0
        if not linha.empty:
            numeros_ordenados = sorted(linha)
            seq_atual = 1
            for i in range(1, len(numeros_ordenados)):
                if numeros_ordenados[i] == numeros_ordenados[i-1] + 1:
                    seq_atual += 1
                else:
                    max_seq = max(max_seq, seq_atual)
                    seq_atual = 1
            max_seq = max(max_seq, seq_atual)
        return max_seq

    def _calcular_distribuicao_pares(self, linha) -> tuple[int, int]:
        '''Calcula a quantidade de números pares e ímpares em uma linha.'''
        pares = sum(1 for num in linha if num % 2 == 0)
        impares = len(linha) - pares
        return pares, impares

    def calcular_estatisticas_basicas(self) -> pd.DataFrame:
        '''Calcula estatísticas básicas para cada concurso (soma, média, max_sequencia, pares/ímpares).'''
        estats_df = self.dados.copy()
        numeros_cols = [f'Numero_{i+1}' for i in range(15)]
        
        estats_df['Soma'] = estats_df[numeros_cols].sum(axis=1)
        estats_df['Media'] = estats_df[numeros_cols].mean(axis=1)
        estats_df['Max_Sequencia'] = estats_df[numeros_cols].apply(self._calcular_max_sequencia, axis=1)
        
        dist_pares_impares = estats_df[numeros_cols].apply(self._calcular_distribuicao_pares, axis=1)
        estats_df['Pares'] = [item[0] for item in dist_pares_impares]
        estats_df['Impares'] = [item[1] for item in dist_pares_impares]
        return estats_df[['Concurso', 'Data', 'Soma', 'Media', 'Max_Sequencia', 'Pares', 'Impares']]

    def calcular_media_soma(self) -> float:
        '''Calcula a média da soma de todos os concursos.'''
        return self.dados_numeros.sum(axis=1).mean()

    def calcular_frequencia_relativa(self) -> pd.Series:
        '''Calcula a frequência relativa de cada número.'''
        todos_os_numeros = self.dados_numeros.values.flatten()
        frequencia_absoluta = Counter(todos_os_numeros)
        total_numeros_sorteados = len(todos_os_numeros)
        frequencia_relativa = {num: count / total_numeros_sorteados for num, count in frequencia_absoluta.items()}
        return pd.Series(frequencia_relativa).sort_index()

    def _calcular_media_repeticao(self) -> float:
        '''Calcula a média de números repetidos do concurso anterior.'''
        media_repeticao = []
        for i in range(len(self.dados) - 1):
            concurso_atual = set(self.dados_numeros.iloc[i])
            concurso_anterior = set(self.dados_numeros.iloc[i+1]) # i+1 é mais antigo na ordem do df original
            repetidos = len(concurso_atual.intersection(concurso_anterior))
            media_repeticao.append(repetidos)
        return np.mean(media_repeticao) if media_repeticao else 0

    def calcular_intervalo_aparicoes(self) -> pd.DataFrame:
        '''Calcula o intervalo de aparições de cada número.'''
        intervalos = {num: [] for num in range(1, 26)}
        posicao_ultima_aparicao = {num: -1 for num in range(1, 26)}

        for i, linha in self.dados_numeros.iterrows():
            numeros_concurso = set(linha)
            for num in range(1, 26):
                if num in numeros_concurso:
                    if posicao_ultima_aparicao[num] != -1:
                        intervalos[num].append(i - posicao_ultima_aparicao[num])
                    posicao_ultima_aparicao[num] = i
        
        # Calcula a média dos intervalos para cada número
        media_intervalos = {num: np.mean(lista_intervalos) if lista_intervalos else 0 
                            for num, lista_intervalos in intervalos.items()}
        return pd.DataFrame.from_dict(media_intervalos, orient='index', columns=['Media_Intervalo_Aparicoes']).sort_index()

    def analisar_combinacoes(self, combinacao: list[int]) -> dict:
        '''
        Analisa uma combinação específica com base nas estatísticas gerais.
        Retorna um dicionário com a análise.
        '''
        if len(combinacao) != 15:
            return {"erro": "A combinação deve ter 15 números."}
        if not all(1 <= num <= 25 for num in combinacao):
            return {"erro": "Todos os números devem estar entre 1 e 25."}
        if len(set(combinacao)) != 15:
            return {"erro": "Os números na combinação devem ser únicos."}

        soma_combinacao = sum(combinacao)
        media_combinacao = np.mean(combinacao)
        max_seq_combinacao = self._calcular_max_sequencia(pd.Series(combinacao))
        pares_combinacao, impares_combinacao = self._calcular_distribuicao_pares(combinacao)
        
        # Adicionar mais análises conforme necessário (ex: comparar com médias gerais)
        return {
            "combinacao": sorted(combinacao),
            "soma": soma_combinacao,
            "media": media_combinacao,
            "max_sequencia": max_seq_combinacao,
            "pares": pares_combinacao,
            "impares": impares_combinacao
        }

    def gerar_relatorio_estatistico_geral(self) -> dict:
        '''
        Gera um relatório textual com as principais estatísticas dos dados carregados.
        '''
        frequencia = self.calcular_frequencia_relativa()
        media_soma_total = self.calcular_media_soma()
        media_repeticao_anterior = self._calcular_media_repeticao()
        intervalos = self.calcular_intervalo_aparicoes()

        return {
            "total_concursos_analisados": len(self.dados),
            "media_soma_geral": media_soma_total,
            "media_numeros_repetidos_concurso_anterior": media_repeticao_anterior,
            "frequencia_relativa_numeros": frequencia.to_dict(),
            "media_intervalo_aparicoes_numeros": intervalos['Media_Intervalo_Aparicoes'].to_dict()
        }

# Exemplo de como usar a classe (esta parte não iria para o app em si):
# if __name__ == '__main__':
#     # Criar um DataFrame de exemplo
#     data_exemplo = {
#         'Concurso': [1, 2, 3],
#         'Data': ['2023-01-01', '2023-01-02', '2023-01-03'],
#         'Numero_1': [1, 2, 3], 'Numero_2': [2, 3, 4], 'Numero_3': [3, 4, 5],
#         'Numero_4': [4, 5, 6], 'Numero_5': [5, 6, 7], 'Numero_6': [6, 7, 8],
#         'Numero_7': [7, 8, 9], 'Numero_8': [8, 9, 10], 'Numero_9': [9, 10, 11],
#         'Numero_10': [10, 11, 12], 'Numero_11': [11, 12, 13], 'Numero_12': [12, 13, 14],
#         'Numero_13': [13, 14, 15], 'Numero_14': [14, 15, 16], 'Numero_15': [15, 16, 17]
#     }
#     df_concursos = pd.DataFrame(data_exemplo)
#
#     analisador = AnaliseLotoFacil(dados_concursos=df_concursos)
#     print("Estatísticas Básicas:")
#     print(analisador.calcular_estatisticas_basicas())
#     print("\nFrequência Relativa:")
#     print(analisador.calcular_frequencia_relativa())
#     print("\nRelatório Geral:")
#     print(analisador.gerar_relatorio_estatistico_geral())
#     print("\nAnálise de uma combinação:")
#     print(analisador.analisar_combinacoes([1,2,3,4,5,6,7,8,9,10,11,12,13,14,25]))
