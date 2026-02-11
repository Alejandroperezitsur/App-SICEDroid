package com.example.marsphotos.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val carrera: String = "",
    val semestre: String = "",
    val promedio: String = "",
    val estado: String = "",
    val statusMatricula: String = "",
    // Campos adicionales obtenidos desde la página HTML
    val fotoUrl: String = "",
    val especialidad: String = "",
    val cdtsReunidos: String = "",
    val cdtsActuales: String = "",
    val semActual: String = "",
    val inscrito: String = "",
    val estatusAcademico: String = "",
    val estatusAlumno: String = "",
    val reinscripcionFecha: String = "",
    val sinAdeudos: String = "",
    val operaciones: List<String> = emptyList(),
    // Detalles de operaciones
    val kardex: List<MateriaKardex> = emptyList(),
    val cargaAcademica: List<MateriaCarga> = emptyList(),
    val calificacionesParciales: List<MateriaParcial> = emptyList(),
    // Debug info
    val kardexTitle: String = "",
    val cargaTitle: String = "",
    val califTitle: String = "",
    val kardexHtml: String = "",
    val cargaHtml: String = "",
    val califHtml: String = ""
)
// (El parsing del DataSet XML se encuentra en ResponseAcceso.kt)
