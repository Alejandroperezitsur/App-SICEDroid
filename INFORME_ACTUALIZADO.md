# SICEDroid Multiplatform (Android/Desktop/Web)

**Materia:** Programación Móvil II  
**Práctica:** Evaluación final: SICEDroid Multiplatform  
**Alumno(s):** Juan Carlos Moreno López y Alejandro Pérez Vázquez  
**Fecha:** 18/05/2026

---

## Objetivo

### General
Implementar una solución multiplataforma (Android, Desktop, Web) que consuma servicios SOAP de SICENET, autenticación segura, persistencia local offline, navegación con back stack, notificaciones de calificaciones, y exposición de datos académicos a través de un Content Provider con permisos personalizados.

### Específicos
- Desarrollar autenticación SOAP integrando el servicio `wsalumnos.asmx` de SICENET.
- Gestionar sesiones mediante cookies y persistencia local (SQLDelight Room).
- Implementar navegación con back stack manual para Android (botón atrás del sistema no cierra la app).*
- Detectar cambios en calificaciones y enviar notificaciones push locales al alumno.*
- Implementar modo offline con indicador visual de datos cacheados.*
- Implementar un Content Provider que exponga la base de datos Room (Kardex, Carga, Calificaciones).
- Configurar niveles de seguridad mediante permisos `READ` y `WRITE` personalizados.
- Empaquetar APK firmado con certificado y EXE nativo de escritorio.*

---

## Temas del plan de estudios

- Consumo de servicios Web (SOAP/XML).
- Persistencia de datos local (SQLDelight / Room Database).
- Interoperabilidad entre aplicaciones (Content Providers).
- Seguridad en Android (Permisos y HTTPS).
- Arquitectura de software móvil (MVVM + Repository Pattern).
- UI Moderna con Jetpack Compose Multiplatform.
- Kotlin Multiplatform (KMP) para Android, Desktop y Web.
- Navegación con back stack y control de botón atrás del sistema.
- Notificaciones locales multiplataforma (expect/actual).
- Empaquetado: APK firmado con JKS, EXE con WiX Toolset.

---

## Material

- **Software:** Android Studio (Flamingo+), IntelliJ IDEA, Kotlin 2.0.21, Jetpack Compose Multiplatform 1.7.0, SQLDelight 2.0.2, Ktor 3.0.0 (cliente HTTP), KSP, Room 2.6.1, Retrofit 2.9.0, OkHttp 4.11.0, WorkManager 2.9.0, WiX Toolset.
- **Hardware:** Dispositivo Android (API 24+) o Emulador, PC Windows (para escritorio).
- **Servicios Externos:** Web Service SICENET (TecNM Sur de Guanajuato) — `https://sicenet.itsur.edu.mx/ws/wsalumnos.asmx`.

---

## Marco Teórico

- **SOAP (Simple Object Access Protocol):** Protocolo estándar basado en XML para intercambio de datos entre sistemas. Se utilizó para la autenticación y consulta de datos académicos contra el servicio SICENET.

- **Content Provider:** Componente de Android que expone datos a otras aplicaciones mediante URIs. Se implementó `SicenetContentProvider` con permisos personalizados `READ`/`WRITE`.

- **MVVM (Model-View-ViewModel):** Patrón que separa la lógica de negocio (Model/Repository), la UI (View/Composable) y el estado (ViewModel con `StateFlow` y `mutableStateOf`).

- **Kotlin Multiplatform (KMP):** Permite compartir código entre Android, Desktop (JVM), Web (JS/Wasm) y iOS desde un solo módulo `commonMain`.

- **SQLDelight:** Genera tipos seguros (type-safe) para SQL desde archivos `.sq`. Se utilizó para la persistencia local en la versión multiplataforma.

- **Room:** ORM de Android para persistencia local con DAOs, utilizado en la versión nativa Android.

- **Interceptores OkHttp:** Mecanismo para monitorear, reescribir y reintentar llamadas HTTP. Se implementaron `AddCookiesInterceptor` y `ReceivedCookiesInterceptor` para mantener la sesión activa con cookies.

