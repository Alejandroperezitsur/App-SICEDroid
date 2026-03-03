package com.example.marsphotos.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaParcial
import com.example.marsphotos.workers.FetchWorker
import com.example.marsphotos.workers.StoreWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AcademicUiState<out T> {
    object Loading : AcademicUiState<Nothing>
    data class Success<T>(val data: T, val lastUpdate: Long = 0, val isOffline: Boolean = false) : AcademicUiState<T>
    data class Error(val message: String) : AcademicUiState<Nothing>
}

class AcademicViewModel(
    private val snRepository: SNRepository,
    private val localRepository: LocalRepository,
    private val application: MarsPhotosApplication
) : ViewModel() {

    private val workManager = WorkManager.getInstance(application)

    private val _kardexState = MutableStateFlow<AcademicUiState<List<MateriaKardex>>>(AcademicUiState.Loading)
    val kardexState: StateFlow<AcademicUiState<List<MateriaKardex>>> = _kardexState.asStateFlow()

    private val _cargaState = MutableStateFlow<AcademicUiState<List<MateriaCarga>>>(AcademicUiState.Loading)
    val cargaState: StateFlow<AcademicUiState<List<MateriaCarga>>> = _cargaState.asStateFlow()

    private val _parcialesState = MutableStateFlow<AcademicUiState<List<MateriaParcial>>>(AcademicUiState.Loading)
    val parcialesState: StateFlow<AcademicUiState<List<MateriaParcial>>> = _parcialesState.asStateFlow()

    private val _finalesState = MutableStateFlow<AcademicUiState<List<MateriaFinal>>>(AcademicUiState.Loading)
    val finalesState: StateFlow<AcademicUiState<List<MateriaFinal>>> = _finalesState.asStateFlow()

    private fun isOnline(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun scheduleSync(feature: String, onWorkerFinished: () -> Unit, onWorkerFailed: (String) -> Unit) {
        val workName = "Sync_${feature}"
        val finalWorkerTag = "TAG_FINAL_${feature}"

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchData = Data.Builder()
            .putString("feature", feature)
            .build()

        val fetchRequest = OneTimeWorkRequestBuilder<FetchWorker>()
            .setInputData(fetchData)
            .setConstraints(constraints)
            .build()

        val storeRequest = OneTimeWorkRequestBuilder<StoreWorker>()
            .addTag(finalWorkerTag)
            .build()

        workManager.beginUniqueWork(workName, ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(storeRequest)
            .enqueue()

        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(workName).collect { workInfos ->
                val finalWorker = workInfos.find { it.tags.contains(finalWorkerTag) }
                
                if (finalWorker != null) {
                    when (finalWorker.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            android.util.Log.d("AcademicVM", "Chain succeeded: $feature")
                            onWorkerFinished()
                        }
                        WorkInfo.State.FAILED -> {
                            android.util.Log.e("AcademicVM", "Chain failed at StoreWorker: $feature")
                            onWorkerFailed("Error al guardar datos de $feature")
                        }
                        WorkInfo.State.CANCELLED -> {
                            android.util.Log.e("AcademicVM", "Chain cancelled: $feature")
                            onWorkerFailed("Sincronización cancelada")
                        }
                        WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                            // En proceso, mantenemos estado actual
                        }
                    }
                }
            }
        }
    }

    fun loadKardex(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _kardexState.value
            
            // Si ya tenemos datos y no se fuerza refresh, no recargar
            if (!forceRefresh && currentState is AcademicUiState.Success) return@launch
            
            _kardexState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            
            android.util.Log.d("AcademicVM", "loadKardex() - matrícula obtenida: '$matricula'")
            android.util.Log.d("AcademicVM", "isOnline(): ${isOnline()}")
            
            if (matricula.isEmpty()) {
                android.util.Log.e("AcademicVM", "Matrícula vacía - no se puede cargar kardex")
                _kardexState.value = AcademicUiState.Error("Matrícula no encontrada. Inicia sesión nuevamente.")
                return@launch
            }
            
            // Cargar desde local PRIMERO
            val hasLocalData = loadKardexFromLocal(matricula)
            
            // Si hay internet, sincronizar en segundo plano
            if (isOnline()) {
                scheduleSync(
                    feature = "KARDEX", 
                    onWorkerFinished = {
                        viewModelScope.launch { loadKardexFromLocal(matricula, isSync = true) }
                    },
                    onWorkerFailed = { msg ->
                        // Solo mostrar error si no tenemos datos locales
                        if (_kardexState.value !is AcademicUiState.Success) {
                            _kardexState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            } else if (!hasLocalData) {
                // No hay internet y no hay datos locales
                _kardexState.value = AcademicUiState.Error("Sin conexión y sin datos guardados. Conéctate a internet para cargar tus datos.")
            }
        }
    }

    private suspend fun loadKardexFromLocal(matricula: String, isSync: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val localKardex = localRepository.getKardexSync(matricula)
                val list = localKardex.map { entity ->
                    MateriaKardex(
                        clave = entity.clave,
                        nombre = entity.nombre,
                        calificacion = entity.calificacion,
                        acreditacion = entity.acreditacion,
                        periodo = entity.periodo
                    )
                }
                
                if (list.isNotEmpty()) {
                    val lastUpdate = localKardex.firstOrNull()?.lastUpdate ?: 0
                    val isOffline = !isOnline()
                    _kardexState.value = AcademicUiState.Success(list, lastUpdate, isOffline)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Error cargando kardex local: ${e.message}")
                false
            }
        }
    }

    fun loadCarga(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _cargaState.value
            if (!forceRefresh && currentState is AcademicUiState.Success) return@launch

            _cargaState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            
            if (matricula.isEmpty()) {
                _cargaState.value = AcademicUiState.Error("Matrícula no encontrada")
                return@launch
            }
            
            // Cargar desde local PRIMERO
            val hasLocalData = loadCargaFromLocal(matricula)
            
            // Si hay internet, sincronizar en segundo plano
            if (isOnline()) {
                scheduleSync(
                    feature = "CARGA",
                    onWorkerFinished = {
                        viewModelScope.launch { loadCargaFromLocal(matricula, isSync = true) }
                    },
                    onWorkerFailed = { msg ->
                        if (_cargaState.value !is AcademicUiState.Success) {
                            _cargaState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            } else if (!hasLocalData) {
                _cargaState.value = AcademicUiState.Error("Sin conexión y sin datos guardados. Conéctate a internet para cargar tus datos.")
            }
        }
    }

    private suspend fun loadCargaFromLocal(matricula: String, isSync: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val localCarga = localRepository.getCargaSync(matricula)
                val list = localCarga.map { entity ->
                    MateriaCarga(
                        nombre = entity.nombre,
                        docente = entity.docente,
                        grupo = entity.grupo,
                        creditos = entity.creditos,
                        lunes = entity.lunes,
                        martes = entity.martes,
                        miercoles = entity.miercoles,
                        jueves = entity.jueves,
                        viernes = entity.viernes,
                        sabado = entity.sabado
                    )
                }
                
                if (list.isNotEmpty()) {
                    val lastUpdate = localCarga.firstOrNull()?.lastUpdate ?: 0
                    val isOffline = !isOnline()
                    _cargaState.value = AcademicUiState.Success(list, lastUpdate, isOffline)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Error cargando carga local: ${e.message}")
                false
            }
        }
    }

    fun loadGrades(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentPState = _parcialesState.value
            val currentFState = _finalesState.value
            if (!forceRefresh && (currentPState is AcademicUiState.Success || currentFState is AcademicUiState.Success)) return@launch

            _parcialesState.value = AcademicUiState.Loading
            _finalesState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            
            if (matricula.isEmpty()) {
                _parcialesState.value = AcademicUiState.Error("Matrícula no encontrada")
                _finalesState.value = AcademicUiState.Error("Matrícula no encontrada")
                return@launch
            }
            
            // Cargar desde local PRIMERO
            val hasLocalData = loadGradesFromLocal(matricula)
            
            // Si hay internet, sincronizar en segundo plano
            if (isOnline()) {
                scheduleSync(
                    feature = "GRADES",
                    onWorkerFinished = {
                        viewModelScope.launch { loadGradesFromLocal(matricula, isSync = true) }
                    },
                    onWorkerFailed = { msg ->
                        if (_parcialesState.value !is AcademicUiState.Success) {
                            _parcialesState.value = AcademicUiState.Error(msg)
                        }
                        if (_finalesState.value !is AcademicUiState.Success) {
                            _finalesState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            } else if (!hasLocalData) {
                val err = AcademicUiState.Error("Sin conexión y sin datos guardados. Conéctate a internet para cargar tus datos.")
                _parcialesState.value = err
                _finalesState.value = err
            }
        }
    }

    private suspend fun loadGradesFromLocal(matricula: String, isSync: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val localParciales = localRepository.getCalifUnidadSync(matricula)
                val localFinales = localRepository.getCalifFinalSync(matricula)

                if (localParciales.isNotEmpty() || localFinales.isNotEmpty()) {
                    val pList = localParciales.map { entity ->
                        MateriaParcial(materia = entity.materia, parciales = entity.parciales)
                    }
                    val fList = localFinales.map { entity ->
                        MateriaFinal(materia = entity.materia, calif = entity.calif)
                    }
                    
                    val lastUpdate = localParciales.firstOrNull()?.lastUpdate ?: localFinales.firstOrNull()?.lastUpdate ?: 0
                    val isOffline = !isOnline()
                    _parcialesState.value = AcademicUiState.Success(pList, lastUpdate, isOffline)
                    _finalesState.value = AcademicUiState.Success(fList, lastUpdate, isOffline)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Error cargando grades local: ${e.message}")
                false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val snRepository = application.container.snRepository
                val localRepository = application.container.localRepository
                AcademicViewModel(
                    snRepository = snRepository, 
                    localRepository = localRepository,
                    application = application
                )
            }
        }
    }
}
