# 📱 LotoLab - App Android

Aplicativo Android para análise de concursos da Lotofácil com integração Python via Chaquopy.

## 🚀 Funcionalidades

- ✅ **Login/Registro** via Firebase Authentication
- ✅ **Cálculos Locais** usando Python (Chaquopy)
- ✅ **API Backend** para dados atualizados
- ✅ **Controle Premium/Free** com limites de uso
- ✅ **Histórico de Cálculos** com Room Database
- ✅ **Gráficos e Estatísticas** com MPAndroidChart
- ✅ **Notificações Push** via Firebase Cloud Messaging
- ✅ **Funcionamento Offline** com dados locais

## 🛠️ Tecnologias

- **Kotlin** - Linguagem principal
- **Android Jetpack** - Components, Room, Navigation
- **Firebase** - Auth, Firestore, Cloud Messaging
- **Chaquopy** - Execução de Python no Android
- **Retrofit** - Cliente HTTP para API
- **MPAndroidChart** - Gráficos e visualizações
- **Room Database** - Banco de dados local

## 📁 Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/lotolab/app/
│   │   ├── config/           # Configurações (NetworkConfig)
│   │   ├── models/           # Modelos de dados
│   │   ├── ui/               # Activities e Fragments
│   │   ├── database/         # Room Database
│   │   ├── network/          # Retrofit e API
│   │   ├── utils/            # Utilitários
│   │   └── services/         # Serviços em background
│   └── python/               # Scripts Python (Chaquopy)
│       └── probabilidades.py # Cálculos de probabilidades
├── build.gradle.kts          # Dependências
└── AndroidManifest.xml       # Permissões e configurações
```

## 🔧 Configuração

### 1. Configurar IP da API

Edite o arquivo `NetworkConfig.kt`:

```kotlin
// DESENVOLVIMENTO LOCAL - Altere o IP da sua máquina aqui
const val DEV_IP = "192.168.1.100"  // ← Seu IP local
const val DEV_PORT = "8000"          // ← Porta da API
```

### 2. Configurar Firebase

1. Baixe o arquivo `google-services.json` do Firebase Console
2. Coloque na pasta `app/`
3. Configure as credenciais no Firebase Console

### 3. Configurar Chaquopy

O plugin já está configurado no `build.gradle.kts`. O Python será executado localmente no dispositivo.

## 📱 Telas Principais

### 1. **Login/Registro**
- Firebase Authentication
- Login com Google
- Criação de conta

### 2. **Home**
- Último concurso
- Estatísticas rápidas
- Menu de navegação

### 3. **Calculadora**
- Seleção de números (1-25)
- Cálculos locais via Python
- Resultados em tempo real

### 4. **Histórico**
- Cálculos realizados
- Filtros por tipo
- Exportação de dados

### 5. **Gráficos**
- Frequência de números
- Padrões de sorteio
- Análises estatísticas

### 6. **Perfil**
- Status premium
- Limites de uso
- Configurações

## 🔐 Controle de Acesso

### Usuários Free
- ✅ 3 cálculos por dia
- ✅ Histórico limitado (últimos 50)
- ✅ Gráficos básicos
- ❌ Notificações push

### Usuários Premium
- ✅ Cálculos ilimitados
- ✅ Histórico completo
- ✅ Gráficos avançados
- ✅ Notificações push

## 🐍 Integração Python

### Scripts Python
- `probabilidades.py` - Cálculos principais
- Execução local via Chaquopy
- Sem necessidade de servidor Python

### Tipos de Cálculo
1. **Probabilidade Simples** - Chance de acerto
2. **Frequência de Números** - Análise estatística
3. **Padrões** - Análise de tendências
4. **Sugestão de Números** - Recomendações

### Exemplo de Uso
```kotlin
// Executar cálculo Python
val python = Python.getInstance()
val module = python.getModule("probabilidades")
val resultado = module.callAttr("executar_calculo", 
    "probabilidade_simples", 
    jsonDados
)
```

## 🌐 API Backend

### Endpoints Principais
- **Saúde**: `/health`, `/ip-local`
- **Usuários**: verificação Firebase, status premium
- **Concursos**: latest, específico, todos
- **Histórico**: registro e consulta
- **Notificações**: envio e leitura

### Configuração de Rede
```kotlin
// Desenvolvimento
val baseUrl = "http://192.168.1.100:8000"

// Produção
val baseUrl = "https://seudominio.com"
```

## 📊 Banco de Dados Local

### Room Database
- **Usuarios** - Dados do usuário
- **Concursos** - Concursos da Lotofácil
- **HistoricoCalculos** - Histórico de cálculos
- **Notificacoes** - Notificações locais

### Migrations
- Suporte a atualizações de schema
- Backup automático de dados

## 🔔 Notificações

### Firebase Cloud Messaging
- Notificações push para usuários premium
- Novos concursos disponíveis
- Manutenções do sistema

### Notificações Locais
- Histórico de notificações
- Marcação de leitura
- Filtros por tipo

## 🎨 Interface

### Cores do Tema
- **Primária**: Verde loteria (#4CAF50)
- **Secundária**: Dourado (#FFD700)
- **Acento**: Azul (#2196F3)

### Componentes
- Material Design 3
- Navegação por bottom navigation
- Cards informativos
- Gráficos interativos

## 🚀 Build e Deploy

### Build Debug
```bash
./gradlew assembleDebug
```

### Build Release
```bash
./gradlew assembleRelease
```

### APK Location
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## 🐛 Solução de Problemas

### Erro de Chaquopy
```bash
# Verificar versão do Python
# Verificar dependências no requirements.txt
```

### Erro de Firebase
```bash
# Verificar google-services.json
# Verificar configurações no Console
```

### Erro de API
```bash
# Verificar IP no NetworkConfig.kt
# Verificar se o backend está rodando
```

## 📝 Logs

### Logcat
```bash
adb logcat | grep "LotoLab"
```

### Logs Python
```kotlin
Log.d("Python", "Resultado: $resultado")
```

## 🔄 Atualizações

### Backend
- API REST com versionamento
- Migrations automáticas
- Backup de dados

### App Android
- Google Play Store
- Atualizações automáticas
- Rollback em caso de erro

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs no Logcat
2. Consulte a documentação da API
3. Verifique as configurações do Firebase
4. Teste a conectividade com o backend

---

**LotoLab Android** - Análise inteligente de concursos da Lotofácil 🎯📱