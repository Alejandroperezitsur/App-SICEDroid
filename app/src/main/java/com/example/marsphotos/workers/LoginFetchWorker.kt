package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.model.ProfileStudent
import com.google.gson.Gson
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import java.io.IOException

class LoginFetchWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val matricula = inputData.getString(KEY_MATRICULA) ?: return failure("missing_credentials", "Matrícula requerida")
        val contrasenia = inputData.getString(KEY_CONTRASENIA) ?: return failure("missing_credentials", "Contraseña requerida")

        val appContainer = (applicationContext as MarsPhotosApplication).container
        val snRepository: SNRepository = appContainer.snRepository
        val gson = Gson()

        return try {
            Log.d(TAG, "LoginFetchWorker started for $matricula")

            val success = try {
                snRepository.acceso(matricula, contrasenia)
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error during login: ${e.code()}", e)
                return failure("server_error", "Error del servidor (${e.code()})")
            } catch (e: IOException) {
                Log.e(TAG, "Network error during login", e)
                return failure("network_error", "Error de conexión")
            } catch (e: ConnectionShutdownException) {
                Log.e(TAG, "Connection shutdown during login", e)
                return failure("network_error", "Error de conexión")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during login", e)
                return failure("unexpected_error", "Error inesperado al autenticar")
            }

            if (!success) {
                Log.d(TAG, "Invalid credentials for $matricula")
                return failure("invalid_credentials", "Credenciales inválidas")
            }

            val profile = try {
                snRepository.profile(matricula)
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error fetching profile: ${e.code()}", e)
                return failure("server_error", "Error del servidor al obtener perfil (${e.code()})")
            } catch (e: IOException) {
                Log.e(TAG, "Network error fetching profile", e)
                return failure("network_error", "Error de conexión al obtener perfil")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching profile", e)
                return failure("unexpected_error", "Error inesperado al obtener perfil")
            }

            val profileJson = gson.toJson(profile, ProfileStudent::class.java)

            val output = Data.Builder()
                .putString(KEY_MATRICULA, matricula)
                .putString(KEY_PROFILE_JSON, profileJson)
                .build()

            Log.d(TAG, "LoginFetchWorker succeeded for $matricula")
            Result.success(output)
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in LoginFetchWorker", e)
            failure("unexpected_error", "Error inesperado en autenticación")
        }
    }

    private fun failure(code: String, message: String): Result {
        val output = Data.Builder()
            .putString(KEY_ERROR_CODE, code)
            .putString(KEY_ERROR_MESSAGE, message)
            .build()
        return Result.failure(output)
    }

    companion object {
        const val TAG = "LoginFetchWorker"
        const val KEY_MATRICULA = "login_matricula"
        const val KEY_CONTRASENIA = "login_contrasenia"
        const val KEY_PROFILE_JSON = "login_profile_json"
        const val KEY_ERROR_CODE = "login_error_code"
        const val KEY_ERROR_MESSAGE = "login_error_message"
    }
}

