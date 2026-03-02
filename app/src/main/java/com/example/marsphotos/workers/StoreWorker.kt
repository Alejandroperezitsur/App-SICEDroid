package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.local.CalifFinalEntity
import com.example.marsphotos.data.local.CalifUnidadEntity
import com.example.marsphotos.data.local.CargaEntity
import com.example.marsphotos.data.local.KardexEntity
import com.example.marsphotos.data.local.StudentEntity
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaParcial
import com.example.marsphotos.model.ProfileStudent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StoreWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val dataJson = inputData.getString("data_json") ?: return Result.failure()
        val feature = inputData.getString("feature") ?: return Result.failure()
        val appContainer = (applicationContext as MarsPhotosApplication).container
        val localRepository = appContainer.localRepository
        val snRepository = appContainer.snRepository
        val gson = Gson()
        val matricula = snRepository.getMatricula()

        if (matricula.isEmpty()) return Result.failure()

        return try {
            Log.d("StoreWorker", "Storing $feature for $matricula")
            val now = System.currentTimeMillis()

            when (feature) {
                "PROFILE" -> {
                    val profile = gson.fromJson(dataJson, ProfileStudent::class.java)
                    localRepository.insertStudent(
                        StudentEntity(
                            matricula = matricula,
                            nombre = profile.nombre,
                            apellidos = profile.apellidos,
                            carrera = profile.carrera,
                            semestre = profile.semestre,
                            promedio = profile.promedio,
                            estado = profile.estado,
                            statusMatricula = profile.statusMatricula,
                            fotoUrl = profile.fotoUrl,
                            especialidad = profile.especialidad,
                            cdtsReunidos = profile.cdtsReunidos,
                            cdtsActuales = profile.cdtsActuales,
                            semActual = profile.semActual,
                            inscrito = profile.inscrito,
                            estatusAcademico = profile.estatusAcademico,
                            estatusAlumno = profile.estatusAlumno,
                            reinscripcionFecha = profile.reinscripcionFecha,
                            sinAdeudos = profile.sinAdeudos,
                            lineamiento = profile.lineamiento,
                            modEducativo = profile.modEducativo,
                            operaciones = profile.operaciones,
                            lastUpdate = now
                        )
                    )
                }
                "KARDEX" -> {
                    val listType = object : TypeToken<List<MateriaKardex>>() {}.type
                    val list = gson.fromJson<List<MateriaKardex>>(dataJson, listType)
                    localRepository.updateKardex(matricula, list.map {
                        KardexEntity(
                            matricula = matricula,
                            clave = it.clave,
                            nombre = it.nombre,
                            calificacion = it.calificacion,
                            acreditacion = it.acreditacion,
                            periodo = it.periodo,
                            lastUpdate = now
                        )
                    })
                }
                "CARGA" -> {
                    val listType = object : TypeToken<List<MateriaCarga>>() {}.type
                    val list = gson.fromJson<List<MateriaCarga>>(dataJson, listType)
                    localRepository.updateCarga(matricula, list.map {
                        CargaEntity(
                            matricula = matricula,
                            nombre = it.nombre,
                            docente = it.docente,
                            grupo = it.grupo,
                            creditos = it.creditos,
                            lunes = it.lunes,
                            martes = it.martes,
                            miercoles = it.miercoles,
                            jueves = it.jueves,
                            viernes = it.viernes,
                            sabado = it.sabado,
                            lastUpdate = now
                        )
                    })
                }
                "GRADES" -> {
                    Log.e("StoreWorker", "=== GUARDANDO GRADES ===")
                    Log.e("StoreWorker", "dataJson: ${dataJson.take(500)}...")
                    
                    val gradesData = gson.fromJson(dataJson, com.example.marsphotos.model.GradesData::class.java)
                    
                    val parciales = gradesData.parciales
                    val finales = gradesData.finales
                    
                    Log.e("StoreWorker", "Parciales recibidos: ${parciales.size}")
                    parciales.forEachIndexed { index, materia ->
                        Log.d("StoreWorker", "[$index] ${materia.materia}: ${materia.parciales}")
                    }
                    Log.e("StoreWorker", "Finales recibidos: ${finales.size}")

                    localRepository.updateCalifUnidad(matricula, parciales.map {
                        CalifUnidadEntity(
                            matricula = matricula,
                            materia = it.materia,
                            parciales = it.parciales,
                            lastUpdate = now
                        )
                    })
                    Log.e("StoreWorker", "✅ updateCalifUnidad completado")
                    
                    localRepository.updateCalifFinal(matricula, finales.map {
                        CalifFinalEntity(
                            matricula = matricula,
                            materia = it.materia,
                            calif = it.calif,
                            lastUpdate = now
                        )
                    })
                    Log.e("StoreWorker", "✅ updateCalifFinal completado")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("StoreWorker", "Error storing $feature", e)
            Result.failure()
        }
    }
}
