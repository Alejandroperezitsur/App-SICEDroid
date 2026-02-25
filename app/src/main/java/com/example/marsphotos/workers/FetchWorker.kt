package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.marsphotos.MarsPhotosApplication
import com.google.gson.Gson

class FetchWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val feature = inputData.getString("feature") ?: return Result.failure()
        val appContainer = (applicationContext as MarsPhotosApplication).container
        val snRepository = appContainer.snRepository
        val gson = Gson()

        return try {
            val matricula = snRepository.getMatricula()
            if (matricula.isEmpty()) {
                Log.e("FetchWorker", "No matricula found")
                return Result.failure()
            }

            Log.d("FetchWorker", "Fetching $feature for $matricula")

            val resultJson: String = when (feature) {
                "PROFILE" -> gson.toJson(snRepository.profile(matricula))
                "KARDEX" -> gson.toJson(snRepository.getKardex(matricula))
                "CARGA" -> gson.toJson(snRepository.getCarga(matricula))
                "GRADES" -> {
                    val parciales = snRepository.getCalifUnidades(matricula)
                    val finales = snRepository.getCalifFinal(matricula)
                    gson.toJson(mapOf("parciales" to parciales, "finales" to finales))
                }
                else -> return Result.failure()
            }

            val outputData = Data.Builder()
                .putString("data_json", resultJson)
                .putString("feature", feature)
                .build()

            Result.success(outputData)
        } catch (e: Exception) {
            Log.e("FetchWorker", "Error fetching $feature", e)
            Result.retry()
        }
    }
}
