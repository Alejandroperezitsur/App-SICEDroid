package com.example.marsphotos.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.workers.SyncDataWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state para la pantalla de Login
 */
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val matricula: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

/**
 * ViewModel para la pantalla de login
 */
class LoginViewModel(
    private val snRepository: SNRepository,
    private val application: MarsPhotosApplication
) : ViewModel() {

    private val workManager = WorkManager.getInstance(application)

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    var matricula: String by mutableStateOf("")
        private set

    var contrasenia: String by mutableStateOf("")
        private set

    private fun isOnline(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun updateMatricula(newValue: String) {
        matricula = newValue
    }

    fun updateContrasenia(newValue: String) {
        contrasenia = newValue
    }

    fun login() {
        if (matricula.isBlank() || contrasenia.isBlank()) {
            loginUiState = LoginUiState.Error("Por favor ingresa matrícula y contraseña")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            loginUiState = LoginUiState.Loading
            loginUiState = try {
                val success = snRepository.acceso(matricula, contrasenia)
                if (success) {
                    if (isOnline()) {
                        val syncRequest = OneTimeWorkRequestBuilder<SyncDataWorker>().build()
                        workManager.beginUniqueWork(
                            "Sync_FULL",
                            ExistingWorkPolicy.REPLACE,
                            syncRequest
                        ).enqueue()
                    }
                    LoginUiState.Success(matricula)
                } else {
                    LoginUiState.Error("Credenciales inválidas")
                }
            } catch (e: IOException) {
                LoginUiState.Error("Error de conexión: ${e.message}")
            } catch (e: HttpException) {
                LoginUiState.Error("Error del servidor: ${e.message}")
            } catch (e: Exception) {
                LoginUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        loginUiState = LoginUiState.Idle
    }
    
    fun resetForm() {
        loginUiState = LoginUiState.Idle
        matricula = ""
        contrasenia = ""
    }
    
    /**
     * Factory para crear instancias de LoginViewModel
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val snRepository = application.container.snRepository
                LoginViewModel(
                    snRepository = snRepository,
                    application = application
                )
            }
        }
    }
}
