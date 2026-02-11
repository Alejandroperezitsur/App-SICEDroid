# Resumen Técnico - Implementación SICENET

## 1. Arquitectura de la Solución

### 1.1 Capas de la Aplicación

```
┌─────────────────────────────────────────────┐
│          Presentation Layer (UI)            │
│  ├─ LoginScreen (Compose)                   │
│  └─ ProfileScreen (Compose)                 │
├─────────────────────────────────────────────┤
│          ViewModel Layer                    │
│  ├─ LoginViewModel                          │
│  └─ ProfileViewModel                        │
├─────────────────────────────────────────────┤
│          Repository Layer                   │
│  └─ SNRepository (NetworSNRepository)        │
├─────────────────────────────────────────────┤
│          Network Layer                      │
│  ├─ SICENETWService (Retrofit)               │
│  ├─ AddCookiesInterceptor                   │
│  └─ ReceivedCookiesInterceptor              │
├─────────────────────────────────────────────┤
│          Remote Service                     │
│  └─ https://sicenet.surguanajuato.tecnm.mx │
└─────────────────────────────────────────────┘
```

## 2. Flujo de Datos

### 2.1 Autenticación

```
User Input
    ↓
LoginScreen
    ↓
LoginViewModel.login()
    ↓
SNRepository.acceso(matricula, contrasenia)
    ↓
SICENETWService.acceso(soapRequest)
    ↓
OkHttpClient
    ↓
ReceivedCookiesInterceptor (captura Set-Cookie)
    ↓
SharedPreferences (almacena cookies)
    ↓
Parser XML (EnvelopeSobreAcceso)
    ↓
LoginUiState.Success
    ↓
Navegar a ProfileScreen
```

### 2.2 Consulta de Perfil

```
ProfileScreen mounted
    ↓
ProfileViewModel.loadProfile(matricula)
    ↓
SNRepository.profile(matricula)
    ↓
SICENETWService.perfil(soapRequest)
    ↓
AddCookiesInterceptor (agrega cookies almacenadas)
    ↓
OkHttpClient
    ↓
SICENET valida cookies y retorna perfil
    ↓
Parser XML (EnvelopeSobrePerfil)
    ↓
ProfileUiState.Success
    ↓
Mostrar ProfileDetailScreen
```

## 3. Detalle de Interceptores

### 3.1 ReceivedCookiesInterceptor

**Propósito**: Capturar cookies de sesión en las respuestas

```kotlin
class ReceivedCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        
        // Si hay cookies Set-Cookie en la respuesta
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val cookies = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet("PREF_COOKIES", HashSet()) as HashSet<String>?
            
            // Agregar todas las cookies nuevas
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies!!.add(header)
            }
            
            // Guardar en SharedPreferences
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet("PREF_COOKIES", cookies)
                .commit()
        }
        return originalResponse
    }
}
```

**Flujo**:
1. Intercepta respuesta HTTP
2. Verifica headers "Set-Cookie"
3. Extrae valores de cookies
4. Almacena en SharedPreferences
5. Retorna respuesta original

### 3.2 AddCookiesInterceptor

**Propósito**: Incluir cookies almacenadas en todas las solicitudes

```kotlin
class AddCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet(PREF_COOKIES, HashSet()) as HashSet<String>?
        
        // Agregar cada cookie como header
        for (cookie in preferences!!) {
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build())
    }
}
```

**Flujo**:
1. Intercepta solicitud HTTP
2. Obtiene cookies de SharedPreferences
3. Agrega cada cookie como header "Cookie"
4. Procede con la solicitud modificada

## 4. Estructuras de Datos SOAP

### 4.1 Respuesta de Autenticación

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <accesoLoginResponse xmlns="http://tempuri.org/">
      <accesoLoginResult>
        true
      </accesoLoginResult>
    </accesoLoginResponse>
  </soap:Body>
</soap:Envelope>
```

**Parseo Kotlin**:
```kotlin
@Root(name = "soap:Envelope", strict = false)
data class EnvelopeSobreAcceso(
    @Element(name = "soap:Body", required = false)
    val body: BodyAccesoResponse? = null
)

@Root(name = "soap:Body", strict = false)
data class BodyAccesoResponse(
    @Element(name = "accesoLoginResponse", required = false)
    val accesoLoginResponse: AccesoLoginResponse? = null
)

@Root(name = "accesoLoginResponse", strict = false)
data class AccesoLoginResponse(
    @Element(name = "accesoLoginResult", required = false)
    val accesoLoginResult: String? = null
)
```

### 4.2 Respuesta de Perfil

La respuesta contiene un DataSet XML con información académica.

## 5. Manejo de Estados

### 5.1 LoginUiState

```
    ┌─────────┐
    │  Idle   │◄─────┐
    └────┬────┘      │
         │ login()   │
         ▼           │ resetState()
    ┌─────────┐      │
    │ Loading │      │
    └────┬────┘      │
         │           │
         ├──► Success ──────┐
         │                  │
         └──► Error ────────┘
```

### 5.2 ProfileUiState

```
    ┌─────────┐
    │ Loading │
    └────┬────┘
         │
         ├──► Success
         │
         └──► Error
