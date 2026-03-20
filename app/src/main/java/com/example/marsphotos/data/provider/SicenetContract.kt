package com.example.marsphotos.data.provider

import android.net.Uri

/**
 * Contract class para el SicenetContentProvider.
 * Define las URIs, columnas y MIME types para acceder a los datos académicos.
 */
object SicenetContract {

    /**
     * Content Authority - Identificador único del Content Provider
     */
    const val CONTENT_AUTHORITY = "com.example.marsphotos.provider"

    /**
     * Base URI para el Content Provider
     */
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

    /**
     * Permisos personalizados para el Content Provider
     */
    const val PERMISSION_READ = "com.example.marsphotos.provider.READ"
    const val PERMISSION_WRITE = "com.example.marsphotos.provider.WRITE"

    /**
     * Paths para cada tabla
     */
    object Paths {
        const val STUDENT = "student"
        const val KARDEX = "kardex"
        const val CARGA = "carga"
        const val CALIF_UNIDAD = "califunidad"
        const val CALIF_FINAL = "califfinal"
    }

    /**
     * URIs para acceso a cada tabla
     */
    object Student {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.STUDENT).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${Paths.STUDENT}"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.${Paths.STUDENT}"

        // Columnas disponibles
        const val MATRICULA = "matricula"
        const val NOMBRE = "nombre"
        const val APELLIDOS = "apellidos"
        const val CARRERA = "carrera"
        const val SEMESTRE = "semestre"
        const val PROMEDIO = "promedio"
        const val ESTADO = "estado"
        const val STATUS_MATRICULA = "statusMatricula"
        const val FOTO_URL = "fotoUrl"
        const val ESPECIALIDAD = "especialidad"
        const val CDTS_REUNIDOS = "cdtsReunidos"
        const val CDTS_ACTUALES = "cdtsActuales"
        const val SEM_ACTUAL = "semActual"
        const val INSCRITO = "inscrito"
        const val ESTATUS_ACADEMICO = "estatusAcademico"
        const val ESTATUS_ALUMNO = "estatusAlumno"
        const val REINSCRIPCION_FECHA = "reinscripcionFecha"
        const val SIN_ADEUDOS = "sinAdeudos"
        const val LINEAMIENTO = "lineamiento"
        const val MOD_EDUCATIVO = "modEducativo"
        const val LAST_UPDATE = "lastUpdate"

        val DEFAULT_PROJECTION = arrayOf(
            MATRICULA, NOMBRE, APELLIDOS, CARRERA, SEMESTRE, PROMEDIO,
            ESTADO, FOTO_URL, ESPECIALIDAD, CDTS_REUNIDOS, CDTS_ACTUALES,
            SEM_ACTUAL, INSCRITO, ESTATUS_ACADEMICO, ESTATUS_ALUMNO,
            REINSCRIPCION_FECHA, SIN_ADEUDOS, LAST_UPDATE
        )
    }

    /**
     * URIs y columnas para Kardex
     */
    object Kardex {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.KARDEX).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${Paths.KARDEX}"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.${Paths.KARDEX}"

        // Columnas disponibles
        const val MATRICULA = "matricula"
        const val CLAVE = "clave"
        const val NOMBRE = "nombre"
        const val CALIFICACION = "calificacion"
        const val ACREDITACION = "acreditacion"
        const val PERIODO = "periodo"
        const val LAST_UPDATE = "lastUpdate"

        val DEFAULT_PROJECTION = arrayOf(
            MATRICULA, CLAVE, NOMBRE, CALIFICACION, ACREDITACION, PERIODO, LAST_UPDATE
        )
    }

    /**
     * URIs y columnas para Carga Académica
     */
    object Carga {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CARGA).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${Paths.CARGA}"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.${Paths.CARGA}"

        // Columnas disponibles
        const val MATRICULA = "matricula"
        const val NOMBRE = "nombre"
        const val DOCENTE = "docente"
        const val GRUPO = "grupo"
        const val CREDITOS = "creditos"
        const val LUNES = "lunes"
        const val MARTES = "martes"
        const val MIERCOLES = "miercoles"
        const val JUEVES = "jueves"
        const val VIERNES = "viernes"
        const val SABADO = "sabado"
        const val LAST_UPDATE = "lastUpdate"

        val DEFAULT_PROJECTION = arrayOf(
            MATRICULA, NOMBRE, DOCENTE, GRUPO, CREDITOS,
            LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, LAST_UPDATE
        )
    }

    /**
     * URIs y columnas para Calificaciones por Unidad
     */
    object CalifUnidad {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CALIF_UNIDAD).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${Paths.CALIF_UNIDAD}"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.${Paths.CALIF_UNIDAD}"

        // Columnas disponibles
        const val MATRICULA = "matricula"
        const val MATERIA = "materia"
        const val PARCIALES = "parciales" // JSON array como string
        const val LAST_UPDATE = "lastUpdate"

        val DEFAULT_PROJECTION = arrayOf(
            MATRICULA, MATERIA, PARCIALES, LAST_UPDATE
        )
    }

    /**
     * URIs y columnas para Calificaciones Finales
     */
    object CalifFinal {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CALIF_FINAL).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.${Paths.CALIF_FINAL}"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.${Paths.CALIF_FINAL}"

        // Columnas disponibles
        const val MATRICULA = "matricula"
        const val MATERIA = "materia"
        const val CALIF = "calif"
        const val LAST_UPDATE = "lastUpdate"

        val DEFAULT_PROJECTION = arrayOf(
            MATRICULA, MATERIA, CALIF, LAST_UPDATE
        )
    }

    /**
     * Códigos de URI para el URI Matcher
     */
    object UriCodes {
        const val STUDENT = 100
        const val STUDENT_ID = 101
        const val KARDEX = 200
        const val KARDEX_ID = 201
        const val CARGA = 300
        const val CARGA_ID = 301
        const val CALIF_UNIDAD = 400
        const val CALIF_UNIDAD_ID = 401
        const val CALIF_FINAL = 500
        const val CALIF_FINAL_ID = 501
    }
}
