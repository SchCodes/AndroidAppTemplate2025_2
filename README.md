# 🎯 Lotofácil Stats - App Android com Backend Python

## 📱 Sobre o Projeto

Este é um aplicativo Android que integra um backend Python local para análise estatística da Lotofácil. O projeto combina a funcionalidade do repositório [Probabilidade_LotoFacil](https://github.com/SchCodes/Probabilidade_LotoFacil) com uma interface moderna e responsiva.

## 🚀 Funcionalidades

### ✨ Para Usuários Comuns:
- **Visualização do último resultado** da Lotofácil
- **Estatísticas em tempo real** dos números mais e menos sorteados
- **Histórico completo** de sorteios
- **Interface offline** com cache local
- **Atualizações automáticas** quando novos dados estão disponíveis

### 🔐 Para Administradores:
- **Web scraping automático** do site da Lotofácil
- **Atualização manual** de dados
- **Gerenciamento de usuários** e permissões
- **Sincronização de dados** entre backend Python e app Android

## 🏗️ Arquitetura

### 📱 Frontend (Android):
- **Kotlin** com **Material Design 3**
- **Room Database** para cache local
- **ViewModel** e **LiveData** para gerenciamento de estado
- **Navigation Component** com drawer menu
- **ViewBinding** para binding de views

### 🐍 Backend (Python Local):
- **Chaquopy** para execução Python no Android
- **SQLite** para armazenamento de dados
- **Web scraping** com BeautifulSoup e Requests
- **Análise estatística** com Pandas e NumPy

### 🗄️ Banco de Dados:
- **Room Database** (Android) + **SQLite** (Python)
- **Sincronização automática** entre ambos
- **Cache inteligente** com expiração configurável

## 🛠️ Tecnologias Utilizadas

### Android:
- Kotlin
- AndroidX
- Material Components
- Room Database
- Navigation Component
- ViewModel & LiveData
- Coroutines

### Python:
- Chaquopy (integração Python-Android)
- Pandas
- NumPy
- BeautifulSoup
- Requests
- SQLite3

## 📋 Pré-requisitos

- Android Studio Arctic Fox ou superior
- Android SDK 24+
- Python 3.11
- Gradle 7.0+

## 🔧 Configuração

### 1. Clone o Repositório
```bash
git clone https://github.com/seu-usuario/lotofacil-stats.git
cd lotofacil-stats
```

### 2. Configuração do Android Studio
1. Abra o projeto no Android Studio
2. Sincronize o Gradle
3. Aguarde o download das dependências

### 3. Configuração do Chaquopy
O plugin Chaquopy já está configurado no projeto. Ele irá:
- Baixar automaticamente o Python 3.11
- Instalar as dependências Python necessárias
- Configurar o ambiente de execução

### 4. Executar o App
1. Conecte um dispositivo Android ou inicie um emulador
2. Clique em "Run" no Android Studio
3. Aguarde a compilação e instalação

## 📱 Como Usar

### 🏠 Tela Principal:
- **Último Resultado**: Mostra o concurso mais recente
- **Próximo Sorteio**: Data e horário do próximo sorteio
- **Estatísticas Rápidas**: Total de sorteios e média de frequência
- **Números Mais/Menos Sorteados**: Visualização em chips coloridos

### 📊 Menu Drawer:
- **Início**: Tela principal com estatísticas
- **Estatísticas**: Análises detalhadas e gráficos
- **Histórico**: Lista completa de sorteios
- **Admin**: Funcionalidades administrativas
- **Configurações**: Preferências do usuário
- **Sobre**: Informações do app

### 🔐 Funcionalidades Admin:
1. **Web Scraping**: Atualiza dados automaticamente
2. **Sincronização**: Força atualização dos dados
3. **Gerenciamento**: Controla usuários e permissões

## 🔒 Segurança

- **Web scraping** restrito apenas a usuários admin
- **Validação** de dados antes da inserção
- **Sanitização** de inputs do usuário
- **Logs** de todas as operações administrativas

## 📊 Estrutura de Dados

### Tabelas:
- **sorteios**: Resultados dos concursos
- **estatisticas**: Frequência de cada número
- **usuarios**: Dados dos usuários e permissões

### Campos Principais:
- `concurso`: Número do concurso
- `data_sorteio`: Data do sorteio
- `numeros`: Lista dos 15 números sorteados
- `frequencia`: Quantas vezes o número foi sorteado
- `is_admin`: Se o usuário é administrador

## 🚨 Solução de Problemas

### Erro de Compilação:
- Verifique se o Gradle está sincronizado
- Limpe e reconstrua o projeto
- Verifique as versões das dependências

### Erro de Python:
- Verifique se o Chaquopy está configurado
- Confirme se as dependências Python estão instaladas
- Verifique os logs do Python no Logcat

### Erro de Banco de Dados:
- Verifique as permissões de escrita
- Confirme se o Room está configurado
- Verifique a estrutura das tabelas

## 🔄 Atualizações

### Automáticas:
- **Verificação diária** de novos resultados
- **Sincronização** quando há conexão com internet
- **Cache** atualizado automaticamente

### Manuais:
- **Botão "Atualizar Dados"** na tela principal
- **Web scraping** via menu admin
- **Sincronização forçada** via configurações

## 📈 Melhorias Futuras

- [ ] **Gráficos interativos** com MPAndroidChart
- [ ] **Notificações push** para novos resultados
- [ ] **Modo escuro** personalizado
- [ ] **Exportação** de dados para Excel/CSV
- [ ] **Análises avançadas** de padrões
- [ ] **Widgets** para tela inicial
- [ ] **Backup** na nuvem

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Suporte

- **Issues**: Use o GitHub Issues para reportar bugs
- **Discussions**: Use o GitHub Discussions para dúvidas
- **Email**: seu-email@exemplo.com

## 🙏 Agradecimentos

- [SchCodes](https://github.com/SchCodes) pelo repositório original
- Comunidade Android por ferramentas e bibliotecas
- Comunidade Python por bibliotecas de análise de dados

---

**Desenvolvido com ❤️ para a comunidade de loterias**