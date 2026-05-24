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
import com.example.sicedroid.notifications.platformSendGradeNotification
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
    data class Success(val isOffline: Boolean = false) : AcademicDataState
    data class Error(val message: String) : AcademicDataState
}

class SicedroidViewModel(
    private val repository: SNRepository,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    //NAVEGACIÓN CON BACK STACK MANUAL
    private val _screenStack = mutableListOf(Screen.LOGIN)
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
                    navigateAndClearStack(Screen.PROFILE)
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
                    navigateAndClearStack(Screen.PROFILE)
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

    fun navigateTo(screen: Screen) {
        _screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun goBack() {
        if (_screenStack.size > 1) {
            _screenStack.removeLast()
            _currentScreen.value = _screenStack.last()
        }
    }

    private fun navigateAndClearStack(screen: Screen) {
        _screenStack.clear()
        _screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun loadAcademicData() {
        val m = _matricula.value
        val oldParciales = localDataSource.getCalifUnidad(m)
        val oldFinales = localDataSource.getCalifFinal(m)
        _kardexData.value = localDataSource.getKardex(m)
        _cargaData.value = localDataSource.getCarga(m)
        _parcialesData.value = oldParciales
        _finalesData.value = oldFinales

        _academicState.value = AcademicDataState.Loading
        viewModelScope.launch {
            try {
                val newParciales = repository.getCalifUnidades(m)
                val newFinales = repository.getCalifFinal(m)

                checkGradeChanges(oldParciales, newParciales, oldFinales, newFinales)

                _kardexData.value = repository.getKardex(m)
                _cargaData.value = repository.getCarga(m)
                _parcialesData.value = newParciales
                _finalesData.value = newFinales
                localDataSource.saveKardex(m, _kardexData.value)
                localDataSource.saveCarga(m, _cargaData.value)
                localDataSource.saveCalifUnidad(m, newParciales)
                localDataSource.saveCalifFinal(m, newFinales)
                _academicState.value = AcademicDataState.Success()
            } catch (_: Exception) {
                _academicState.value = AcademicDataState.Success(isOffline = true)
            }
        }
    }

    private fun checkGradeChanges(
        oldParciales: List<MateriaParcial>,
        newParciales: List<MateriaParcial>,
        oldFinales: List<MateriaFinal>,
        newFinales: List<MateriaFinal>
    ) {
        var changes = 0
        newParciales.forEach { newM ->
            val oldM = oldParciales.find { it.materia == newM.materia }
            newM.parciales.forEachIndexed { i, newGrade ->
                val oldGrade = oldM?.parciales?.getOrNull(i) ?: ""
                if (hasNewGrade(oldGrade, newGrade)) {
                    platformSendGradeNotification(
                        "Nueva calificación",
                        "${newM.materia} - Unidad ${i + 1}: $newGrade"
                    )
                    changes++
                }
            }
        }
        newFinales.forEach { newF ->
            val oldF = oldFinales.find { it.materia == newF.materia }
            val oldCalif = oldF?.calif?.toString() ?: ""
            if (hasNewGrade(oldCalif, newF.calif.toString())) {
                platformSendGradeNotification(
                    "Nueva calificación final",
                    "${newF.materia}: ${newF.calif}"
                )
                changes++
            }
        }
        if (changes > 1) {
            platformSendGradeNotification(
                "Calificaciones actualizadas",
                "Se actualizaron $changes calificaciones"
            )
        }
    }

    private fun hasNewGrade(oldGrade: String, newGrade: String): Boolean {
        if (oldGrade.isNullOrEmpty() || oldGrade == "0" || oldGrade == "-") {
            return newGrade.isNotEmpty() && newGrade != "0" && newGrade != "-"
        }
        return false
    }

    fun logout() {
        localDataSource.clearSession()
        localDataSource.clearAll(_matricula.value)
        navigateAndClearStack(Screen.LOGIN)
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