- **Back Stack Manual:** Pila de navegación (`MutableList<Screen>`) que permite al usuario navegar hacia atrás sin cerrar la aplicación. `HandleSystemBack` intercepta el botón atrás en Android vía `expect`/`actual`.

- **Notificaciones multiplataforma:** Patrón `expect`/`actual` para enviar notificaciones locales. En Android usa `NotificationManager` con canal dedicado; en Desktop usa `SystemTray`.

---

## Desarrollo

### Arquitectura del Proyecto

El proyecto se divide en **tres módulos** principales:

#### 1. `composeApp` (Kotlin Multiplatform)
Módulo principal con soporte para **Android**, **Desktop (Windows)** , **JS** y **Wasm**.

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose Multiplatform + Material 3 |
| Navegación | Back stack manual (`MutableList<Screen>`) + `HandleSystemBack` (expect/actual) |
| Red | Ktor HttpClient + SOAP XML |
| Datos locales | SQLDelight (6 tablas: Session, Profile, Kardex, Carga, CalifUnidad, CalifFinal) |
| Sesión | SQLDelight `SessionEntity` con auto-login |
| Offline | Cache-first: carga datos locales, luego red; indicador visual |
| Notificaciones | `expect`/`actual` + `NotificationManager` (Android) / `SystemTray` (Desktop) |
| ViewModel | `SicedroidViewModel` con `StateFlow` y back stack |

**Cambios implementados en esta evaluación:**
- **Navegación con back stack** (`navigateTo`, `goBack`, `navigateAndClearStack`) en reemplazo de `StateFlow<Screen>` directo.
- **`HandleSystemBack`** composable `expect`/`actual` para interceptar botón atrás en Android.
- **Modo offline**: `AcademicDataState.Success(isOffline = true)` cuando falla la red, con indicador visual "Modo offline - Datos almacenados".
- **Notificaciones**: detección de cambios en calificaciones parciales y finales; notificación local al alumno.

#### 2. `app` (Android Nativo)
Aplicación Android independiente con las siguientes capas:

| Capa | Tecnología |
|------|-----------|
| Navegación | Jetpack Navigation Compose (`NavHost` + `NavController`) |
| Red | Retrofit + SimpleXmlConverter + OkHttp interceptors |
| Datos locales | Room (5 tablas) |
| Sesión | SharedPreferences (`SessionManager`) |
| Offline | WorkManager + cache-first con `FetchWorker` → `StoreWorker` |
| Notificaciones | `NotificationHelper` + WorkManager + detección de cambios |
| Content Provider | `SicenetContentProvider` con permisos READ/WRITE |

**Cambios implementados en esta evaluación:**
- Migración de `mutableStateOf<AppScreen>` a **`NavHost`** + **`NavController`** con rutas: `login`, `profile`, `kardex`, `carga`, `grades`.
- Botón atrás del sistema ahora utiliza `navController.popBackStack()`.

#### 3. `SICEDroid-Client` (App Cliente)
Aplicación de demostración que consume el Content Provider.

---

### Diagrama de Navegación (composeApp)

```
LOGIN ──(login exitoso)──→ PROFILE ──(operaciones)──→ ACADEMIC_HOME
  ↑                           ↑                            │
  │                           │                     ┌──────┼──────┐
  │                           │                     │      │      │
  └──(logout)── limpia stack ─┘               KARDEX CARGA CALIFICACIONES
                                                     │
                                               goBack() → pantalla anterior
```

- `navigateAndClearStack()` se usa en login/logout para reiniciar el stack.
- `navigateTo()` hace push al stack.
- `goBack()` hace pop (solo si stack size > 1).
- `HandleSystemBack` llama a `goBack()` en Android.

---

### Capa de Red

#### composeApp (Ktor)
```kotlin
val client = HttpClient {
    install(HttpCookies)
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
    }
}
```
Peticiones SOAP con `SiceApiService.kt`. Parseo de XML a JSON con extracción manual de etiquetas SOAP y deserialización con `kotlinx.serialization`.

#### app (Retrofit + OkHttp)
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(AddCookiesInterceptor(preferences))
    .addInterceptor(ReceivedCookiesInterceptor(preferences))
    .build()
