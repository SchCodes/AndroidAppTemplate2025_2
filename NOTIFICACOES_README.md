# Sistema de Notificações - LotoLab

## Visão Geral

O sistema de notificações do LotoLab é composto por três componentes principais:

1. **FCMService** - Serviço Firebase Cloud Messaging para notificações push
2. **LocalNotificationService** - Serviço para notificações locais do app
3. **NotificationManager** - Gerenciador principal que coordena ambos os sistemas

## Arquitetura

```
┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   Firebase      │    │    FCMService       │    │ NotificationManager │
│   Cloud         │───▶│                     │───▶│                     │
│   Messaging     │    │                     │    │                     │
└─────────────────┘    └─────────────────────┘    └─────────────────────┘
                                                           │
                                                           ▼
                                               ┌─────────────────────┐
                                               │LocalNotification    │
                                               │Service              │
                                               │                     │
                                               └─────────────────────┘
```

## Componentes

### 1. FCMService

**Arquivo**: `app/src/main/java/com/lotolab/app/services/FCMService.kt`

**Responsabilidades**:
- Receber notificações push do Firebase
- Processar mensagens com dados customizados
- Criar notificações locais baseadas nas mensagens FCM
- Gerenciar tokens FCM
- Inscrever/desinscrever em tópicos

**Funcionalidades**:
- `onNewToken()` - Gerencia novos tokens FCM
- `onMessageReceived()` - Processa mensagens recebidas
- `processDataMessage()` - Processa mensagens com dados
- `createNotification()` - Cria notificações locais
- `createNotificationChannel()` - Configura canais de notificação

### 2. LocalNotificationService

**Arquivo**: `app/src/main/java/com/lotolab/app/services/LocalNotificationService.kt`

**Responsabilidades**:
- Exibir notificações locais
- Gerenciar canais de notificação
- Configurar ações de notificação
- Suportar notificações em lote

**Funcionalidades**:
- `showNotification()` - Exibe notificação individual
- `showBatchNotifications()` - Exibe múltiplas notificações
- `addNotificationActions()` - Adiciona ações baseadas no tipo
- `createNotificationChannel()` - Configura canais

### 3. NotificationManager

**Arquivo**: `app/src/main/java/com/lotolab/app/services/NotificationManager.kt`

**Responsabilidades**:
- Coordenar FCM e notificações locais
- Gerenciar tópicos FCM por usuário
- Criar notificações de diferentes tipos
- Gerenciar estado das notificações

**Funcionalidades**:
- `initializeFCM()` - Inicializa sistema FCM
- `subscribeToUserTopics()` - Gerencia tópicos por usuário
- `createNovoConcursoNotification()` - Cria notificação de novo concurso
- `createLimiteCalculosNotification()` - Cria notificação de limite
- `onUserLogin()` / `onUserLogout()` - Gerencia estado do usuário

### 4. NotificationUtils

**Arquivo**: `app/src/main/java/com/lotolab/app/utils/NotificationUtils.kt`

**Responsabilidades**:
- Utilitários para formatação de data/hora
- Validação de dados de notificação
- Filtros e ordenação
- Estatísticas de notificações

## Tipos de Notificação

### 1. Novo Concurso
- **Tipo**: `novo_concurso`
- **Prioridade**: Alta
- **Ações**: Ver Concurso, Ver Estatísticas
- **Dados**: ID do concurso, número, data, dezenas

### 2. Atualização de Estatísticas
- **Tipo**: `atualizacao_estatisticas`
- **Prioridade**: Média
- **Ações**: Atualizar
- **Dados**: Tipo de estatística, data de atualização

### 3. Premium Expirando
- **Tipo**: `premium_expirando`
- **Prioridade**: Alta (≤7 dias) / Média (>7 dias)
- **Ações**: Renovar Premium, Ver Detalhes
- **Dados**: Dias restantes, data de expiração

### 4. Manutenção
- **Tipo**: `manutencao`
- **Prioridade**: Média
- **Ações**: Ver Detalhes
- **Dados**: Tipo de manutenção, duração estimada

### 5. Limite de Cálculos
- **Tipo**: `limite_calculos`
- **Prioridade**: Alta (0 restantes) / Média (>0 restantes)
- **Ações**: Upgrade Premium, Ver Limites
- **Dados**: Cálculos restantes, limite diário

### 6. Sistema
- **Tipo**: `sistema`
- **Prioridade**: Configurável
- **Ações**: Nenhuma
- **Dados**: Título e mensagem customizáveis

## Configuração

### AndroidManifest.xml

```xml
<!-- Permissões necessárias -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Serviço FCM -->
<service
    android:name=".services.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Receiver para notificações locais -->
<receiver
    android:name=".receivers.LocalNotificationReceiver"
    android:exported="false" />
```

### firebase_config.xml

