package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.local.LocalRepository
import com.example.marsphotos.data.local.StudentEntity
import com.example.marsphotos.model.ProfileStudent
import com.google.gson.Gson

class LoginStoreWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val profileJson = inputData.getString(LoginFetchWorker.KEY_PROFILE_JSON) ?: return Result.failure()
        val matricula = inputData.getString(LoginFetchWorker.KEY_MATRICULA) ?: return Result.failure()

        val appContainer = (applicationContext as MarsPhotosApplication).container
        val localRepository: LocalRepository = appContainer.localRepository
        val gson = Gson()

        return try {
            val profile = gson.fromJson(profileJson, ProfileStudent::class.java)
            val now = System.currentTimeMillis()

            val entity = StudentEntity(
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

            localRepository.insertStudent(entity)
            Log.d(TAG, "LoginStoreWorker stored profile for $matricula")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing profile in LoginStoreWorker", e)
            Result.failure()
        }
    }

    companion object {
        const val TAG = "LoginStoreWorker"
    }
}
