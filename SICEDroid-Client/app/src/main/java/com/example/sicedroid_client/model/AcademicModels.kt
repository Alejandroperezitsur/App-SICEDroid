package com.example.sicedroid_client.model

/**
 * Modelo de datos para el Perfil del Estudiante
 */
data class Student(
    val matricula: String,
    val nombre: String,
    val apellidos: String,
    val carrera: String,
    val semestre: Int,
    val promedio: String,
    val estado: String = "",
    val fotoUrl: String = "",
    val especialidad: String = "",
    val cdtsReunidos: Int = 0,
    val cdtsActuales: Int = 0,
    val semActual: Int = 0,
    val inscrito: String = "",
    val estatusAcademico: String = "",
    val estatusAlumno: String = "",
    val lastUpdate: Long = 0
) {
    val nombreCompleto: String
        get() = "$nombre $apellidos"
}

/**
 * Modelo de datos para el Kardex (historial académico)
 */
data class KardexEntry(
    val matricula: String,
    val clave: String,
    val nombre: String,
    val calificacion: Int,
    val acreditacion: String,
    val periodo: String,
    val lastUpdate: Long = 0
) {
    val estaAcreditada: Boolean
        get() = calificacion >= 70
}

/**
 * Modelo de datos para la Carga Académica
 */
data class CargaEntry(
    val matricula: String,
    val nombre: String,
    val docente: String,
    val grupo: String,
    val creditos: Int,
    val lunes: String = "",
    val martes: String = "",
    val miercoles: String = "",
    val jueves: String = "",
    val viernes: String = "",
    val sabado: String = "",
    val lastUpdate: Long = 0
) {
    val horario: String
        get() = listOfNotNull(
            lunes.takeIf { it.isNotBlank() }?.let { "Lun: $it" },
            martes.takeIf { it.isNotBlank() }?.let { "Mar: $it" },
            miercoles.takeIf { it.isNotBlank() }?.let { "Mie: $it" },
            jueves.takeIf { it.isNotBlank() }?.let { "Jue: $it" },
            viernes.takeIf { it.isNotBlank() }?.let { "Vie: $it" },
            sabado.takeIf { it.isNotBlank() }?.let { "Sab: $it" }
        ).joinToString("\n")
}

/**
 * Modelo de datos para Calificaciones por Unidad
 */
data class CalifUnidad(
    val matricula: String,
    val materia: String,
    val parciales: List<String>,
    val lastUpdate: Long = 0
) {
    val promedioParcial: Double
        get() = try {
            parciales.map { it.toDoubleOrNull() ?: 0.0 }.average()
        } catch (e: Exception) {
            0.0
        }
}

/**
 * Modelo de datos para Calificaciones Finales
 */
data class CalifFinal(
    val matricula: String,
    val materia: String,
    val calif: Int,
    val lastUpdate: Long = 0
) {
    val estaAprobada: Boolean
        get() = calif >= 70
}

/**
 * Estados de resultado para operaciones con el Content Provider
 */
sealed class ProviderResult<out T> {
    data class Success<T>(val data: T) : ProviderResult<T>()
    data class Error(val message: String, val securityException: Boolean = false) : ProviderResult<Nothing>()
    object Loading : ProviderResult<Nothing>()
}
