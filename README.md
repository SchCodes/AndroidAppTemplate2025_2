# LotoLab

Aplicativo Android para análise de probabilidades da Lotofácil com funcionalidades integradas.

## 📱 Estrutura do Projeto

### Aplicativo Principal Integrado

- **`app/`** - Aplicativo principal Android com todas as funcionalidades integradas
  - Sistema de autenticação com Firebase
  - Navegação por abas (Home, Dashboard, Lotofácil, Notifications, Profile)
  - Banco de dados local com Room
  - **Módulo Lotofácil integrado** - Análise de probabilidades, histórico e configurações
  - Integração com serviços Google

### Funcionalidades Integradas

#### Sistema Principal
- Autenticação e cadastro de usuários
- Dashboard personalizado
- Sistema de notificações
- Perfil do usuário

#### Módulo Lotofácil (Integrado)
- **Análise de Probabilidades** - Cálculos e padrões dos números
- **Histórico de Sorteios** - Visualização completa e estatísticas
- **Configurações** - Parâmetros personalizados
- **Execução Python** - Scripts de análise via Chaquopy

## 🚀 Como Executar

1. **Clone o repositório**
   ```bash
   git clone <url-do-repositorio>
   cd LotoLab
   ```

2. **Abra no Android Studio**
   - Abra o projeto no Android Studio
   - Sincronize o Gradle
   - Configure o SDK Android

3. **Configure as credenciais**
   - Adicione seu arquivo `google-services.json` na pasta `app/`
   - Configure as chaves de API necessárias

4. **Compile e execute**
   - Conecte um dispositivo Android ou use um emulador
   - Clique em "Run" no Android Studio

## 🛠️ Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Android Jetpack** - Componentes de arquitetura
- **Firebase** - Autenticação, banco de dados e serviços
- **Room** - Banco de dados local
- **Chaquopy** - Execução de código Python integrado
- **Material Design** - Interface do usuário consistente

## 📁 Estrutura de Arquivos

```
LotoLab/
├── app/                          # Aplicativo principal integrado
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/ifpr/lotolab/
│   │   │       ├── ui/          # Interfaces principais
│   │   │       │   ├── login/   # Sistema de login
│   │   │       │   ├── usuario/ # Cadastro de usuários
│   │   │       │   ├── home/    # Tela inicial
│   │   │       │   ├── dashboard/ # Painel de controle
│   │   │       │   └── notifications/ # Notificações
│   │   │       └── lotofacil/   # Módulo Lotofácil integrado
│   │   │           ├── ui/      # Atividades do módulo
│   │   │           ├── models/  # Modelos de dados
│   │   │           └── adapters/ # Adaptadores
│   │   ├── res/                 # Recursos (layouts, strings, etc.)
│   │   │   └── layout/
│   │   │       └── lotofacil/   # Layouts do módulo Lotofácil
│   │   ├── python/              # Scripts Python para análise
│   │   └── AndroidManifest.xml  # Manifesto com todas as atividades
│   └── build.gradle.kts         # Configuração do app
├── gradle/                      # Wrapper do Gradle
├── build.gradle.kts             # Configuração do projeto raiz
├── settings.gradle.kts          # Configuração dos módulos
└── README.md                    # Este arquivo
```

## 🔧 Configurações

### Versões
- **compileSdk**: 35
- **minSdk**: 24
- **targetSdk**: 35
- **Kotlin**: 1.9+
- **Gradle**: 8.0+

### Dependências Python (Chaquopy)
- pandas
- numpy
- openpyxl

## 📱 Navegação Integrada

O aplicativo utiliza navegação por abas com as seguintes seções:

1. **Home** - Tela inicial do sistema
2. **Dashboard** - Painel de controle principal
3. **Lotofácil** - **Módulo integrado** de análise de probabilidades
4. **Notifications** - Sistema de notificações
5. **Profile** - Perfil e configurações do usuário

### Fluxo do Módulo Lotofácil
- **Menu Principal** → Escolha da funcionalidade
- **Análise de Probabilidades** → Cálculos e padrões
- **Histórico de Sorteios** → Dados e estatísticas
- **Configurações** → Parâmetros personalizados

## 🎯 Regras de Negócio Implementadas

### Integração Completa
- **Módulo único**: Todas as funcionalidades em um só aplicativo
- **Navegação consistente**: Mesmo padrão visual e de navegação
- **Dados compartilhados**: Acesso às mesmas configurações e perfil
- **Experiência unificada**: Interface consistente em todo o app

### Funcionalidades do Lotofácil
- **Análise estatística** dos números mais frequentes
- **Histórico completo** de todos os sorteios
- **Configurações personalizadas** para análises
- **Execução de scripts Python** para cálculos avançados

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Suporte

Para dúvidas ou suporte, entre em contato através dos canais disponíveis no projeto.