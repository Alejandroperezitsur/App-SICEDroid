package com.example.sicedroid.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semestre: Int = 0,
    val promedio: String = "",
    val estado: String = "",
    val statusMatricula: String = "",
    val cdtsReunidos: Int = 0,
    val cdtsActuales: Int = 0,
    val semActual: Int = 0,
    val inscrito: String = "",
    val reinscripcionFecha: String = "",
    val estatusAlumno: String = "",
    val estatusAcademico: String = "",
    val fotoUrl: String = "",
    val sinAdeudos: String = "",
    val lineamiento: Int = 1,
    val modEducativo: Int = 1,
    val operaciones: List<String> = emptyList()
)
