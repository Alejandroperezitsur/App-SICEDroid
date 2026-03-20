package com.example.sicedroid_client.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicedroid_client.data.SicenetProviderClient
import com.example.sicedroid_client.model.CalifFinal
import com.example.sicedroid_client.model.CalifUnidad
import com.example.sicedroid_client.model.CargaEntry
import com.example.sicedroid_client.model.KardexEntry
import com.example.sicedroid_client.model.ProviderResult
import com.example.sicedroid_client.model.Student
import kotlinx.coroutines.launch

/**
 * ViewModel principal para la aplicación cliente.
 * Gestiona el estado de la UI y las operaciones con el Content Provider.
 */
class AcademicViewModel(application: Application) : AndroidViewModel(application) {

    private val providerClient = SicenetProviderClient(application.applicationContext)

    // Estados de permisos
    private val _hasReadPermission = mutableStateOf(false)
    val hasReadPermission: State<Boolean> = _hasReadPermission

    private val _hasWritePermission = mutableStateOf(false)
    val hasWritePermission: State<Boolean> = _hasWritePermission

    // Estado de carga
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Estados de mensajes
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Estados de datos
    private val _student = mutableStateOf<Student?>(null)
    val student: State<Student?> = _student

    private val _kardex = mutableStateOf<List<KardexEntry>>(emptyList())
    val kardex: State<List<KardexEntry>> = _kardex

    private val _carga = mutableStateOf<List<CargaEntry>>(emptyList())
    val carga: State<List<CargaEntry>> = _carga

    private val _califUnidad = mutableStateOf<List<CalifUnidad>>(emptyList())
    val califUnidad: State<List<CalifUnidad>> = _califUnidad

    private val _califFinal = mutableStateOf<List<CalifFinal>>(emptyList())
    val califFinal: State<List<CalifFinal>> = _califFinal

    init {
        checkPermissions()
    }

    /**
     * Verifica el estado actual de los permisos.
     */
    fun checkPermissions() {
        _hasReadPermission.value = providerClient.hasReadPermission()
        _hasWritePermission.value = providerClient.hasWritePermission()
    }

    /**
     * Consulta todos los datos del estudiante.
     */
    fun fetchAllData(matricula: String) {
        if (!_hasReadPermission.value) {
            _errorMessage.value = "Se requiere permiso de lectura"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            // Consultar estudiante
            when (val result = providerClient.getStudent(matricula)) {
                is ProviderResult.Success -> {
                    _student.value = result.data
                }
                is ProviderResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }

            // Consultar kardex
            when (val result = providerClient.getKardex(matricula)) {
                is ProviderResult.Success -> {
                    _kardex.value = result.data
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            // Consultar carga
            when (val result = providerClient.getCarga(matricula)) {
                is ProviderResult.Success -> {
                    _carga.value = result.data
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            // Consultar calif unidad
            when (val result = providerClient.getCalifUnidad(matricula)) {
                is ProviderResult.Success -> {
                    _califUnidad.value = result.data
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            // Consultar calif final
            when (val result = providerClient.getCalifFinal(matricula)) {
                is ProviderResult.Success -> {
                    _califFinal.value = result.data
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            _isLoading.value = false
            if (_errorMessage.value == null) {
                _successMessage.value = "Datos cargados correctamente"
            }
        }
    }

    /**
     * Prueba de escritura: Inserta un registro de prueba en el kardex.
     */
    fun testInsert(matricula: String) {
        if (!_hasWritePermission.value) {
            _errorMessage.value = "Se requiere permiso de escritura para esta operación"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val testEntry = KardexEntry(
                matricula = matricula,
                clave = "TEST001",
                nombre = "Materia de Prueba",
                calificacion = 85,
                acreditacion = "A",
                periodo = "AGO-DIC 2024"
            )

            when (val result = providerClient.insertKardexEntry(testEntry)) {
                is ProviderResult.Success -> {
                    _successMessage.value = "Registro insertado correctamente"
                    // Recargar kardex
                    fetchKardex(matricula)
                }
                is ProviderResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Prueba de escritura: Elimina registros del kardex.
     */
    fun testDelete(matricula: String) {
        if (!_hasWritePermission.value) {
            _errorMessage.value = "Se requiere permiso de escritura para esta operación"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            when (val result = providerClient.deleteKardex(matricula)) {
                is ProviderResult.Success -> {
                    _successMessage.value = "${result.data} registros eliminados"
                    _kardex.value = emptyList()
                }
                is ProviderResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Consulta solo el kardex.
     */
    fun fetchKardex(matricula: String) {
        if (!_hasReadPermission.value) {
            _errorMessage.value = "Se requiere permiso de lectura"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = providerClient.getKardex(matricula)) {
                is ProviderResult.Success -> {
                    _kardex.value = result.data
                    _successMessage.value = "Kardex cargado: ${result.data.size} materias"
                }
                is ProviderResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Consulta solo la carga académica.
     */
    fun fetchCarga(matricula: String) {
        if (!_hasReadPermission.value) {
            _errorMessage.value = "Se requiere permiso de lectura"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = providerClient.getCarga(matricula)) {
                is ProviderResult.Success -> {
                    _carga.value = result.data
                    _successMessage.value = "Carga cargada: ${result.data.size} materias"
                }
                is ProviderResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Consulta solo las calificaciones.
     */
    fun fetchCalificaciones(matricula: String) {
        if (!_hasReadPermission.value) {
            _errorMessage.value = "Se requiere permiso de lectura"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Calificaciones por unidad
            when (val result = providerClient.getCalifUnidad(matricula)) {
                is ProviderResult.Success -> {
                    _califUnidad.value = result.data
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            // Calificaciones finales
            when (val result = providerClient.getCalifFinal(matricula)) {
                is ProviderResult.Success -> {
                    _califFinal.value = result.data
                    _successMessage.value = "Calificaciones cargadas"
                }
                is ProviderResult.Error -> {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = result.message
                    }
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