```
Interceptores para gestión automática de cookies de sesión.

---

### Capa de Datos

#### composeApp — SQLDelight (6 tablas)

| Archivo `.sq` | Tabla | PK |
|---------------|-------|----|
| `Session.sq` | `SessionEntity` | `matricula` |
| `Profile.sq` | `ProfileEntity` | `matricula` |
| `Kardex.sq` | `KardexEntity` | `(matricula, clave, periodo)` |
| `Carga.sq` | `CargaEntity` | `(matricula, nombre, grupo)` |
| `CalifUnidad.sq` | `CalifUnidadEntity` | `(matricula, materia)` |
| `CalifFinal.sq` | `CalifFinalEntity` | `(matricula, materia)` |

`LocalDataSource.kt` wrappea todas las consultas generadas, con soporte para transacciones y serialización JSON de campos complejos (parciales, operaciones).

#### app — Room (5 tablas)

Entidades: `StudentEntity`, `KardexEntity`, `CargaEntity`, `CalifUnidadEntity`, `CalifFinalEntity`. DAO con operaciones `@Transaction` para batch inserts y consultas con `Flow`.

---

### Sesión y Persistencia

- **composeApp**: `LocalDataSource.saveSession(matricula, password)` → SQLDelight. `checkSession()` en `init` del ViewModel restaura sesión automáticamente.
- **app**: `SessionManager` (SharedPreferences) con `saveSession()`, `isLoggedIn()`, `clearSession()`. Login automático al iniciar si hay sesión guardada.

---

### Modo Offline (composeApp)

Flujo en `loadAcademicData()`:
1. Carga datos desde SQLDelight (caché local) inmediatamente.
2. Lanza corrutina para obtener datos frescos desde red.
3. Si la red falla: `_academicState.value = AcademicDataState.Success(isOffline = true)`.
4. Las pantallas muestran "Modo offline - Datos almacenados" con un banner de color.

---

### Notificaciones

#### composeApp (nuevo)
- **Detección**: En `loadAcademicData()`, compara datos viejos (caché) vs nuevos (red) para calificaciones parciales y finales.
- **Envío**: `expect fun platformSendGradeNotification(title, message)`.
- **Android actual**: `NotificationManager` con canal `grades_channel`, importancia `HIGH`, permiso `POST_NOTIFICATIONS` (API 33+). Icono `android.R.drawable.ic_dialog_info`.
- **Desktop actual**: `SystemTray.displayMessage()` con `TrayIcon`.

#### app (existente)
- `NotificationHelper` con `NotificationCompat`.
- `StoreWorker` detecta cambios post-sync.
- `WorkManager` con `NetworkType.CONNECTED` constraint.

---

### Content Provider (app)

- **Authority**: `com.example.marsphotos.provider`
- **Permisos**: `com.example.marsphotos.provider.READ` y `WRITE` (protectionLevel = "dangerous").
- **URIs**: `/student`, `/kardex`, `/carga`, `/califunidad`, `/califfinal`.
- **App Cliente**: `SICEDroid-Client` consume vía `ContentResolver`.

---

## Empaquetado

### APK Android (Firmado)
```bash
gradlew.bat :composeApp:assembleRelease
```
- **Keystore**: `composeApp/sicedroid.jks` (password: `android`, alias: `SICEDroid`).
- **APK generado**: `composeApp/build/outputs/apk/release/composeApp-release.apk` (12.6 MB).
- **Firma**: `jarsigner` verifica integridad y autenticidad.

### APK Android (app módulo)
```bash
gradlew.bat :app:assembleRelease
```
- `app/build/outputs/apk/release/app-release-unsigned.apk` (11.9 MB) — requiere firma adicional.

### EXE Desktop (Windows)
```bash
gradlew.bat :composeApp:packageExe
```
- Requiere **WiX Toolset** (descargado automáticamente por Gradle).
- **EXE generado**: `composeApp/build/compose/binaries/main/exe/SICEDroid-1.0.0.exe` (123 MB).
- Incluye JRE empaquetado (runtime image) para ejecución sin JDK instalado.
- Contiene la base de datos SQLDelight embebida en `~/.sicedroid/sicenet.db`.

### Estructura de salida
```
composeApp/build/outputs/apk/
├── release/composeApp-release.apk    ← FIRMADO ✅
└── debug/composeApp-debug.apk

