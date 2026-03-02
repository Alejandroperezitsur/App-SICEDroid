package com.example.marsphotos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
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

// Modelo alternativo para parsear respuesta del SICEnet que podría venir con campos individuales
// Los campos deben coincidir EXACTAMENTE con los nombres del JSON del servidor
@Serializable
data class MateriaParcialRaw(
    val Materia: String = "",  // El servidor usa "Materia" con M mayúscula
    val Grupo: String = "",    // El servidor usa "Grupo" con G mayúscula
    // Campos C1-C16 para calificaciones (el servidor usa C1, C2, etc. mayúsculas)
    val C1: String? = null,
    val C2: String? = null,
    val C3: String? = null,
    val C4: String? = null,
    val C5: String? = null,
    val C6: String? = null,
    val C7: String? = null,
    val C8: String? = null,
    val C9: String? = null,
    val C10: String? = null,
    val C11: String? = null,
    val C12: String? = null,
    val C13: String? = null,
    val C14: String? = null,
    val C15: String? = null,
    val C16: String? = null,
    // Lista directa si viene como array
    val parciales: List<String>? = null
) {
    fun toMateriaParcial(): MateriaParcial {
        // Si ya viene como lista, usarla directamente
        if (!parciales.isNullOrEmpty()) {
            return MateriaParcial(Materia, parciales)
        }
        
        // Si viene como campos individuales, convertirlos a lista
        val lista = mutableListOf<String>()
        val campos = listOf(C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16)
        
        for (campo in campos) {
            campo?.let { 
                // Incluir incluso si es "0" para mantener la posición, pero no si está vacío o null
                lista.add(it)
            }
        }
        
        return MateriaParcial(Materia, lista)
    }
}

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
