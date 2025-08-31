#!/usr/bin/env python3
"""
Script de inicialização do LotoLab Backend
"""

import uvicorn
import os
import sys
from pathlib import Path

def main():
    """Função principal de inicialização"""
    
    # Configurar diretório de trabalho
    backend_dir = Path(__file__).parent
    os.chdir(backend_dir)
    
    # Verificar se as dependências estão instaladas
    try:
        import fastapi
        import pandas
        import numpy
        import matplotlib
        import seaborn
        import selenium
    except ImportError as e:
        print(f"❌ Erro: Dependência não encontrada: {e}")
        print("💡 Execute: pip install -r requirements.txt")
        sys.exit(1)
    
    # Configurações do servidor
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    reload = os.getenv("RELOAD", "true").lower() == "true"
    
    print("🚀 Iniciando LotoLab Backend...")
    print(f"📍 Host: {host}")
    print(f"🔌 Porta: {port}")
    print(f"🔄 Reload: {reload}")
    print("📊 API disponível em: http://localhost:8000")
    print("📚 Documentação: http://localhost:8000/docs")
    print("🔍 Health Check: http://localhost:8000/health")
    print("\n" + "="*50)
    
    try:
        # Iniciar servidor
        uvicorn.run(
            "app.main:app",
            host=host,
            port=port,
            reload=reload,
            log_level="info"
        )
    except KeyboardInterrupt:
        print("\n\n🛑 Servidor interrompido pelo usuário")
    except Exception as e:
        print(f"\n❌ Erro ao iniciar servidor: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
