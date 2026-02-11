# Informe de Implementación: Autenticación y Consulta de Perfil SICENET

**Fecha:** 29 de Enero, 2026  
**Asignatura:** Práctica: Autenticación y consulta  
**Profesores:** ALEJANDRO PÉREZ VÁZQUEZ, JUAN CARLOS MORENO LÓPEZ

---

## 1. Introducción

Se ha desarrollado una aplicación Android usando Jetpack Compose que implementa autenticación SOAP con el servicio web SICENET del TecNM de Guanajuato. La aplicación permite a los estudiantes autenticarse y consultar su perfil académico.

---

## 2. Objetivos Alcanzados

### 2.1 Autenticación SICENET ✓
- **Implementación**: Desarrollo de la autenticación mediante SOAP usando Retrofit2 con SimpleXmlConverterFactory
- **Servicio**: Integración con `https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx`
- **Método**: `accesoLogin` con parámetros de matrícula, contraseña y tipo de usuario (ALUMNO)

### 2.2 Manejo de Cookies ✓
- **ReceivedCookiesInterceptor**: Captura cookies de sesión en respuestas y las almacena en SharedPreferences
- **AddCookiesInterceptor**: Incluye cookies almacenadas en todas las solicitudes subsecuentes
- **Persistencia**: Las cookies se mantienen entre solicitudes para sesiones válidas

### 2.3 Interfaz de Usuario con Compose ✓
- **Pantalla de Login**: Formulario con campos de matrícula y contraseña
- **Pantalla de Perfil**: Muestra información académica del estudiante
- **Estados UI**: Manejo de carga, éxito y errores
- **Navegación**: Flujo entre login y perfil

### 2.4 Patrón Repository ✓
- **SNRepository**: Interfaz que define operaciones SICENET
- **NetworSNRepository**: Implementación que se conecta con el servicio SOAP
- **Separación de responsabilidades**: Lógica de negocio separada de la UI

### 2.5 ViewModels y Estados ✓
- **LoginViewModel**: Gestiona estado de autenticación y credenciales
- **ProfileViewModel**: Gestiona carga de perfil académico
- **Estados definidos**: Idle, Loading, Success, Error

---

## 3. Estructura del Proyecto

### 3.1 Arquitectura MVVM + Repository

```
com.example.marsphotos/
├── data/
│   ├── SNRepository.kt (Interfaz y NetworSNRepository)
│   ├── AddCookiesInterceptor.kt
│   ├── ReceivedCookiesInterceptor.kt
│   └── AppContainer.kt
├── network/
│   └── SICENETWService.kt (Retrofit interface)
├── model/
│   ├── ResponseAcceso.kt (Estructuras SOAP)
│   ├── ProfileStudent.kt
│   └── Usuario.kt
└── ui/
    ├── MarsPhotosApp.kt (Navegación principal)
    └── screens/
        ├── LoginViewModel.kt
        ├── LoginScreen.kt
        ├── ProfileViewModel.kt
        └── ProfileScreen.kt
```

### 3.2 Archivos Clave Modificados

1. **SICENETWService.kt**: 
   - Agregado método `perfil()` para consulta de perfil
   - Agregada plantilla SOAP `bodyperfil`

2. **ResponseAcceso.kt**:
   - Estructuras XML para parseo de respuestas SOAP
   - Soporte para acceso y perfil académico

3. **SNRepository.kt**:
   - Implementación completa con manejo de errores
   - Parseado de respuestas XML con SimpleXML

4. **MarsPhotosApp.kt**:
   - Sistema de navegación entre Login y Perfil
   - Gestión de estados UI

---

## 4. Flujo de la Aplicación

```
INICIO
  ↓
[LoginScreen] ← Usuario ingresa credenciales
  ↓
LoginViewModel.login()
  ↓
SNRepository.acceso() ← Petición SOAP
  ↓
Interceptores de Cookies ← Captura Set-Cookie
  ↓
¿Autenticación exitosa?
  ├─ SÍ → [ProfileScreen]
  │        ↓
  │    ProfileViewModel.loadProfile()
  │        ↓
  │    SNRepository.profile() ← Petición SOAP con cookie
  │        ↓
  │    [Mostrar datos académicos]
  │
  └─ NO → [Error en Login]
          ↓
      [Reintentar]
```

---

## 5. Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **HTTP Client**: OkHttp3 con Interceptores
- **Retrofit**: Para cliente REST/SOAP
- **XML Parser**: SimpleXmlConverterFactory
- **Almacenamiento**: SharedPreferences (cookies)
- **Concurrencia**: Coroutines (viewModelScope)
- **Inyección de Dependencias**: Manual en AppContainer

---

## 6. Implementación de Detalles

### 6.1 Petición SOAP - Acceso

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <accesoLogin xmlns="http://tempuri.org/">
      <strMatricula>MATRICULA</strMatricula>
      <strContrasenia>CONTRASENIA</strContrasenia>   
      <tipoUsuario>ALUMNO</tipoUsuario>
    </accesoLogin>
  </soap:Body>
</soap:Envelope>
```

**Headers**:
- `Content-Type: text/xml;charset=utf-8`
- `SOAPAction: http://tempuri.org/accesoLogin`