composeApp/build/compose/binaries/main/exe/
└── SICEDroid-1.0.0.exe               ← NATIVO ✅

app/build/outputs/apk/
├── release/app-release-unsigned.apk
└── debug/app-debug.apk
```

---

## Resultados

- **Autenticación Exitosa**: Acceso al sistema SICENET y recuperación de cookies de sesión en ambas versiones (Ktor y Retrofit).
- **Navegación fluida**: Back stack manual en composeApp + NavHost en app. El botón atrás del sistema **no cierra la app**, navega al historial anterior.
- **Persistencia offline**: Los datos académicos se cargan desde caché local inmediatamente. Si no hay red, se muestra indicador "Modo offline". La sesión persiste entre reinicios.
- **Notificaciones automáticas**: Cuando una calificación cambia (parcial o final), se envía una notificación local al alumno en Android y Desktop.
- **Exposición Segura**: La aplicación cliente (`SICEDroid-Client`) puede leer datos solo si el usuario otorga el permiso `READ`. Intentos de escritura sin permiso `WRITE` son bloqueados.
- **Interfaz fluida**: Estados reactivos con `StateFlow` y `collectAsState()` que evitan bloqueos de UI.
- **Multiplataforma**: Un solo código base para Android, Desktop (EXE), JS y Wasm. Cada plataforma con su implementación `actual` (DriverFactory, HandleSystemBack, PlatformNotifier).
- **APK firmado** con certificado JKS listo para distribución. **EXE nativo** de 123 MB con JRE empaquetado.

---

## Conclusión

La aplicación SICEDroid Multiplatform demuestra la viabilidad de desarrollar una solución académica completa utilizando Kotlin Multiplatform, compartiendo la lógica de negocio entre Android y Desktop. Las mejoras implementadas en esta evaluación —navegación con back stack, modo offline, notificaciones automáticas y empaquetado profesional— elevan la calidad de la aplicación a un nivel listo para producción.

La migración de `StateFlow` a back stack manual resolvió el problema crítico del botón atrás del sistema que cerraba la aplicación. El uso de `expect`/`actual` para notificaciones y back handler permitió mantener una base de código común mientras se aprovechan las capacidades nativas de cada plataforma.

El APK Release firmado con certificado JKS y el EXE de escritorio empaquetado con WiX Toolset demuestran el dominio del ciclo completo de desarrollo y distribución de software multiplataforma.

---

## Bibliografía

1. Google Developers. (2024). *Jetpack Compose Navigation*. Recuperado de https://developer.android.com/jetpack/compose/navigation
2. JetBrains. (2024). *Compose Multiplatform*. Recuperado de https://www.jetbrains.com/lp/compose-multiplatform/
3. Cash App. (2024). *SQLDelight*. Recuperado de https://cashapp.github.io/sqldelight/
4. Google Developers. (2024). *Room Database*. Recuperado de https://developer.android.com/training/data-storage/room
5. Square. (2024). *Retrofit*. Recuperado de https://square.github.io/retrofit/
6. JetBrains. (2024). *Ktor Client*. Recuperado de https://ktor.io/docs/client.html
7. Google Developers. (2024). *WorkManager*. Recuperado de https://developer.android.com/topic/libraries/architecture/workmanager
8. Google Developers. (2024). *Content Providers*. Recuperado de https://developer.android.com/guide/topics/providers/content-providers
9. WiX Toolset. (2024). *WiX Toolset Documentation*. Recuperado de https://wixtoolset.org/documentation/
10. TecNM SUR. (2024). *SICENET Web Service*. Recuperado de https://sicenet.itsur.edu.mx/ws/wsalumnos.asmx

---

**SICEDroid v1.0.0 — Mayo 2026**  
Código fuente: https://github.com/JuanCarlosMorenoLopez/App-SICEDroid
