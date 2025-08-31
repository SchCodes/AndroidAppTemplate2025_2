# 🎯 LotoLab - Análise Estatística da Lotofácil

## 📋 Descrição

O **LotoLab** é uma aplicação completa para análise estatística da Lotofácil, integrando um backend Python com FastAPI e um aplicativo Android. O projeto utiliza técnicas avançadas de análise de dados, web scraping e geração de gráficos para fornecer insights sobre os padrões dos sorteios.

## 🚀 Funcionalidades Principais

### Backend Python
- **Análise Estatística Avançada**: Cálculo de frequências, correlações e padrões temporais
- **Web Scraping Automatizado**: Coleta de dados dos sorteios oficiais
- **Geração de Gráficos**: Visualizações interativas com matplotlib e seaborn
- **API REST Completa**: Endpoints para todas as funcionalidades
- **Geração de Combinações Inteligentes**: Algoritmos baseados em estatística

### Aplicativo Android
- **Interface Intuitiva**: Design moderno e responsivo
- **Visualização de Dados**: Gráficos e tabelas interativas
- **Histórico de Análises**: Acompanhamento das análises realizadas
- **Sistema de Usuários**: Controle de acesso e personalização

## 🏗️ Arquitetura do Projeto

```
LotoLab/
├── backend/                 # Backend Python
│   ├── app/
│   │   ├── main.py         # API FastAPI principal
│   │   ├── models.py       # Modelos do banco de dados
│   │   ├── crud.py         # Operações CRUD
│   │   ├── database.py     # Configuração do banco
│   │   ├── schemas.py      # Schemas Pydantic
│   │   ├── lotofacil_analyzer.py  # Análise estatística
│   │   └── graficos.py     # Geração de gráficos
│   ├── requirements.txt    # Dependências Python
│   └── config.py          # Configurações
├── app-android/            # Aplicativo Android
│   └── app/
│       └── src/
│           └── main/
│               └── java/
│                   └── com/
│                       └── lotolab/
│                           ├── MainActivity.kt
│                           ├── CalculadoraActivity.kt
│                           ├── GraficosActivity.kt
│                           ├── HistoricoActivity.kt
│                           └── LoginActivity.kt
└── README.md              # Esta documentação
```

## 🛠️ Tecnologias Utilizadas

### Backend
- **FastAPI**: Framework web moderno e rápido
- **SQLAlchemy**: ORM para banco de dados
- **Pandas & NumPy**: Análise de dados
- **Matplotlib & Seaborn**: Geração de gráficos
- **Selenium**: Web scraping automatizado
- **Pydantic**: Validação de dados

### Frontend (Android)
- **Kotlin**: Linguagem principal
- **Android SDK**: Framework nativo
- **Material Design**: Interface moderna
- **Retrofit**: Comunicação com API

## 📊 Funcionalidades de Análise

### 1. Análise de Frequências
- Contagem de aparições de cada número
- Identificação de números mais e menos sorteados
- Análise de tendências temporais

### 2. Padrões Temporais
- Análise por dia da semana
- Análise por mês
- Identificação de sazonalidade

### 3. Correlações entre Números
- Matriz de correlação
- Identificação de números que aparecem juntos
- Análise de dependências

### 4. Geração de Combinações
- Algoritmos baseados em estatística
- Balanceamento entre frequência e diversidade
- Personalização por perfil de risco

### 5. Análise de Prêmios
- Distribuição dos valores
- Análise de tendências
- Comparação entre diferentes acertos

## 🚀 Como Executar

### Backend Python

1. **Instalar dependências**:
```bash
cd Projeto/LotoLab/backend
pip install -r requirements.txt
```

2. **Configurar variáveis de ambiente**:
```bash
# Criar arquivo .env
DATABASE_URL=sqlite:///./lotolab.db
SECRET_KEY=sua_chave_secreta_aqui
CHROME_DRIVER_PATH=/caminho/para/chromedriver
```

3. **Executar a API**:
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Aplicativo Android

1. **Abrir no Android Studio**
2. **Configurar SDK e dependências**
3. **Executar no emulador ou dispositivo**

## 📱 Endpoints da API

### Análise Estatística
- `POST /analise/estatisticas` - Análise completa ou específica
- `POST /analise/combinacao` - Análise de combinação específica
- `GET /analise/frequencias` - Frequências de todos os números
- `GET /analise/combinacoes-recomendadas` - Combinações recomendadas
- `GET /analise/relatorio-completo` - Relatório completo

### Dados
- `POST /dados/atualizar` - Atualizar dados dos sorteios
- `GET /concursos/latest` - Último concurso
- `GET /concursos/all` - Todos os concursos

### Usuários
- `GET /usuarios/{usuario_id}` - Dados do usuário
- `GET /historico/{usuario_id}` - Histórico do usuário
- `GET /notificacoes/{usuario_id}` - Notificações

## 📊 Exemplos de Uso

### Análise de Frequências
```python
from app.lotofacil_analyzer import criar_analisador_lotofacil

analisador = criar_analisador_lotofacil()
frequencias = analisador.calcular_frequencias()
print("Números mais frequentes:", analisador._get_numeros_mais_frequentes(5))
```

### Geração de Combinações
```python
combinacoes = analisador.gerar_combinacoes_inteligentes(10)
for i, combo in enumerate(combinacoes, 1):
    print(f"Combinação {i}: {combo}")
```

### Análise de Combinação Específica
```python
minha_combinacao = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
analise = analisador.calcular_probabilidade_combinacao(minha_combinacao)
print(f"Probabilidade: {analise['probabilidade_combinada']:.2e}")
```

## 🔧 Configuração Avançada

### Banco de Dados
O projeto usa SQLite por padrão, mas pode ser configurado para PostgreSQL ou MySQL:

```python
# config.py
DATABASE_URL = "postgresql://user:password@localhost/lotolab"
```

### Web Scraping
Para configurar o Selenium:

```python
# config.py
CHROME_DRIVER_PATH = "/usr/local/bin/chromedriver"
SELENIUM_HEADLESS = True
```

### Cache
Configurar cache Redis para melhor performance:

```python
# config.py
REDIS_URL = "redis://localhost:6379"
CACHE_TTL = 3600
```

## 📈 Monitoramento e Logs

### Health Check
```bash
curl http://localhost:8000/health
```

### Logs
Os logs são configurados via variável de ambiente:
```bash
LOG_LEVEL=DEBUG
```

## 🚨 Tratamento de Erros

O sistema inclui tratamento robusto de erros:
- Validação de entrada com Pydantic
- Tratamento de exceções específicas
- Respostas de erro padronizadas
- Logs detalhados para debugging

## 🔒 Segurança

- Validação de entrada rigorosa
- Sanitização de dados
- Rate limiting (configurável)
- CORS configurado adequadamente
- Autenticação JWT (implementação futura)

## 📝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🤝 Suporte

Para suporte e dúvidas:
- Abra uma issue no GitHub
- Consulte a documentação da API em `/docs`
- Entre em contato com a equipe de desenvolvimento

## 🔮 Roadmap

### Versão 2.1
- [ ] Autenticação JWT completa
- [ ] Cache Redis
- [ ] Testes automatizados
- [ ] CI/CD pipeline

### Versão 2.2
- [ ] Análise de outras loterias
- [ ] Machine Learning para predições
- [ ] Dashboard web
- [ ] Notificações push

### Versão 3.0
- [ ] API pública
- [ ] Documentação completa
- [ ] SDK para desenvolvedores
- [ ] Marketplace de análises

---

**LotoLab** - Transformando dados em insights para a Lotofácil! 🎯📊
