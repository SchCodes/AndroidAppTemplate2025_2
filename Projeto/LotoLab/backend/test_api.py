#!/usr/bin/env python3
"""
Script de teste para a API LotoLab
"""

import requests
import json
import time
from datetime import datetime

# Configuração da API
BASE_URL = "http://localhost:8000"
HEADERS = {"Content-Type": "application/json"}

def test_health_check():
    """Testa o endpoint de health check"""
    print("🔍 Testando Health Check...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Health Check OK - Status: {data['status']}")
            print(f"   Versão: {data['version']}")
            print(f"   Timestamp: {data['timestamp']}")
        else:
            print(f"❌ Health Check falhou - Status: {response.status_code}")
    except Exception as e:
        print(f"❌ Erro no Health Check: {e}")

def test_analise_estatisticas():
    """Testa o endpoint de análise estatística"""
    print("\n📊 Testando Análise Estatística...")
    
    # Teste de análise completa
    payload = {
        "num_concursos": 50,
        "tipo_analise": "completa"
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/analise/estatisticas",
            json=payload,
            headers=HEADERS
        )
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Análise Estatística OK")
            print(f"   Total de concursos: {data['estatisticas']['total_concursos']}")
            print(f"   Números mais frequentes: {data['estatisticas']['numeros_mais_frequentes'][:3]}")
            print(f"   Combinações recomendadas: {len(data['combinacoes_recomendadas'])}")
        else:
            print(f"❌ Análise Estatística falhou - Status: {response.status_code}")
            print(f"   Resposta: {response.text}")
    except Exception as e:
        print(f"❌ Erro na Análise Estatística: {e}")

def test_frequencias():
    """Testa o endpoint de frequências"""
    print("\n📈 Testando Frequências...")
    
    try:
        response = requests.get(f"{BASE_URL}/analise/frequencias")
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Frequências OK")
            
            # Mostrar top 5 números mais frequentes
            sorted_freq = sorted(data.items(), key=lambda x: x[1], reverse=True)
            print("   Top 5 números mais frequentes:")
            for i, (num, freq) in enumerate(sorted_freq[:5], 1):
                print(f"     {i}. Número {num}: {freq} vezes")
        else:
            print(f"❌ Frequências falharam - Status: {response.status_code}")
    except Exception as e:
        print(f"❌ Erro nas Frequências: {e}")

def test_combinacoes_recomendadas():
    """Testa o endpoint de combinações recomendadas"""
    print("\n🎯 Testando Combinações Recomendadas...")
    
    try:
        response = requests.get(f"{BASE_URL}/analise/combinacoes-recomendadas?num_combinacoes=3")
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Combinações Recomendadas OK")
            print(f"   Total de combinações: {data['total']}")
            
            for i, combo in enumerate(data['combinacoes'], 1):
                print(f"   Combinação {i}: {combo}")
        else:
            print(f"❌ Combinações falharam - Status: {response.status_code}")
    except Exception as e:
        print(f"❌ Erro nas Combinações: {e}")

def test_analise_combinacao():
    """Testa o endpoint de análise de combinação específica"""
    print("\n🔍 Testando Análise de Combinação...")
    
    # Combinação de teste
    payload = {
        "numeros": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/analise/combinacao",
            json=payload,
            headers=HEADERS
        )
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Análise de Combinação OK")
            print(f"   Combinação: {data['combinacao']}")
            print(f"   Probabilidade individual: {data['probabilidade_individual']:.6f}")
            print(f"   Probabilidade combinada: {data['probabilidade_combinada']:.2e}")
        else:
            print(f"❌ Análise de Combinação falhou - Status: {response.status_code}")
            print(f"   Resposta: {response.text}")
    except Exception as e:
        print(f"❌ Erro na Análise de Combinação: {e}")

def test_relatorio_completo():
    """Testa o endpoint de relatório completo"""
    print("\n📋 Testando Relatório Completo...")
    
    try:
        response = requests.get(f"{BASE_URL}/analise/relatorio-completo")
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Relatório Completo OK")
            print(f"   Data de geração: {data['data_geracao']}")
            print(f"   Total de concursos: {data['resumo']['total_concursos_analisados']}")
            print(f"   Período de análise: {data['resumo']['periodo_analise']}")
        else:
            print(f"❌ Relatório falhou - Status: {response.status_code}")
    except Exception as e:
        print(f"❌ Erro no Relatório: {e}")

def test_atualizacao_dados():
    """Testa o endpoint de atualização de dados"""
    print("\n🔄 Testando Atualização de Dados...")
    
    try:
        response = requests.post(f"{BASE_URL}/dados/atualizar?num_concursos=20")
        
        if response.status_code == 200:
            data = response.json()
            print("✅ Atualização de Dados OK")
            print(f"   Mensagem: {data['mensagem']}")
            print(f"   Total de concursos: {data['total_concursos']}")
            print(f"   Data de atualização: {data['data_atualizacao']}")
        else:
            print(f"❌ Atualização falhou - Status: {response.status_code}")
    except Exception as e:
        print(f"❌ Erro na Atualização: {e}")

def main():
    """Função principal de teste"""
    print("🧪 INICIANDO TESTES DA API LOTOLAB")
    print("=" * 50)
    
    # Aguardar um pouco para o servidor inicializar
    print("⏳ Aguardando servidor inicializar...")
    time.sleep(2)
    
    # Executar todos os testes
    test_health_check()
    test_analise_estatisticas()
    test_frequencias()
    test_combinacoes_recomendadas()
    test_analise_combinacao()
    test_relatorio_completo()
    test_atualizacao_dados()
    
    print("\n" + "=" * 50)
    print("🏁 TESTES CONCLUÍDOS!")
    print("\n💡 Para ver a documentação da API, acesse: http://localhost:8000/docs")

if __name__ == "__main__":
    main()
