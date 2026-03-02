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
    data class Success<T>(val data: T, val lastUpdate: Long = 0) : AcademicUiState<T>
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
                // Determinismo absoluto: Solo nos importa el estado del StoreWorker (el último del chain)
                // de la ejecución actual.
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
                            // En proceso o esperando reintento. Mantenemos estado actual.
                        }
                    }
                }
            }
        }
    }

    fun loadKardex(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _kardexState.value
            val isAlreadySyncing = isSyncing("KARDEX")

            // Evitar re-sincronización si ya hay una en curso o si ya hay datos y no se fuerza refresh
            if (!forceRefresh && (currentState is AcademicUiState.Success || isAlreadySyncing)) {
                if (currentState is AcademicUiState.Loading) {
                    // Si estamos en Loading pero hay sync en curso, simplemente esperamos a que termine el collector
                    return@launch
                }
                if (currentState is AcademicUiState.Success) {
                    return@launch
                }
            }

            _kardexState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            
            // First load from local
            loadKardexFromLocal(matricula, isInitialLoad = true)

            // If online, trigger sync
            if (isOnline() && matricula.isNotEmpty()) {
                scheduleSync(
                    feature = "KARDEX", 
                    onWorkerFinished = {
                        viewModelScope.launch { loadKardexFromLocal(matricula, isInitialLoad = false) }
                    },
                    onWorkerFailed = { msg ->
                        if (_kardexState.value is AcademicUiState.Loading) {
                            _kardexState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            } else if (matricula.isEmpty()) {
                _kardexState.value = AcademicUiState.Error("Matrícula no encontrada")
            }
        }
    }

    private fun isSyncing(feature: String): Boolean {
        // Esta es una aproximación. En una app real, podríamos guardar los ids de los Jobs o el estado de WorkManager
        // Para este blindaje, usaremos el estado del UniqueWork de forma síncrona si fuera posible,
        // pero como es asíncrono, confiaremos en el estado de la UI y en que beginUniqueWork con REPLACE
        // maneja la exclusión mutua. El objetivo aquí es evitar el flicker de Loading.
        return false 
    }

    private suspend fun loadKardexFromLocal(matricula: String, isInitialLoad: Boolean = false) {
        withContext(Dispatchers.IO) {
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
                    _kardexState.value = AcademicUiState.Success(list, localKardex.firstOrNull()?.lastUpdate ?: 0)
                } else if (isInitialLoad) {
                    // Si es carga inicial y está vacío, solo marcamos error si estamos offline
                    if (!isOnline()) {
                        _kardexState.value = AcademicUiState.Error("Offline: No hay datos guardados")
                    }
                } else {
                    // Si llegamos aquí después de una sincronización exitosa (isInitialLoad = false)
                    // y la lista sigue vacía, es un éxito vacío legítimo.
                    _kardexState.value = AcademicUiState.Success(emptyList(), System.currentTimeMillis())
                }
            } catch (e: Exception) {
                _kardexState.value = AcademicUiState.Error("Error local: ${e.message}")
            }
        }
    }

    fun loadCarga(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _cargaState.value
            if (!forceRefresh && currentState is AcademicUiState.Success) return@launch

            _cargaState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            
            loadCargaFromLocal(matricula, isInitialLoad = true)

            if (isOnline() && matricula.isNotEmpty()) {
                scheduleSync(
                    feature = "CARGA",
                    onWorkerFinished = {
                        viewModelScope.launch { loadCargaFromLocal(matricula, isInitialLoad = false) }
                    },
                    onWorkerFailed = { msg ->
                        if (_cargaState.value is AcademicUiState.Loading) {
                            _cargaState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            }
        }
    }

    private suspend fun loadCargaFromLocal(matricula: String, isInitialLoad: Boolean = false) {
        withContext(Dispatchers.IO) {
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
                    _cargaState.value = AcademicUiState.Success(list, localCarga.firstOrNull()?.lastUpdate ?: 0)
                } else if (isInitialLoad) {
                    if (!isOnline()) {
                        _cargaState.value = AcademicUiState.Error("Offline: No hay datos guardados")
                    }
                } else {
                    _cargaState.value = AcademicUiState.Success(emptyList(), System.currentTimeMillis())
                }
            } catch (e: Exception) {
                _cargaState.value = AcademicUiState.Error("Error local: ${e.message}")
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
            
            loadGradesFromLocal(matricula, isInitialLoad = true)

            if (isOnline() && matricula.isNotEmpty()) {
                scheduleSync(
                    feature = "GRADES",
                    onWorkerFinished = {
                        viewModelScope.launch { loadGradesFromLocal(matricula, isInitialLoad = false) }
                    },
                    onWorkerFailed = { msg ->
                        if (_parcialesState.value is AcademicUiState.Loading) {
                            _parcialesState.value = AcademicUiState.Error(msg)
                        }
                        if (_finalesState.value is AcademicUiState.Loading) {
                            _finalesState.value = AcademicUiState.Error(msg)
                        }
                    }
                )
            }
        }
    }

    private suspend fun loadGradesFromLocal(matricula: String, isInitialLoad: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.e("AcademicVM", "=== loadGradesFromLocal ===")
                android.util.Log.e("AcademicVM", "Matrícula: $matricula, isInitialLoad: $isInitialLoad")
                
                val localParciales = localRepository.getCalifUnidadSync(matricula)
                val localFinales = localRepository.getCalifFinalSync(matricula)

                android.util.Log.e("AcademicVM", "Parciales locales: ${localParciales.size} items")
                android.util.Log.e("AcademicVM", "Finales locales: ${localFinales.size} items")
                
                if (localParciales.isNotEmpty()) {
                    localParciales.forEachIndexed { index, entity ->
                        android.util.Log.d("AcademicVM", "[$index] ${entity.materia}: ${entity.parciales}")
                    }
                }

                if (localParciales.isNotEmpty() || localFinales.isNotEmpty()) {
                    val pList = localParciales.map { entity ->
                        MateriaParcial(materia = entity.materia, parciales = entity.parciales)
                    }
                    val fList = localFinales.map { entity ->
                        MateriaFinal(materia = entity.materia, calif = entity.calif)
                    }
                    
                    android.util.Log.e("AcademicVM", "✅ pList: ${pList.size}, fList: ${fList.size}")
                    
                    val lastUpdate = localParciales.firstOrNull()?.lastUpdate ?: localFinales.firstOrNull()?.lastUpdate ?: 0
                    _parcialesState.value = AcademicUiState.Success(pList, lastUpdate)
                    _finalesState.value = AcademicUiState.Success(fList, lastUpdate)
                } else if (isInitialLoad) {
                    if (!isOnline()) {
                        val err = AcademicUiState.Error("Offline: No hay datos guardados")
                        _parcialesState.value = err
                        _finalesState.value = err
                    }
                } else {
                    android.util.Log.e("AcademicVM", "⚠️ No hay datos locales, mostrando listas vacías")
                    _parcialesState.value = AcademicUiState.Success(emptyList(), System.currentTimeMillis())
                    _finalesState.value = AcademicUiState.Success(emptyList(), System.currentTimeMillis())
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "❌ Error en loadGradesFromLocal: ${e.message}", e)
                val err = AcademicUiState.Error("Error local: ${e.message}")
                _parcialesState.value = err
                _finalesState.value = err
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