```xml
<!-- Configurações de notificação -->
<bool name="notifications_enabled">true</bool>
<bool name="notifications_sound_enabled">true</bool>
<bool name="notifications_vibration_enabled">true</bool>

<!-- Tópicos FCM -->
<string name="fcm_topic_novos_concursos">novos_concursos</string>
<string name="fcm_topic_premium">premium</string>
```

## Integração com ViewModels

### LotoLabViewModel

```kotlin
class LotoLabViewModel(
    // ... outros repositórios
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    fun inicializarNotificacoes() {
        viewModelScope.launch {
            notificationManager.initializeFCM()
        }
    }
    
    fun criarNotificacaoNovoConcurso(
        concursoId: Int,
        numeroConcurso: Int,
        dataSorteio: Date,
        dezenas: List<Int>
    ) {
        val notificacao = notificationManager.createNovoConcursoNotification(
            concursoId, numeroConcurso, dataSorteio, dezenas
        )
        notificationManager.showNotification(notificacao)
    }
}
```

### NotificacaoViewModel

```kotlin
class NotificacaoViewModel(
    private val notificacaoRepository: NotificacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {
    // Integração com sistema de notificações
}
```

## Uso Prático

### 1. Inicialização

```kotlin
// No MainActivity ou Application
val notificationManager = NotificationManager(this)
viewModel.inicializarNotificacoes()
```

### 2. Criação de Notificação

```kotlin
// Notificação de novo concurso
viewModel.criarNotificacaoNovoConcurso(
    concursoId = 1234,
    numeroConcurso = 1234,
    dataSorteio = Date(),
    dezenas = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
)

// Notificação de sistema
viewModel.criarNotificacaoSistema(
    titulo = "Atualização Disponível",
    mensagem = "Nova versão do app está disponível",
    prioridade = "media"
)
```

### 3. Gerenciamento de Estado

```kotlin
// Usuário faz login
viewModel.onUserLogin(usuario)

// Usuário faz logout
viewModel.onUserLogout()

// Status premium muda
viewModel.onPremiumStatusChanged(usuario)
```

## Tópicos FCM

### Tópicos Padrão (Todos os usuários)
- `novos_concursos` - Novos concursos da Lotofácil
- `sistema` - Notificações do sistema

### Tópicos Premium
- `premium` - Notificações exclusivas para usuários premium
- `atualizacoes` - Atualizações de estatísticas e recursos

### Inscrição Automática
- Usuários free são inscritos apenas nos tópicos padrão
- Usuários premium são inscritos em todos os tópicos
- Mudanças de status são refletidas automaticamente

## Tratamento de Erros

### FCM
- Falhas de conexão são tratadas graciosamente
- Tokens inválidos são renovados automaticamente
- Erros de inscrição em tópicos são logados

### Notificações Locais
- Canais de notificação são criados automaticamente
- Falhas de exibição são logadas
- Configurações inválidas usam valores padrão

## Logs e Debug

### Tags de Log
- `FCMService` - Serviço FCM
- `LocalNotificationService` - Notificações locais
- `NotificationManager` - Gerenciador principal
- `LocalNotificationReceiver` - Receiver local

### Informações Logadas
- Tokens FCM recebidos
- Mensagens processadas
- Notificações criadas/exibidas
- Erros e exceções
- Mudanças de estado do usuário

## Considerações de Performance

### Otimizações
- Notificações em lote para múltiplas mensagens
- Cache de configurações de notificação
- Operações assíncronas para FCM
- Limpeza automática de notificações antigas

### Limitações
- Máximo de 5 notificações por segundo (Android)
- Tamanho máximo de payload FCM: 4KB
- Limite de ações por notificação: 3

## Testes

### Testes Unitários
- Validação de dados de notificação
- Formatação de data/hora
- Filtros e ordenação
- Criação de notificações

### Testes de Integração
- Fluxo completo FCM → Notificação local
- Gerenciamento de tópicos
- Mudanças de estado do usuário

### Testes de UI
- Exibição de notificações
- Ações de notificação
- Configurações de notificação

## Próximos Passos

### Funcionalidades Futuras
- Notificações agendadas
- Templates de notificação personalizáveis
- Analytics de engajamento
- A/B testing de notificações
- Integração com analytics

### Melhorias Técnicas
- Cache de notificações offline
- Sincronização com backend
- Compressão de payload
- Retry automático para falhas FCM

## Suporte

Para dúvidas ou problemas com o sistema de notificações:

1. Verificar logs do Android Studio
2. Confirmar configuração do Firebase
3. Verificar permissões do app
4. Testar em dispositivo físico (emuladores podem ter limitações)

## Referências

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Android Notifications](https://developer.android.com/guide/topics/ui/notifiers/notifications)
- [Notification Channels](https://developer.android.com/training/notify-user/channels)
- [Pending Intents](https://developer.android.com/reference/android/app/PendingIntent)