```

## 6. Configuración de Retrofit

### 6.1 Setup en AppContainer

```kotlin
private val retrofitSN: Retrofit = Retrofit.Builder()
    .baseUrl("https://sicenet.surguanajuato.tecnm.mx")
    .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
    .client(client) // OkHttpClient con interceptores
    .build()

private val retrofitServiceSN: SICENETWService by lazy {
    retrofitSN.create(SICENETWService::class.java)
}
```

### 6.2 Construcción del Cliente OkHttp

```kotlin
val builder = OkHttpClient.Builder()
builder.addInterceptor(AddCookiesInterceptor(applicationContext))
builder.addInterceptor(ReceivedCookiesInterceptor(applicationContext))
client = builder.build()
```

## 7. Patrones de Diseño Utilizados

### 7.1 Repository Pattern

```
Interface SNRepository
    ├─ acceso(matricula, contrasenia): Boolean
    ├─ accesoObjeto(matricula, contrasenia): Usuario
    ├─ profile(matricula): ProfileStudent
    └─ getMatricula(): String

Implementación: NetworSNRepository
    ├─ Inyecta SICENETWService
    ├─ Maneja errores
    └─ Parsea respuestas XML
```

**Beneficios**:
- Abstracción de fuente de datos
- Fácil de testear (mock implementations)
- Desacoplamiento de UI

### 7.2 ViewModel Pattern

```
ViewModel (Android Architecture Components)
    ├─ Gestiona estado de UI
    ├─ Sobrevive a rotaciones de pantalla
    ├─ Lanza coroutines en viewModelScope
    └─ Observable (MutableState en Compose)
```

### 7.3 Factory Pattern

```kotlin
companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
            val snRepository = application.container.snRepository
            LoginViewModel(snRepository = snRepository)
        }
    }
}

// Uso
val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
```

## 8. Manejo de Errores

### 8.1 Tipos de Errores Capturados

```kotlin
try {
    val response = snApiService.acceso(soapRequest)
    // ...
} catch (e: IOException) {
    // Errores de conexión (sin red, timeout, etc)
    LoginUiState.Error("Error de conexión: ${e.message}")
} catch (e: HttpException) {
    // Errores HTTP (4xx, 5xx)
    LoginUiState.Error("Error del servidor: ${e.message}")
} catch (e: Exception) {
    // Excepciones inesperadas
    LoginUiState.Error("Error inesperado: ${e.message}")
}
```

### 8.2 Validación en UI

```kotlin
fun login() {
    if (matricula.isBlank() || contrasenia.isBlank()) {
        loginUiState = LoginUiState.Error(
            "Por favor ingresa matrícula y contraseña"
        )
        return
    }
    // Proceder con autenticación
}
```

## 9. Seguridad

### 9.1 HTTPS Obligatorio
- Todas las conexiones usan HTTPS
- Certificados validados automáticamente

### 9.2 Almacenamiento de Cookies
- SharedPreferences (privada por aplicación)
- No encriptadas (cookies ya tienen token)
- Limpias al desinstalar app

### 9.3 Enmascaramiento de Contraseña
```kotlin
OutlinedTextField(
    value = contrasenia,
    visualTransformation = PasswordVisualTransformation(),
    // ...
)
```

## 10. Coroutines

### 10.1 Ejecución de Red en IO Thread

```kotlin
fun login() {
    viewModelScope.launch(Dispatchers.IO) {
        // Ejecución en background thread
        loginUiState = try {
            val success = snRepository.acceso(matricula, contrasenia)
            // ...
        } catch (e: Exception) {
            // ...
        }
    }
}
```

**Ventajas**:
- No bloquea UI thread
- Manejo automático del ciclo de vida
- Cancelación automática al destruir ViewModel

## 11. Pruebas

### 11.1 Verificación Manual

1. **Conexión HTTPS**
   - Verificar con Wireshark o Android Profiler
   - Confirmar certificado HTTPS

2. **Cookies**
   - Device Explorer → SharedPreferences
   - Buscar "PREF_COOKIES"
   - Verificar contenido

3. **UI**
   - Capturarlas de pantalla en cada estado
   - Verificar botones y campos

### 11.2 Logs para Debug

```kotlin
Log.d("SNRepository", "Respuesta SOAP: $xmlString")
Log.d("SNRepository", "Resultado: $result")
Log.e("SNRepository", "Error en autenticación", e)
```

## 12. Deployment

### 12.1 Checklist Pre-Release

- [ ] Probar con credenciales reales
- [ ] Verificar manejo de errores
- [ ] Ejecutar en múltiples dispositivos
- [ ] Verificar consumo de batería
- [ ] Verificar consumo de datos
- [ ] Probar sin red (offline)
- [ ] Probar con red lenta

### 12.2 Versioning

```
Versión: 1.0.0
  - Autenticación SICENET
  - Consulta de perfil
  - Gestión de cookies
  - Interfaz Compose
```

---

**Documento técnico**: SICENET Implementación  
**Fecha**: 29 de Enero, 2026  
**Versión**: 1.0
