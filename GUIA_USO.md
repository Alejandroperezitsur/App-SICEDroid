# SICENET Autenticación - Aplicación Android Kotlin Compose

## 📱 Descripción

Aplicación Android que implementa autenticación SOAP con el servicio web SICENET del TecNM de Guanajuato. Permite a estudiantes autenticarse y consultar su perfil académico.

## 🎯 Características

- ✅ Autenticación SOAP con SICENET
- ✅ Gestión automática de cookies de sesión
- ✅ Interfaz moderna con Jetpack Compose
- ✅ Arquitectura MVVM + Repository
- ✅ Manejo robusto de errores
- ✅ Indicadores de carga
- ✅ Persistencia de sesiones

## 🛠 Requisitos

- Android Studio Flamingo o superior
- JDK 17+
- Gradle 7.0+
- Android SDK 29+
- Conexión a Internet (para acceder a SICENET)

## 📦 Dependencias Principales

```gradle
// Retrofit y SOAP
retrofit = "2.9.0"
converter-simplexml = "2.9.0"

// Compose
compose-bom = "2023.10.01"
lifecycle-viewmodel-compose = "2.6.2"

// OkHttp
okhttp = "4.11.0"

// Coroutines
kotlinx-coroutines-core = "1.7.2"
```

## 🚀 Instalación y Uso

### 1. Clonar o descargar el proyecto

```bash
cd basic-android-kotlin-compose-training-mars-photos-coil-starter
```

### 2. Abrir en Android Studio

1. File → Open
2. Seleccionar la carpeta del proyecto
3. Esperar a que se sincronice Gradle

### 3. Compilar y ejecutar

```bash
# En Terminal de Android Studio
./gradlew build

# O directamente en Android Studio:
# Run → Run 'app'
```

## 📋 Flujo de la Aplicación

### 1. Pantalla de Login

```
┌─────────────────────────┐
│     SICENET             │
│  Iniciar Sesión         │
├─────────────────────────┤
│ Matrícula: [________]   │
│ Contraseña: [_______]   │
│                         │
│ [Iniciar Sesión]        │
└─────────────────────────┘
```

**Ingresa:**
- Matrícula: Tu matrícula de estudiante
- Contraseña: Tu contraseña SICENET

### 2. Proceso de Autenticación

La app realiza una petición SOAP a:
```
https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx
```

La respuesta incluye cookies que se almacenan automáticamente.

### 3. Pantalla de Perfil

Después de autenticar, ve tu perfil académico:

```
┌────────────────────────────┐
│ ◄ Perfil Académico         │
├────────────────────────────┤
│ Información Personal       │
│ ─────────────────────      │
│ Matrícula: S19120153       │
│ Nombre: Juan               │
│ Apellidos: Pérez García    │
│                            │
│ Información Académica      │
│ ─────────────────────      │
│ Carrera: Ing. Sistemas     │
│ Semestre: 6                │
│ Promedio: 8.5              │
│ Estado: Activo             │
│ Status Matrícula: Vigente  │
└────────────────────────────┘
```

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/marsphotos/
├── data/
│   ├── SNRepository.kt              # Interfaz y implementación
│   ├── AddCookiesInterceptor.kt      # Agrega cookies a peticiones
│   ├── ReceivedCookiesInterceptor.kt # Captura cookies de respuestas
│   └── AppContainer.kt              # Inyección de dependencias
├── network/
│   └── SICENETWService.kt           # Interface Retrofit para SOAP
├── model/
│   ├── ResponseAcceso.kt            # Estructuras XML SOAP
│   ├── ProfileStudent.kt            # Modelo de Perfil
│   └── Usuario.kt                   # Modelo de Usuario
└── ui/
    ├── MarsPhotosApp.kt             # Navegación principal
    └── screens/
        ├── LoginViewModel.kt        # ViewModel para Login
        ├── LoginScreen.kt           # UI de Login
        ├── ProfileViewModel.kt      # ViewModel para Perfil
        └── ProfileScreen.kt         # UI de Perfil
```

## 🔍 Detalles Técnicos

### Petición SOAP - Autenticación

```xml
POST /ws/wsalumnos.asmx HTTP/1.1
Host: sicenet.surguanajuato.tecnm.mx
Content-Type: text/xml;charset=utf-8
SOAPAction: http://tempuri.org/accesoLogin

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

### Petición SOAP - Perfil

```xml
POST /ws/wsalumnos.asmx HTTP/1.1
Host: sicenet.surguanajuato.tecnm.mx
Content-Type: text/xml;charset=utf-8
SOAPAction: http://tempuri.org/consultaPerfil
Cookie: [cookies obtenidas en autenticación]

<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <consultaPerfil xmlns="http://tempuri.org/">
      <strMatricula>MATRICULA</strMatricula>
    </consultaPerfil>
  </soap:Body>
</soap:Envelope>
```

## 🔐 Seguridad

- **HTTPS**: Todas las conexiones son seguras
- **Cookies**: Almacenadas en SharedPreferences
- **Contraseñas**: Enmascaradas en entrada
- **Validación**: Entrada validada antes de enviar

## ⚠️ Manejo de Errores

La app maneja los siguientes errores:

| Error | Mensaje | Acción |
|-------|---------|--------|
| Sin conexión | "Error de conexión" | Reintentar |
| Credenciales inválidas | "Credenciales inválidas" | Editar campos |
| Servidor no responde | "Error del servidor" | Reintentar |
| Datos incompletos | "Por favor ingresa matrícula y contraseña" | Completar campos |

## 📊 Estados de la Aplicación

### LoginUiState
- `Idle`: Listo para login
- `Loading`: Procesando autenticación
- `Success`: Autenticación exitosa
- `Error`: Error en autenticación

### ProfileUiState
- `Loading`: Cargando perfil
- `Success`: Perfil disponible
- `Error`: Error al cargar

## 🧪 Testing

Para probar la aplicación:

1. **Credenciales de prueba**: Usa tu matrícula y contraseña SICENET
2. **Verificar cookies**: Usa Android Studio Debugger para ver SharedPreferences
3. **Monitorar Red**: Usa Android Profiler para ver peticiones SOAP

## 📝 Logs

La aplicación genera logs en:
```
SNRepository: Detalles de peticiones SOAP
```

Acceder en Android Studio:
- Logcat → Filter "SNRepository"

## 🤝 Contribuciones

Este proyecto es parte de la práctica de autenticación SICENET.

## 📄 Licencia

Se proporcionan los términos de licencia en el archivo LICENSE.

## 📞 Soporte

Para problemas o preguntas:
1. Revisa el INFORME.md para detalles técnicos
2. Verifica los logs en Logcat
3. Consulta con los profesores de la asignatura

---

**Versión**: 1.0.0  
**Última actualización**: 29 de Enero, 2026
