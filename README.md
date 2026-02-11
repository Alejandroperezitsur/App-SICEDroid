# SICENET Autenticación - Aplicación Android Kotlin Compose

## 📋 Descripción General

Aplicación Android que implementa autenticación SOAP con el servicio web SICENET del TecNM de Guanajuato. Permite a estudiantes autenticarse y consultar su perfil académico.

**Basado en**: Mars Photos app - Demo app de Android Basics with Compose

## 🎯 Funcionalidades

- **Autenticación SOAP**: Conexión segura con SICENET
- **Gestión de Cookies**: Almacenamiento y persistencia automática
- **Interfaz Moderna**: Desarrollada con Jetpack Compose
- **Perfil Académico**: Visualización de información del estudiante
- **Manejo de Errores**: Gestión robusta de excepciones
- **Arquitectura MVVM**: Patrón Repository implementado

## 🔑 Características Técnicas

- HTTPS/TLS para comunicación segura
- Retrofit2 con SimpleXmlConverterFactory para SOAP
- OkHttp3 con interceptores personalizados
- Coroutines para operaciones asincrónicas
- SharedPreferences para almacenamiento local
- ViewModels con estados UI
- Composables para UI declarativa

## 📖 Documentación

Este proyecto incluye documentación completa:

1. **[INFORME.md](INFORME.md)** - Informe técnico detallado
   - Objetivos y logros
   - Estructura del proyecto
   - Tecnologías utilizadas
   - Pruebas realizadas
   - Conclusiones

2. **[GUIA_USO.md](GUIA_USO.md)** - Guía de instalación y uso
   - Requisitos del sistema
   - Pasos de instalación
   - Flujo de la aplicación
   - Detalles técnicos

3. **[TECNICO.md](TECNICO.md)** - Documentación técnica
   - Arquitectura de la solución
   - Patrones de diseño
   - Flujo de datos
   - Interceptores y seguridad

4. **[RESUMEN_EJECUTIVO.md](RESUMEN_EJECUTIVO.md)** - Resumen ejecutivo
   - Entregables completados
   - Checklist de implementación
   - Git history

## 🚀 Inicio Rápido

### Requisitos
- Android Studio Flamingo+
- JDK 17+
- Android SDK 29+

### Instalación
```bash
# Clonar o descargar el proyecto
cd basic-android-kotlin-compose-training-mars-photos-coil-starter

# En Android Studio: File → Open → Seleccionar carpeta
# Esperar sincronización de Gradle

# Compilar
./gradlew build

# Ejecutar
./gradlew installDebug
```

### Uso
1. Abre la aplicación
2. Ingresa matrícula y contraseña SICENET
3. Haz clic en "Iniciar Sesión"
4. Visualiza tu perfil académico
5. Presiona atrás para cerrar sesión

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/marsphotos/
├── data/
│   ├── SNRepository.kt              # Interfaz y implementación
│   ├── AddCookiesInterceptor.kt      # Inyecta cookies
│   ├── ReceivedCookiesInterceptor.kt # Captura cookies
│   └── AppContainer.kt              # DI
├── network/
│   └── SICENETWService.kt           # Interface Retrofit
├── model/
│   ├── ResponseAcceso.kt            # Estructuras SOAP
│   ├── ProfileStudent.kt            # Perfil
│   └── Usuario.kt                   # Usuario
└── ui/
    ├── MarsPhotosApp.kt             # Navegación
    └── screens/
        ├── LoginViewModel.kt
        ├── LoginScreen.kt
        ├── ProfileViewModel.kt
        └── ProfileScreen.kt
```

## 🏗️ Arquitectura

```
UI Layer (Compose)
    ↓
ViewModel (StateManagement)
    ↓
Repository Pattern
    ↓
Network Layer (Retrofit + OkHttp)
    ↓
SICENET SOAP Service
```

## 🔐 Seguridad

- ✅ HTTPS obligatorio (TLS 1.2+)
- ✅ Cookies almacenadas seguramente
- ✅ Contraseñas enmascaradas
- ✅ Validación de entrada
- ✅ Manejo seguro de errores

## 📊 Git History

```
f860994 - docs: Agregar resumen ejecutivo del proyecto
19bc545 - docs: Agregar documento técnico detallado
24525e7 - docs: Agregar guía de uso de la aplicación
f79a418 - docs: Agregar informe detallado de implementación
17fafe3 - feat: Implementar autenticación SICENET y consulta de perfil académico
```

## 🧪 Testing

Para probar la aplicación:

1. Verifica conexión a Internet
2. Usa tus credenciales reales de SICENET
3. Revisa logs en Android Studio Logcat
4. Inspecciona SharedPreferences con Device Explorer

## 📱 Requisitos de Dispositivo

- Android 10+ (API 29)
- Memoria: 100 MB disponible
- Conexión a Internet requerida

## 🛠️ Tecnologías Utilizadas

| Componente | Versión | Propósito |
|-----------|---------|----------|
| Kotlin | 1.9+ | Lenguaje |
| Compose | 2023.10+ | UI |
| Retrofit | 2.9.0 | REST/SOAP |
| OkHttp | 4.11.0 | HTTP Client |
| Coroutines | 1.7.2 | Async |

## 📝 Pre-requisitos

Necesitas conocer:
- Composable functions en Jetpack Compose
- ViewModels y LiveData
- Coroutines para tareas largas
- Conceptos de SOAP y XML

## 👨‍💻 Desarrollo

### Agregar nuevas funcionalidades

1. Crear branch: `git checkout -b feature/nueva-funcion`
2. Realizar cambios
3. Commit: `git commit -m "feat: descripción"`
4. Push: `git push origin feature/nueva-funcion`

### Resolver bugs

1. Crear branch: `git checkout -b fix/nombre-bug`
2. Solucionar
3. Commit: `git commit -m "fix: descripción"`
4. Push: `git push origin fix/nombre-bug`

## 📞 Soporte

Para preguntas técnicas, consulta:
- [INFORME.md](INFORME.md) - Detalles de implementación
- [TECNICO.md](TECNICO.md) - Arquitectura y flujos
- [GUIA_USO.md](GUIA_USO.md) - Uso y requisitos

## 📄 Licencia

Este proyecto se proporciona bajo los términos especificados en el archivo LICENSE.

## 🎓 Asignatura

**Práctica**: Autenticación y consulta  
**Profesores**: ALEJANDRO PÉREZ VÁZQUEZ, JUAN CARLOS MORENO LÓPEZ  
**Institución**: TecNM Guanajuato

## ✅ Checklist de Implementación

- [x] Autenticación SICENET
- [x] Captura de cookies
- [x] Gestión de cookies
- [x] Pantalla de login
- [x] Pantalla de perfil
- [x] Patrón Repository
- [x] ViewModels
- [x] Manejo de errores
- [x] Indicadores de carga
- [x] Navegación
- [x] Documentación
- [x] Versionamiento Git

---

**Versión**: 1.0.0  
**Actualizado**: 29 de Enero, 2026  
**Estado**: ✅ Completo y Funcional


### [Get data from the internet](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet)
Learn how to use community-developed libraries to connect to a web service to retrieve and display data in your Android Kotlin compose app. 

### [Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository)
Learn how to improve the architecture of the app by separating the network calls into a repository.

### [Load and display images from the internet](https://developer.android.com/codelabs/basic-android-kotlin-compose-load-images)
Use the Coil library to load and display photos from the internet in your Android Compose app. 