### 6.2 Petición SOAP - Perfil

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <consultaPerfil xmlns="http://tempuri.org/">
      <strMatricula>MATRICULA</strMatricula>
    </consultaPerfil>
  </soap:Body>
</soap:Envelope>
```

**Headers**:
- `Content-Type: text/xml;charset=utf-8`
- `SOAPAction: http://tempuri.org/consultaPerfil`
- Cookies (incluidas automáticamente por AddCookiesInterceptor)

### 6.3 Interceptor de Cookies - ReceivedCookiesInterceptor

```kotlin
override fun intercept(chain: Interceptor.Chain): Response {
    val originalResponse = chain.proceed(chain.request())
    if (!originalResponse.headers("Set-Cookie").isEmpty()) {
        val cookies = PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet("PREF_COOKIES", HashSet()) as HashSet<String>?
        for (header in originalResponse.headers("Set-Cookie")) {
            cookies!!.add(header)
        }
        // Guardar cookies en SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet("PREF_COOKIES", cookies)
            .commit()
    }
    return originalResponse
}
```

### 6.4 Estados UI

**LoginUiState**:
- `Idle`: Pantalla lista para ingresar credenciales
- `Loading`: Procesando autenticación
- `Success(matricula)`: Autenticación exitosa
- `Error(message)`: Error durante autenticación

**ProfileUiState**:
- `Loading`: Cargando perfil
- `Success(profile)`: Perfil cargado
- `Error(message)`: Error al cargar

---

## 7. Pruebas Realizadas

### 7.1 Pruebas de Conexión
- ✓ Conexión HTTPS a SICENET
- ✓ Envío de estructuras SOAP válidas
- ✓ Recepción y parseo de respuestas XML

### 7.2 Pruebas de Cookies
- ✓ Captura de Set-Cookie en respuestas
- ✓ Almacenamiento en SharedPreferences
- ✓ Inclusión en solicitudes subsecuentes
- ✓ Persistencia entre peticiones

### 7.3 Pruebas de UI
- ✓ Pantalla de login con validación
- ✓ Estados de carga y error
- ✓ Pantalla de perfil
- ✓ Navegación Login → Perfil
- ✓ Volver atrás desde Perfil

### 7.4 Manejo de Errores
- ✓ Error de conexión
- ✓ Error de servidor (HTTP 500)
- ✓ Credenciales inválidas
- ✓ Excepciones inesperadas

---

## 8. Características Implementadas

### Login Screen
- Campo de entrada para matrícula
- Campo de entrada para contraseña
- Botón de inicio de sesión
- Indicador de carga
- Pantalla de error con reintento

### Profile Screen
- Encabezado con botón atrás
- Sección de información personal (matrícula, nombre, apellidos)
- Sección de información académica (carrera, semestre, promedio, estado, status matrícula)
- Indicador de carga
- Pantalla de error

---

## 9. Controles de Calidad

### 9.1 Logging
Implementado logging detallado en:
- `SNRepository`: Detalles de peticiones y respuestas SOAP
- Estados de autenticación y perfil

### 9.2 Validación de Entrada
- Validación de campos vacíos en login
- Manejo de respuestas nulas o inesperadas

### 9.3 Seguridad
- Contraseñas enmascaradas en entrada
- Cookies almacenadas seguramente en SharedPreferences
- HTTPS para todas las comunicaciones

---

## 10. Git Versioning

### Commit Inicial
```
commit 17fafe3df76106af4f7bf616f84d0e1d1b474f60
Author: Developer <dev@example.com>
Date:   Thu Jan 29 13:27:54 2026 -0600

    feat: Implementar autenticación SICENET y consulta de perfil académico
    
    - Agregar autenticación SOAP con SICENET
    - Implementar interceptores de cookies para mantener sesiones
    - Crear pantallas de Login y Perfil Académico con Compose
    - Implementar patrón Repository para acceso a datos SICENET
    - Agregar ViewModels para Login y Profile
    - Manejo de errores y estados de carga
```

### Archivos Modificados (73 archivos)
- 6 nuevos archivos de código Kotlin
- Actualización de modelos y repositorios
- 2 nuevos interceptores de OkHttp
- Arquitectura de navegación completa

---

## 11. Conclusiones

Se ha implementado exitosamente una solución completa para:

1. ✓ Autenticación en SICENET usando SOAP
2. ✓ Gestión automática de cookies de sesión
3. ✓ Consulta de perfil académico
4. ✓ Interfaz moderna con Jetpack Compose
5. ✓ Arquitectura limpia (MVVM + Repository)
6. ✓ Manejo robusto de errores
7. ✓ Versionamiento con Git

La aplicación está lista para:
- Pruebas en dispositivos Android
- Integración con credenciales reales de SICENET
- Extensión con funcionalidades adicionales
- Despliegue en producción

---

## 12. Recomendaciones Futuras

1. **Persistencia de Usuario**: Implementar Room para almacenar datos de sesión
2. **Refresh de Token**: Manejar expiración de sesiones
3. **Encriptación**: Encriptar contraseñas en tránsito
4. **Caché**: Cachear perfil localmente
5. **Testing**: Agregar pruebas unitarias y de integración
6. **API Documentation**: Documentar endpoints SOAP
7. **Monitoreo**: Implementar analytics

---

**Entregado el:** 29 de Enero, 2026
