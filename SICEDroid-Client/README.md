# SICEDroid Client

Aplicación cliente que consume el **Content Provider** de SICEDroid para demostrar el uso de permisos personalizados y operaciones CRUD en Android.

## Descripción

Esta aplicación es parte de la práctica de Content Providers en Android. Se conecta al Content Provider de la app SICEDroid para consultar y modificar datos académicos como:
- Perfil del estudiante
- Kardex (historial académico)
- Carga académica actual
- Calificaciones parciales y finales

## Arquitectura

```
┌─────────────────────┐         ┌──────────────────────────┐
│  SICEDroid Client   │         │      SICEDroid App       │
│                     │         │                          │
│  ┌───────────────┐   │         │  ┌──────────────────┐    │
│  │   UI Layer   │   │         │  │ Content Provider │    │
│  │  (Compose)   │   │         │  │                  │    │
│  └───────┬───────┘   │         │  │ • query()        │    │
│          │           │         │  │ • insert()       │    │
│  ┌───────▼───────┐   │         │  │ • update()       │    │
│  │  ViewModel   │   │         │  │ • delete()       │    │
│  └───────┬───────┘   │         │  └────────┬─────────┘    │
│          │           │         │           │              │
│  ┌───────▼───────┐   │  CRUD  │  ┌────────▼─────────┐    │
│  │ContentResolver│◄──┼────────┼──►│   Room Database  │    │
│  └───────────────┘   │         │  └──────────────────┘    │
└─────────────────────┘         └──────────────────────────┘
```

## Permisos Requeridos

La aplicación declara en su `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.example.marsphotos.provider.READ"/>
<uses-permission android:name="com.example.marsphotos.provider.WRITE"/>
```

Estos permisos personalizados son definidos por la app SICEDroid y protegen el acceso a los datos académicos.

## URIs del Content Provider

| Recurso | URI |
|---------|-----|
| Student | `content://com.example.marsphotos.provider/student` |
| Kardex | `content://com.example.marsphotos.provider/kardex` |
| Carga | `content://com.example.marsphotos.provider/carga` |
| Calif. Unidad | `content://com.example.marsphotos.provider/califunidad` |
| Calif. Final | `content://com.example.marsphotos.provider/califfinal` |

## Estructura del Proyecto

```
app/src/main/java/com/example/sicedroid_client/
├── MainActivity.kt              # Punto de entrada
├── data/
│   └── SicenetProviderClient.kt # Wrapper del ContentResolver
├── model/
│   └── AcademicModels.kt        # Modelos de datos
├── ui/
│   ├── theme/                   # Colores y tipografía
│   └── screens/                 # Pantallas de la app
│       ├── HomeScreen.kt        # Pantalla principal con navegación
│       ├── KardexScreen.kt      # Historial académico
│       ├── CargaScreen.kt       # Carga académica
│       ├── CalificacionesScreen.kt # Calificaciones
│       └── PermissionsScreen.kt # Panel de permisos y pruebas
└── viewmodel/
    └── AcademicViewModel.kt     # Lógica de negocio
```

## Características

- **UI Moderna**: Desarrollada con Jetpack Compose siguiendo Material Design 3
- **Navegación Inferior**: 5 pestañas (Inicio, Kardex, Carga, Calificaciones, Permisos)
- **Panel de Permisos**: Permite probar todas las operaciones CRUD y verificar permisos
- **Manejo de Errores**: Visualización clara de errores de permisos y operaciones
- **Retroalimentación Visual**: Snackbars para confirmar operaciones exitosas

## Requisitos

- Android 10+ (API 29)
- La app SICEDroid debe estar instalada y configurada
- Permisos READ y WRITE concedidos durante la instalación

## Instalación

1. Clonar o descargar este proyecto
2. Abrir en Android Studio
3. Sincronizar con Gradle
4. Ejecutar en un dispositivo o emulador
5. Asegurarse de que la app SICEDroid esté instalada primero

## Uso

1. Abre la app SICEDroid y realiza login (para que haya datos en la base de datos)
2. Abre SICEDroid Client
3. Ve a la pestaña "Permisos"
4. Ingresa una matrícula
5. Presiona "Consultar todos los datos" para probar lectura
6. Usa los botones de escritura para probar insertar/eliminar (requiere permiso WRITE)

## Demostración de Seguridad

La app demuestra los mecanismos de seguridad del Content Provider:

1. **Sin permisos**: Al intentar consultar sin permiso READ, se muestra error
2. **Solo lectura**: Se pueden ver datos pero no modificarlos
3. **Lectura y escritura**: Acceso completo a todas las operaciones

## Tecnologías

- Kotlin 1.9.20
- Jetpack Compose 2024.02.00
- Material Design 3
- Coroutines para operaciones asíncronas
- ContentResolver para comunicación entre apps

## Autores

- **ALEJANDRO PÉREZ VÁZQUEZ**
- **JUAN CARLOS MORENO LÓPEZ**

## Asignatura

Práctica: Content Provider en Android
TecNM Guanajuato

---

## Enlace al Proyecto Servidor

El código fuente de la app SICEDroid (que expone el Content Provider) se encuentra en:
https://github.com/[usuario]/App-SICEDroid
