package com.example.marsphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "student_table")
data class StudentEntity(
    @PrimaryKey val matricula: String,
    val nombre: String,
    val apellidos: String,
    val carrera: String,
    val semestre: Int,
    val promedio: String,
    val estado: String = "",
    val statusMatricula: String = "",
    val fotoUrl: String,
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
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "kardex_table",
    primaryKeys = ["matricula", "clave", "periodo"]
)
data class KardexEntity(
    val matricula: String,
    val clave: String,
    val nombre: String,
    val calificacion: Int,
    val acreditacion: String,
    val periodo: String,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "carga_table",
    primaryKeys = ["matricula", "nombre", "grupo"]
)
data class CargaEntity(
    val matricula: String,
    val nombre: String,
    val docente: String,
    val grupo: String,
    val creditos: Int,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "calif_unidad_table",
    primaryKeys = ["matricula", "materia"]
)
data class CalifUnidadEntity(
    val matricula: String,
    val materia: String,
    val parciales: List<String>, // Requires Converter
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "calif_final_table",
    primaryKeys = ["matricula", "materia"]
)
data class CalifFinalEntity(
    val matricula: String,
    val materia: String,
    val calif: Int,
    val lastUpdate: Long = System.currentTimeMillis()
)

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
