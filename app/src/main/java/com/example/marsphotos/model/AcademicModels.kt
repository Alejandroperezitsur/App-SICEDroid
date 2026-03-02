package com.example.marsphotos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MateriaKardex(
    @SerialName("ClvOfiMat") val clave: String = "",
    @SerialName("Materia") val nombre: String = "",
    @SerialName("Calif") val calificacion: Int = 0,
    @SerialName("Acred") val acreditacion: String = "",
    @SerialName("P1") val periodo: String = ""
)

@Serializable
data class KardexResponse(
    val lstKardex: List<MateriaKardex> = emptyList(),
    val Promedio: PromedioKardex? = null
)

@Serializable
data class PromedioKardex(
    val PromedioGral: Double = 0.0,
    val CdtsAcum: Int = 0,
    val CdtsPlan: Int = 0
)

@Serializable
data class MateriaCarga(
    @SerialName("Materia") val nombre: String = "",
    @SerialName("Docente") val docente: String = "",
    @SerialName("Grupo") val grupo: String = "",
    @SerialName("CreditosMateria") val creditos: Int = 0,
    @SerialName("Lunes") val lunes: String = "",
    @SerialName("Martes") val martes: String = "",
    @SerialName("Miercoles") val miercoles: String = "",
    @SerialName("Jueves") val jueves: String = "",
    @SerialName("Viernes") val viernes: String = "",
    @SerialName("Sabado") val sabado: String = ""
)

@Serializable
data class MateriaParcial(
    val materia: String = "",
    val parciales: List<String> = emptyList()
)

@Serializable
data class MateriaFinal(
    val materia: String = "",
    val calif: Int = 0,
    @SerialName("acred") val acreditacion: String = "",
    @SerialName("grupo") val grupo: String = ""
)

@Serializable
data class GradesData(
    val parciales: List<MateriaParcial>,
    val finales: List<MateriaFinal>
)
