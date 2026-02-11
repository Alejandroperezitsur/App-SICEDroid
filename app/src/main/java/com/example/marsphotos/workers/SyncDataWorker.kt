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

class SyncDataWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val appContainer = (applicationContext as MarsPhotosApplication).container
        val snRepository = appContainer.snRepository
        val localRepository = appContainer.localRepository

        return try {
            val matricula = snRepository.getMatricula()
            if (matricula.isEmpty()) {
                Log.e("SyncWorker", "No matricula found in session")
                // If no session, we can't sync. 
                // However, if we are offline, we might want to skip sync and success?
                // But SyncWorker implies online sync.
                return Result.failure()
            }

            Log.d("SyncWorker", "Starting sync for $matricula")

            // 1. Fetch & Save Profile
            try {
                val profile = snRepository.profile(matricula)
                localRepository.insertStudent(
                    StudentEntity(
                        matricula = matricula,
                        nombre = profile.nombre,
                        apellidos = profile.apellidos,
                        carrera = profile.carrera,
                        semestre = profile.semestre,
                        promedio = profile.promedio,
                        fotoUrl = profile.fotoUrl,
                        operaciones = profile.operaciones
                    )
                )
                Log.d("SyncWorker", "Profile synced")
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing profile", e)
            }

            // 2. Fetch & Save Kardex
            try {
                val kardexList = snRepository.getKardex(matricula)
                if (kardexList.isNotEmpty()) {
                    val kardexEntities = kardexList.map { 
                        KardexEntity(
                            matricula = matricula,
                            clave = it.clave,
                            nombre = it.nombre,
                            calificacion = it.calificacion,
                            acreditacion = it.acreditacion,
                            periodo = it.periodo
                        )
                    }
                    localRepository.insertKardex(kardexEntities)
                    Log.d("SyncWorker", "Kardex synced: ${kardexEntities.size}")
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing kardex", e)
            }

            // 3. Fetch & Save Carga
            try {
                val cargaList = snRepository.getCarga(matricula)
                if (cargaList.isNotEmpty()) {
                    val cargaEntities = cargaList.map {
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
                            sabado = it.sabado
                        )
                    }
                    localRepository.insertCarga(cargaEntities)
                    Log.d("SyncWorker", "Carga synced: ${cargaEntities.size}")
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing carga", e)
            }

            // 4. Fetch & Save Parciales
            try {
                val parcialesList = snRepository.getCalifUnidades(matricula)
                if (parcialesList.isNotEmpty()) {
                    val entities = parcialesList.map {
                        CalifUnidadEntity(
                            matricula = matricula,
                            materia = it.materia,
                            parciales = it.parciales
                        )
                    }
                    localRepository.insertCalifUnidad(entities)
                    Log.d("SyncWorker", "Parciales synced: ${entities.size}")
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing parciales", e)
            }

            // 5. Fetch & Save Finales
            try {
                val finalesList = snRepository.getCalifFinal(matricula)
                if (finalesList.isNotEmpty()) {
                    val entities = finalesList.map {
                        CalifFinalEntity(
                            matricula = matricula,
                            materia = it.materia,
                            calif = it.calif
                        )
                    }
                    localRepository.insertCalifFinal(entities)
                    Log.d("SyncWorker", "Finales synced: ${entities.size}")
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing finales", e)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.retry()
        }
    }
}
