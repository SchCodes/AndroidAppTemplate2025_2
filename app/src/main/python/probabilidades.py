#!/usr/bin/env python3
"""
Script Python para cálculos de probabilidades da Lotofácil
Executado localmente no Android via Chaquopy
"""

import pandas as pd
import numpy as np
from typing import List, Dict, Any, Tuple
import json
from datetime import datetime

class AnaliseLotoFacil:
    """Classe para análise de probabilidades da Lotofácil"""
    
    def __init__(self):
        self.numeros_possiveis = list(range(1, 26))  # 1 a 25
        self.quantidade_dezenas = 15
        
    def calcular_probabilidade_simples(self, numeros_analisados: List[int]) -> Dict[str, Any]:
        """
        Calcula probabilidade simples de acerto
        """
        try:
            if not numeros_analisados:
                return {"erro": "Nenhum número fornecido"}
            
            # Valida números
            numeros_validos = [n for n in numeros_analisados if 1 <= n <= 25]
            if len(numeros_validos) != len(numeros_analisados):
                return {"erro": "Números inválidos fornecidos"}
            
            # Cálculo da probabilidade
            # C(25,15) = 3.268.760 combinações possíveis
            total_combinacoes = 3268760
            
            # Números corretos (assumindo que todos estão certos)
            numeros_corretos = len(numeros_validos)
            
            # Probabilidade de acertar exatamente X números
            probabilidades = {}
            for acertos in range(numeros_corretos + 1):
                if acertos <= 15:
                    # C(numeros_corretos, acertos) * C(25-numeros_corretos, 15-acertos) / C(25,15)
                    prob = self._calcular_probabilidade_exata(acertos, numeros_corretos)
                    probabilidades[f"{acertos}_acertos"] = {
                        "probabilidade": prob,
                        "percentual": prob * 100,
                        "chance_em_1": int(1/prob) if prob > 0 else "∞"
                    }
            
            return {
                "numeros_analisados": numeros_validos,
                "total_numeros": len(numeros_validos),
                "probabilidades": probabilidades,
                "total_combinacoes": total_combinacoes,
                "data_calculo": datetime.now().isoformat()
            }
            
        except Exception as e:
            return {"erro": f"Erro no cálculo: {str(e)}"}
    
    def _calcular_probabilidade_exata(self, acertos: int, numeros_corretos: int) -> float:
        """
        Calcula probabilidade de acertar exatamente X números
        """
        try:
            from math import comb
            
            if acertos > numeros_corretos or acertos > 15:
                return 0.0
            
            # C(numeros_corretos, acertos) * C(25-numeros_corretos, 15-acertos) / C(25,15)
            numerador = comb(numeros_corretos, acertos) * comb(25 - numeros_corretos, 15 - acertos)
            denominador = comb(25, 15)
            
            return numerador / denominador
            
        except ImportError:
            # Fallback se math.comb não estiver disponível
            return self._calcular_probabilidade_fallback(acertos, numeros_corretos)
    
    def _calcular_probabilidade_fallback(self, acertos: int, numeros_corretos: int) -> float:
        """
        Fallback para cálculo de probabilidade sem math.comb
        """
        try:
            def fatorial(n):
                if n <= 1:
                    return 1
                return n * fatorial(n - 1)
            
            def combinacao(n, k):
                if k > n:
                    return 0
                return fatorial(n) // (fatorial(k) * fatorial(n - k))
            
            numerador = combinacao(numeros_corretos, acertos) * combinacao(25 - numeros_corretos, 15 - acertos)
            denominador = combinacao(25, 15)
            
            return numerador / denominador
            
        except Exception:
            return 0.0
    
    def analisar_frequencia_numeros(self, concursos: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analisa frequência de números nos concursos
        """
        try:
            if not concursos:
                return {"erro": "Nenhum concurso fornecido"}
            
            # Conta frequência de cada número
            frequencias = {i: 0 for i in range(1, 26)}
            
            for concurso in concursos:
                if 'dezenas' in concurso:
                    for dezena in concurso['dezenas']:
                        if 1 <= dezena <= 25:
                            frequencias[dezena] += 1
            
            # Ordena por frequência
            frequencias_ordenadas = sorted(frequencias.items(), key=lambda x: x[1], reverse=True)
            
            # Calcula estatísticas
            total_concursos = len(concursos)
            media_frequencia = sum(frequencias.values()) / len(frequencias)
            
            # Números mais e menos sorteados
            mais_sorteados = frequencias_ordenadas[:5]
            menos_sorteados = frequencias_ordenadas[-5:]
            
            return {
                "total_concursos": total_concursos,
                "frequencias": dict(frequencias_ordenadas),
                "media_frequencia": media_frequencia,
                "mais_sorteados": [{"numero": num, "frequencia": freq} for num, freq in mais_sorteados],
                "menos_sorteados": [{"numero": num, "frequencia": freq} for num, freq in menos_sorteados],
                "data_analise": datetime.now().isoformat()
            }
            
        except Exception as e:
            return {"erro": f"Erro na análise: {str(e)}"}
    
    def analisar_padroes(self, concursos: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analisa padrões nos concursos
        """
        try:
            if not concursos:
                return {"erro": "Nenhum concurso fornecido"}
            
            padroes = {
                "pares_impares": [],
                "baixos_altos": [],
                "soma_total": [],
                "maior_sequencia": []
            }
            
            for concurso in concursos:
                if 'dezenas' in concurso:
                    dezenas = concurso['dezenas']
                    
                    # Pares vs Ímpares
                    pares = len([d for d in dezenas if d % 2 == 0])
                    impares = 15 - pares
                    padroes["pares_impares"].append({"pares": pares, "impares": impares})
                    
                    # Baixos vs Altos (1-12 vs 13-25)
                    baixos = len([d for d in dezenas if d <= 12])
                    altos = 15 - baixos
                    padroes["baixos_altos"].append({"baixos": baixos, "altos": altos})
                    
                    # Soma total
                    soma = sum(dezenas)
                    padroes["soma_total"].append(soma)
                    
                    # Maior sequência
                    dezenas_ordenadas = sorted(dezenas)
                    maior_seq = 1
                    seq_atual = 1
                    for i in range(1, len(dezenas_ordenadas)):
                        if dezenas_ordenadas[i] == dezenas_ordenadas[i-1] + 1:
                            seq_atual += 1
                            maior_seq = max(maior_seq, seq_atual)
                        else:
                            seq_atual = 1
                    padroes["maior_sequencia"].append(maior_seq)
            
            # Calcula médias
            resultados = {}
            for padrao, valores in padroes.items():
                if padrao == "pares_impares":
                    media_pares = sum(v["pares"] for v in valores) / len(valores)
                    media_impares = sum(v["impares"] for v in valores) / len(valores)
                    resultados[padrao] = {
                        "media_pares": round(media_pares, 2),
                        "media_impares": round(media_impares, 2)
                    }
                elif padrao == "baixos_altos":
                    media_baixos = sum(v["baixos"] for v in valores) / len(valores)
                    media_altos = sum(v["altos"] for v in valores) / len(valores)
                    resultados[padrao] = {
                        "media_baixos": round(media_baixos, 2),
                        "media_altos": round(media_altos, 2)
                    }
                else:
                    media = sum(valores) / len(valores)
                    resultados[padrao] = round(media, 2)
            
            return {
                "padroes_analisados": resultados,
                "total_concursos": len(concursos),
                "data_analise": datetime.now().isoformat()
            }
            
        except Exception as e:
            return {"erro": f"Erro na análise de padrões: {str(e)}"}
    
    def gerar_sugestao_numeros(self, concursos: List[Dict[str, Any]], quantidade: int = 15) -> Dict[str, Any]:
        """
        Gera sugestão de números baseada na análise
        """
        try:
            if not concursos:
                return {"erro": "Nenhum concurso fornecido"}
            
            # Analisa frequências
            analise_freq = self.analisar_frequencia_numeros(concursos)
            if "erro" in analise_freq:
                return analise_freq
            
            # Analisa padrões
            analise_padroes = self.analisar_padroes(concursos)
            if "erro" in analise_padroes:
                return analise_padroes
            
            # Estratégia: combina números mais frequentes com alguns menos frequentes
            frequencias = analise_freq["frequencias"]
            
            # Seleciona números baseado na frequência
            numeros_sugeridos = []
            
            # 60% dos números mais frequentes
            qtd_frequentes = int(quantidade * 0.6)
            numeros_frequentes = list(frequencias.keys())[:qtd_frequentes]
            numeros_sugeridos.extend(numeros_frequentes)
            
            # 40% dos números menos frequentes (para diversificar)
            qtd_menos_frequentes = quantidade - qtd_frequentes
            numeros_menos_frequentes = list(frequencias.keys())[-qtd_menos_frequentes:]
            numeros_sugeridos.extend(numeros_menos_frequentes)
            
            # Garante que não há duplicatas
            numeros_sugeridos = list(set(numeros_sugeridos))
            
            # Se ainda não tem 15 números, completa com os mais frequentes
            while len(numeros_sugeridos) < quantidade:
                for num in frequencias.keys():
                    if num not in numeros_sugeridos:
                        numeros_sugeridos.append(num)
                        break
                    if len(numeros_sugeridos) >= quantidade:
                        break
            
            # Ordena os números
            numeros_sugeridos = sorted(numeros_sugeridos[:quantidade])
            
            return {
                "numeros_sugeridos": numeros_sugeridos,
                "quantidade": len(numeros_sugeridos),
                "estrategia": "Combinação de números mais e menos frequentes",
                "data_geracao": datetime.now().isoformat(),
                "observacao": "Esta é apenas uma sugestão baseada em estatísticas. Não garante vitória."
            }
            
        except Exception as e:
            return {"erro": f"Erro na geração de sugestão: {str(e)}"}

# Função principal para ser chamada do Android
def executar_calculo(tipo_calculo: str, dados: str) -> str:
    """
    Função principal para ser chamada do Android via Chaquopy
    
    Args:
        tipo_calculo: Tipo de cálculo a ser executado
        dados: Dados em formato JSON string
    
    Returns:
        Resultado em formato JSON string
    """
    try:
        analisador = AnaliseLotoFacil()
        dados_dict = json.loads(dados)
        
        if tipo_calculo == "probabilidade_simples":
            numeros = dados_dict.get("numeros", [])
            resultado = analisador.calcular_probabilidade_simples(numeros)
            
        elif tipo_calculo == "frequencia_numeros":
            concursos = dados_dict.get("concursos", [])
            resultado = analisador.analisar_frequencia_numeros(concursos)
            
        elif tipo_calculo == "padroes":
            concursos = dados_dict.get("concursos", [])
            resultado = analisador.analisar_padroes(concursos)
            
        elif tipo_calculo == "sugestao_numeros":
            concursos = dados_dict.get("concursos", [])
            quantidade = dados_dict.get("quantidade", 15)
            resultado = analisador.gerar_sugestao_numeros(concursos, quantidade)
            
        else:
            resultado = {"erro": f"Tipo de cálculo não reconhecido: {tipo_calculo}"}
        
        return json.dumps(resultado, ensure_ascii=False)
        
    except Exception as e:
        return json.dumps({"erro": f"Erro na execução: {str(e)}"}, ensure_ascii=False)

# Teste local (se executado diretamente)
if __name__ == "__main__":
    # Teste básico
    numeros_teste = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    resultado = executar_calculo("probabilidade_simples", json.dumps({"numeros": numeros_teste}))
    print(f"Resultado: {resultado}")
