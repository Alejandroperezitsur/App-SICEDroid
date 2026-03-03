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
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.data.SessionManager
import com.example.marsphotos.workers.LoginFetchWorker
import com.example.marsphotos.workers.LoginStoreWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private val sessionManager: SessionManager,
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
            val message = application.getString(com.example.marsphotos.R.string.error_empty_credentials)
            loginUiState = LoginUiState.Error(message)
            return
        }

        if (!isOnline()) {
            val message = application.getString(com.example.marsphotos.R.string.error_network)
            loginUiState = LoginUiState.Error(message)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            loginUiState = LoginUiState.Loading

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData: Data = workDataOf(
                LoginFetchWorker.KEY_MATRICULA to matricula,
                LoginFetchWorker.KEY_CONTRASENIA to contrasenia
            )

            val fetchRequest = OneTimeWorkRequestBuilder<LoginFetchWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            val storeRequest = OneTimeWorkRequestBuilder<LoginStoreWorker>()
                .build()

            workManager.beginUniqueWork(
                "LoginChain",
                ExistingWorkPolicy.REPLACE,
                fetchRequest
            ).then(storeRequest).enqueue()

            workManager.getWorkInfoByIdFlow(fetchRequest.id).collect { workInfo ->
                // Verificar que workInfo no sea null
                if (workInfo == null) {
                    loginUiState = LoginUiState.Loading
                    return@collect
                }
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
                        loginUiState = LoginUiState.Loading
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        // ✅ Guardar sesión en SessionManager para persistencia
                        sessionManager.saveSession(matricula, contrasenia)
                        loginUiState = LoginUiState.Success(matricula)
                    }
                    WorkInfo.State.FAILED -> {
                        val errorCode = workInfo.outputData.getString(LoginFetchWorker.KEY_ERROR_CODE) ?: "unexpected_error"
                        val errorMessage = workInfo.outputData.getString(LoginFetchWorker.KEY_ERROR_MESSAGE)
                        val message = when (errorCode) {
                            "invalid_credentials" -> application.getString(com.example.marsphotos.R.string.error_invalid_credentials)
                            "network_error" -> application.getString(com.example.marsphotos.R.string.error_network)
                            "server_error" -> application.getString(com.example.marsphotos.R.string.error_server)
                            "missing_credentials" -> application.getString(com.example.marsphotos.R.string.error_empty_credentials)
                            else -> errorMessage ?: application.getString(com.example.marsphotos.R.string.error_unexpected)
                        }
                        loginUiState = LoginUiState.Error(message)
                    }
                    WorkInfo.State.CANCELLED -> {
                        val message = application.getString(com.example.marsphotos.R.string.error_login_cancelled)
                        loginUiState = LoginUiState.Error(message)
                    }
                    else -> {}
                }
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
                val sessionManager = application.container.sessionManager
                LoginViewModel(
                    snRepository = snRepository,
                    sessionManager = sessionManager,
                    application = application
                )
            }
        }
    }
}
