# 📍 Alerta Local — App Android de Geofence

App que dispara alarme (som/vibração/notificação) quando você chega em locais cadastrados.

## Funcionalidades

- ✅ Cadastro ilimitado de locais (nome + coordenadas + raio)
- ✅ Modo de alarme por local: 🔊 Som / 📳 Vibração / 🔊📳 Ambos / 🔕 Silencioso
- ✅ Geofencing nativo do Google Play Services (baixo consumo de bateria)
- ✅ Foreground service com notificação persistente (Android 8+)
- ✅ Inicia automaticamente após reboot
- ✅ Tema dark (Material 3)
- ✅ Persistência SQLite

## Como compilar

### Pré-requisitos
- Android Studio (https://developer.android.com/studio)
- JDK 17+

### Passos
1. Abra o Android Studio
2. File → Open → selecione a pasta `geofence-alert`
3. Aguarde o Gradle sincronizar (5-10 min na primeira vez)
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. O APK estará em: `app/build/outputs/apk/debug/app-debug.apk`

### Instalar no celular
1. Transfira o APK para o celular (USB, Telegram, Google Drive)
2. Abra o arquivo .apk
3. Aceite "Instalar de fontes desconhecidas" se solicitado
4. Abra o app e conceda as permissões

## Permissões necessárias

| Permissão | Por que |
|-----------|---------|
| 📍 Localização (Sempre) | Detectar quando você chega no local, mesmo com app fechado |
| 🔔 Notificações | Mostrar o alerta quando chegar |
| 📳 Vibração | Vibrar no alarme |
| 🚫 Otimização de bateria | Samsung/Android matam apps em background — precisa desativar |

### ⚠️ Importante para Samsung (S23, etc.)
1. Vá em Ajustes → Apps → Alerta Local → Bateria → **Sem restrições**
2. Vá em Ajustes → Apps → Alerta Local → Permissões → Localização → **Permitir sempre**

## Como usar

1. Abra o app
2. Toque no botão **+** 
3. Preencha:
   - Nome do local (ex: "Trabalho")
   - Latitude e Longitude (use o botão 🗺️ para abrir no mapa)
   - Raio em metros (ex: 200 = 200m)
   - Modo de alarme
4. Toque em **Salvar**
5. O serviço inicia automaticamente

## Estrutura do projeto

```
geofence-alert/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mateus/geofence/
│       │   ├── MainActivity.java          # Tela principal + CRUD
│       │   ├── DatabaseHelper.java        # SQLite
│       │   ├── GeofenceLocation.java      # Model
│       │   ├── GeofenceManager.java       # Registro de geofences
│       │   ├── GeofenceService.java       # Foreground service
│       │   ├── GeofenceBroadcastReceiver.java  # Alarme
│       │   └── BootReceiver.java          # Auto-start no boot
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── dialog_location.xml
│           │   └── item_location.xml
│           └── values/
│               ├── themes.xml
│               └── strings.xml
├── build.gradle
└── settings.gradle
```
