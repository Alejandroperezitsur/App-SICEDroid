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
    val semestre: String,
    val promedio: String,
    val fotoUrl: String,
    val operaciones: List<String> = emptyList(),
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "kardex_table")
data class KardexEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricula: String,
    val clave: String,
    val nombre: String,
    val calificacion: String,
    val acreditacion: String,
    val periodo: String,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "carga_table")
data class CargaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricula: String,
    val nombre: String,
    val docente: String,
    val grupo: String,
    val creditos: String,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "calif_unidad_table")
data class CalifUnidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricula: String,
    val materia: String,
    val parciales: List<String>, // Requires Converter
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "calif_final_table")
data class CalifFinalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matricula: String,
    val materia: String,
    val calif: String,
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
