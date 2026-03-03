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
import com.example.marsphotos.notifications.NotificationHelper
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

        // Helper para notificaciones
        val notificationHelper = NotificationHelper(applicationContext)

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

                    // Obtener calificaciones anteriores para comparar
                    val oldParciales = localRepository.getCalifUnidadSync(matricula)
                    val oldFinales = localRepository.getCalifFinalSync(matricula)

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

                    // Verificar nuevas calificaciones y enviar notificaciones
                    checkForNewGrades(
                        oldParciales = oldParciales,
                        newParciales = parciales,
                        oldFinales = oldFinales,
                        newFinales = finales,
                        notificationHelper = notificationHelper
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("StoreWorker", "Error storing $feature", e)
            Result.failure()
        }
    }

    /**
     * Compara calificaciones anteriores con nuevas para detectar cambios
     */
    private fun checkForNewGrades(
        oldParciales: List<CalifUnidadEntity>,
        newParciales: List<MateriaParcial>,
        oldFinales: List<CalifFinalEntity>,
        newFinales: List<MateriaFinal>,
        notificationHelper: NotificationHelper
    ) {
        val newGradesCount = mutableListOf<String>()

        // Verificar parciales nuevos
        newParciales.forEach { newMateria ->
            val oldMateria = oldParciales.find { it.materia == newMateria.materia }
            
            newMateria.parciales.forEachIndexed { index, newGrade ->
                val oldGrade = oldMateria?.parciales?.getOrNull(index)
                
                // Si hay una nueva calificación (antes era 0 o vacío, ahora tiene valor)
                if (hasNewGrade(oldGrade, newGrade)) {
                    Log.d("StoreWorker", "Nueva calificación parcial detectada: ${newMateria.materia} - U${index + 1}: $newGrade")
                    notificationHelper.showNewGradeNotification(
                        materia = newMateria.materia,
                        calificacion = newGrade,
                        tipo = "Unidad ${index + 1}"
                    )
                    newGradesCount.add("${newMateria.materia} - U${index + 1}")
                }
            }
        }

        // Verificar finales nuevos
        newFinales.forEach { newFinal ->
            val oldFinal = oldFinales.find { it.materia == newFinal.materia }
            
            // Si hay una nueva calificación final
            val oldCalifStr = oldFinal?.calif?.toString()
            val newCalifStr = newFinal.calif.toString()
            if (hasNewGrade(oldCalifStr, newCalifStr)) {
                Log.d("StoreWorker", "Nueva calificación final detectada: ${newFinal.materia}: ${newFinal.calif}")
                notificationHelper.showNewGradeNotification(
                    materia = newFinal.materia,
                    calificacion = newFinal.calif.toString(),
                    tipo = "Final"
                )
                newGradesCount.add("${newFinal.materia} - Final")
            }
        }

        // Si hubo múltiples cambios, mostrar resumen
        if (newGradesCount.size > 1) {
            notificationHelper.showGradesUpdatedNotification(newGradesCount.size)
        }

        Log.d("StoreWorker", "Total nuevas calificaciones detectadas: ${newGradesCount.size}")
    }

    /**
     * Determina si hay una nueva calificación comparando valores
     */
    private fun hasNewGrade(oldGrade: String?, newGrade: String): Boolean {
        // Si no había calificación antes y ahora sí
        if (oldGrade.isNullOrEmpty() || oldGrade == "0" || oldGrade == "-") {
            return newGrade.isNotEmpty() && newGrade != "0" && newGrade != "-"
        }
        return false
    }
}
