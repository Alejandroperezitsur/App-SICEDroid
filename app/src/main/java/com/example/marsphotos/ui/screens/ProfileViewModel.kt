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
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.local.LocalRepository
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.workers.FetchWorker
import com.example.marsphotos.workers.StoreWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state para la pantalla de Perfil
 */
sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: ProfileStudent, val lastUpdate: Long = 0, val isOffline: Boolean = false) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

/**
 * ViewModel para mostrar el perfil académico del estudiante
 * Optimizado para funcionar SIN INTERNET usando datos locales
 */
class ProfileViewModel(
    private val snRepository: SNRepository,
    private val localRepository: LocalRepository,
    private val application: MarsPhotosApplication
) : ViewModel() {
    
    private val workManager = WorkManager.getInstance(application)

    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set
    
    private fun isOnline(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun scheduleSync(matricula: String) {
        val workName = "Sync_PROFILE"

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchData = Data.Builder()
            .putString("feature", "PROFILE")
            .build()

        val fetchRequest = OneTimeWorkRequestBuilder<FetchWorker>()
            .setInputData(fetchData)
            .setConstraints(constraints)
            .build()

        val storeRequest = OneTimeWorkRequestBuilder<StoreWorker>()
            .build()

        workManager.beginUniqueWork(workName, ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(storeRequest)
            .enqueue()

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(fetchRequest.id).collect { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            loadProfileFromLocal(matricula, isSync = true)
                        }
                        WorkInfo.State.FAILED -> {
                            android.util.Log.e("ProfileVM", "Sync failed for PROFILE")
                            // Solo mostrar error si no tenemos datos locales
                            if (profileUiState !is ProfileUiState.Success) {
                                profileUiState = ProfileUiState.Error("Error al sincronizar perfil")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    /**
     * Carga el perfil académico del estudiante
     * SIEMPRE intenta cargar desde local primero para funcionar sin internet
     */
    fun loadProfile(matricula: String) {
        viewModelScope.launch {
            profileUiState = ProfileUiState.Loading
            
            // Cargar desde local PRIMERO
            val hasLocalData = loadProfileFromLocal(matricula)

            // Si hay internet, sincronizar en segundo plano (no bloquea la UI)
            if (isOnline() && matricula.isNotEmpty()) {
                scheduleSync(matricula)
            } else if (!hasLocalData) {
                // No hay internet y no hay datos locales guardados
                profileUiState = ProfileUiState.Error(
                    "Sin conexión a internet y no hay datos guardados. " +
                    "Por favor conéctate a internet e inicia sesión para descargar tus datos."
                )
            }
            // Si hay datos locales, se mantienen mostrados (aunque sea offline)
        }
    }

    /**
     * Carga el perfil desde la base de datos local (Room)
     * Retorna true si encontró datos, false si no había nada
     */
    private suspend fun loadProfileFromLocal(matricula: String, isSync: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val localStudent = localRepository.getStudentSync(matricula)
                if (localStudent != null) {
                    val profile = ProfileStudent(
                        matricula = localStudent.matricula,
                        nombre = localStudent.nombre,
                        apellidos = localStudent.apellidos,
                        carrera = localStudent.carrera,
                        semestre = localStudent.semestre,
                        promedio = localStudent.promedio,
                        estado = localStudent.estado,
                        statusMatricula = localStudent.statusMatricula,
                        fotoUrl = localStudent.fotoUrl,
                        especialidad = localStudent.especialidad,
                        cdtsReunidos = localStudent.cdtsReunidos,
                        cdtsActuales = localStudent.cdtsActuales,
                        semActual = localStudent.semActual,
                        inscrito = localStudent.inscrito,
                        estatusAcademico = localStudent.estatusAcademico,
                        estatusAlumno = localStudent.estatusAlumno,
                        reinscripcionFecha = localStudent.reinscripcionFecha,
                        sinAdeudos = localStudent.sinAdeudos,
                        lineamiento = localStudent.lineamiento,
                        modEducativo = localStudent.modEducativo,
                        operaciones = localStudent.operaciones
                    )
                    val isOffline = !isOnline()
                    profileUiState = ProfileUiState.Success(profile, localStudent.lastUpdate, isOffline)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error cargando perfil local: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Factory para crear instancias de ProfileViewModel
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val snRepository = application.container.snRepository
                val localRepository = application.container.localRepository
                ProfileViewModel(
                    snRepository = snRepository, 
                    localRepository = localRepository,
                    application = application
                )
            }
        }
    }
}
