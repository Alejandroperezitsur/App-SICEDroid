package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.local.LocalRepository
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.model.ProfileStudent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state para la pantalla de Perfil
 */
sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: ProfileStudent) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

/**
 * ViewModel para mostrar el perfil académico del estudiante
 */
class ProfileViewModel(
    private val snRepository: SNRepository,
    private val localRepository: LocalRepository
) : ViewModel() {
    
    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set
    
    /**
     * Carga el perfil académico del estudiante
     */
    fun loadProfile(matricula: String) {
        viewModelScope.launch {
            android.util.Log.d("ProfileVM", "Iniciando carga de perfil para $matricula")
            profileUiState = ProfileUiState.Loading
            
            // 1. Try Network
            var networkProfile: ProfileStudent? = null
            try {
                networkProfile = withContext(Dispatchers.IO) {
                    android.util.Log.d("ProfileVM", "Llamando a snRepository.profile...")
                    val result = snRepository.profile(matricula)
                    android.util.Log.d("ProfileVM", "snRepository.profile retornó: $result")
                    result
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error de red al cargar perfil", e)
            }

            if (networkProfile != null && networkProfile.matricula.isNotEmpty()) {
                profileUiState = ProfileUiState.Success(networkProfile)
                return@launch
            }

            // 2. Try Local
            try {
                val localStudent = withContext(Dispatchers.IO) {
                    localRepository.getStudentSync(matricula)
                }

                if (localStudent != null) {
                    val profile = ProfileStudent(
                        matricula = localStudent.matricula,
                        nombre = localStudent.nombre,
                        apellidos = localStudent.apellidos,
                        carrera = localStudent.carrera,
                        semestre = localStudent.semestre,
                        promedio = localStudent.promedio,
                        fotoUrl = localStudent.fotoUrl,
                        operaciones = localStudent.operaciones
                    )
                    profileUiState = ProfileUiState.Success(profile)
                } else {
                    profileUiState = ProfileUiState.Error("No se pudo cargar el perfil (ni local ni red)")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error local al cargar perfil", e)
                profileUiState = ProfileUiState.Error("Error: ${e.message}")
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
                ProfileViewModel(snRepository = snRepository, localRepository = localRepository)
            }
        }
    }
}
