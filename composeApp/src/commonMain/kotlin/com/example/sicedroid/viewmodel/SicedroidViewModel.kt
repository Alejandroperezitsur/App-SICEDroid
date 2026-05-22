package com.example.sicedroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicedroid.Screen
import com.example.sicedroid.currentTimeMillis
import com.example.sicedroid.db.LocalDataSource
import com.example.sicedroid.model.MateriaCarga
import com.example.sicedroid.model.MateriaFinal
import com.example.sicedroid.model.MateriaKardex
import com.example.sicedroid.model.MateriaParcial
import com.example.sicedroid.model.ProfileStudent
import com.example.sicedroid.network.SNRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object CheckingSession : LoginUiState
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface AcademicDataState {
    data object Idle : AcademicDataState
    data object Loading : AcademicDataState
    data object Success : AcademicDataState
    data class Error(val message: String) : AcademicDataState
}

class SicedroidViewModel(
    private val repository: SNRepository,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    //NAVEGACIÓN ENTRE PANTALLAS CON STATEFLOW
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.CheckingSession)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _matricula = MutableStateFlow("")
    val matricula: StateFlow<String> = _matricula.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _profileData = MutableStateFlow<ProfileStudent?>(null)
    val profileData: StateFlow<ProfileStudent?> = _profileData.asStateFlow()

    private val _lastUpdate = MutableStateFlow(0L)
    val lastUpdate: StateFlow<Long> = _lastUpdate.asStateFlow()

    private val _kardexData = MutableStateFlow<List<MateriaKardex>>(emptyList())
    val kardexData: StateFlow<List<MateriaKardex>> = _kardexData.asStateFlow()

    private val _cargaData = MutableStateFlow<List<MateriaCarga>>(emptyList())
    val cargaData: StateFlow<List<MateriaCarga>> = _cargaData.asStateFlow()

    private val _parcialesData = MutableStateFlow<List<MateriaParcial>>(emptyList())
    val parcialesData: StateFlow<List<MateriaParcial>> = _parcialesData.asStateFlow()

    private val _finalesData = MutableStateFlow<List<MateriaFinal>>(emptyList())
    val finalesData: StateFlow<List<MateriaFinal>> = _finalesData.asStateFlow()

    private val _academicState = MutableStateFlow<AcademicDataState>(AcademicDataState.Idle)
    val academicState: StateFlow<AcademicDataState> = _academicState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val session = localDataSource.getSession()
            if (session != null) {
                _loginState.value = LoginUiState.Loading
                val success = repository.acceso(session.matricula, session.password)
                if (success) {
                    _matricula.value = session.matricula
                    _password.value = session.password
                    localDataSource.saveSession(session.matricula, session.password)
                    val profile = repository.profile(session.matricula)
                    localDataSource.saveProfile(session.matricula, profile)
                    _profileData.value = profile
                    _lastUpdate.value = currentTimeMillis()
                    _loginState.value = LoginUiState.Success
                    _currentScreen.value = Screen.PROFILE
                } else {
                    localDataSource.clearSession()
                    _loginState.value = LoginUiState.Idle
                }
            } else {
                _loginState.value = LoginUiState.Idle
            }
        }
    }

    fun updateMatricula(value: String) { _matricula.value = value }
    fun updatePassword(value: String) { _password.value = value }

    fun login() {
        val m = _matricula.value
        val p = _password.value
        if (m.isNotBlank() && p.isNotBlank()) {
            _loginState.value = LoginUiState.Loading
            viewModelScope.launch {
                val success = repository.acceso(m, p)
                if (success) {
                    localDataSource.saveSession(m, p)
                    val profile = repository.profile(m)
                    localDataSource.saveProfile(m, profile)
                    _profileData.value = profile
                    _lastUpdate.value = currentTimeMillis()
                    _loginState.value = LoginUiState.Success
                    _currentScreen.value = Screen.PROFILE
                } else {
                    _loginState.value = LoginUiState.Error("Credenciales inválidas. Verifica tu matrícula y contraseña.")
                }
            }
        }
    }

    fun clearLoginError() {
        _loginState.value = LoginUiState.Idle
        _matricula.value = ""
        _password.value = ""
    }

    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    fun loadAcademicData() {
        val m = _matricula.value
        _kardexData.value = localDataSource.getKardex(m)
        _cargaData.value = localDataSource.getCarga(m)
        _parcialesData.value = localDataSource.getCalifUnidad(m)
        _finalesData.value = localDataSource.getCalifFinal(m)

        _academicState.value = AcademicDataState.Loading
        viewModelScope.launch {
            try {
                _kardexData.value = repository.getKardex(m)
                _cargaData.value = repository.getCarga(m)
                _parcialesData.value = repository.getCalifUnidades(m)
                _finalesData.value = repository.getCalifFinal(m)
                localDataSource.saveKardex(m, _kardexData.value)
                localDataSource.saveCarga(m, _cargaData.value)
                localDataSource.saveCalifUnidad(m, _parcialesData.value)
                localDataSource.saveCalifFinal(m, _finalesData.value)
                _academicState.value = AcademicDataState.Success
            } catch (_: Exception) {
                _academicState.value = AcademicDataState.Success
            }
        }
    }

    fun logout() {
        localDataSource.clearSession()
        localDataSource.clearAll(_matricula.value)
        _currentScreen.value = Screen.LOGIN
        _matricula.value = ""
        _password.value = ""
        _profileData.value = null
        _lastUpdate.value = 0L
        _kardexData.value = emptyList()
        _cargaData.value = emptyList()
        _parcialesData.value = emptyList()
        _finalesData.value = emptyList()
        _loginState.value = LoginUiState.Idle
        _academicState.value = AcademicDataState.Idle
    }
}
