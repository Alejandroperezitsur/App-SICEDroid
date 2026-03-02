package com.example.marsphotos.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val carrera: String = "",
    val semestre: Int = 0,
    val promedio: String = "",
    val estado: String = "",
    val statusMatricula: String = "",
    // Campos adicionales obtenidos desde la página HTML
    val fotoUrl: String = "",
    val especialidad: String = "",
    val cdtsReunidos: Int = 0,
    val cdtsActuales: Int = 0,
    val semActual: Int = 0,
    val inscrito: String = "",
    val estatusAcademico: String = "",
    val estatusAlumno: String = "",
    val reinscripcionFecha: String = "",
    val sinAdeudos: String = "",
    val lineamiento: Int = 0,
    val modEducativo: Int = 0,
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
