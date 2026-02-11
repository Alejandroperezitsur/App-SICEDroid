/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaParcial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

sealed interface AcademicUiState<out T> {
    object Loading : AcademicUiState<Nothing>
    data class Success<T>(val data: T) : AcademicUiState<T>
    data class Error(val message: String) : AcademicUiState<Nothing>
}

class AcademicViewModel(
    private val snRepository: SNRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _kardexState = MutableStateFlow<AcademicUiState<List<MateriaKardex>>>(AcademicUiState.Loading)
    val kardexState: StateFlow<AcademicUiState<List<MateriaKardex>>> = _kardexState.asStateFlow()

    private val _cargaState = MutableStateFlow<AcademicUiState<List<MateriaCarga>>>(AcademicUiState.Loading)
    val cargaState: StateFlow<AcademicUiState<List<MateriaCarga>>> = _cargaState.asStateFlow()

    private val _parcialesState = MutableStateFlow<AcademicUiState<List<MateriaParcial>>>(AcademicUiState.Loading)
    val parcialesState: StateFlow<AcademicUiState<List<MateriaParcial>>> = _parcialesState.asStateFlow()

    private val _finalesState = MutableStateFlow<AcademicUiState<List<MateriaFinal>>>(AcademicUiState.Loading)
    val finalesState: StateFlow<AcademicUiState<List<MateriaFinal>>> = _finalesState.asStateFlow()

    fun loadKardex() {
        viewModelScope.launch(Dispatchers.IO) {
            _kardexState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            if (matricula.isEmpty()) {
                _kardexState.value = AcademicUiState.Error("No se encontró matrícula de sesión")
                return@launch
            }

            // 1. Try Network
            try {
                val result = snRepository.getKardex(matricula)
                if (result.isNotEmpty()) {
                    _kardexState.value = AcademicUiState.Success(result)
                    return@launch
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Network Kardex failed", e)
            }

            // 2. Try Local
            try {
                val localKardex = localRepository.getKardexSync(matricula)
                if (localKardex.isNotEmpty()) {
                    val list = localKardex.map { entity ->
                        MateriaKardex(
                            clave = entity.clave,
                            nombre = entity.nombre,
                            calificacion = entity.calificacion,
                            acreditacion = entity.acreditacion,
                            periodo = entity.periodo
                        )
                    }
                    _kardexState.value = AcademicUiState.Success(list)
                } else {
                    _kardexState.value = AcademicUiState.Error("No se pudo obtener el Kardex (ni local ni red)")
                }
            } catch (e: Exception) {
                _kardexState.value = AcademicUiState.Error("Error local: ${e.message}")
            }
        }
    }

    fun loadCarga() {
        viewModelScope.launch(Dispatchers.IO) {
            _cargaState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            if (matricula.isEmpty()) {
                _cargaState.value = AcademicUiState.Error("No se encontró matrícula de sesión")
                return@launch
            }

            // 1. Try Network
            try {
                val result = snRepository.getCarga(matricula)
                if (result.isNotEmpty()) {
                    _cargaState.value = AcademicUiState.Success(result)
                    return@launch
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Network Carga failed", e)
            }

            // 2. Try Local
            try {
                val localCarga = localRepository.getCargaSync(matricula)
                if (localCarga.isNotEmpty()) {
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
                    _cargaState.value = AcademicUiState.Success(list)
                } else {
                    _cargaState.value = AcademicUiState.Error("No se pudo obtener la Carga (ni local ni red)")
                }
            } catch (e: Exception) {
                _cargaState.value = AcademicUiState.Error("Error local: ${e.message}")
            }
        }
    }

    fun loadGrades() {
        viewModelScope.launch(Dispatchers.IO) {
            _parcialesState.value = AcademicUiState.Loading
            _finalesState.value = AcademicUiState.Loading
            val matricula = snRepository.getMatricula()
            if (matricula.isEmpty()) {
                val err = AcademicUiState.Error("No se encontró matrícula de sesión")
                _parcialesState.value = err
                _finalesState.value = err
                return@launch
            }

            var netParciales: List<MateriaParcial> = emptyList()
            var netFinales: List<MateriaFinal> = emptyList()
            var netSuccess = false

            // 1. Try Network
            try {
                netParciales = snRepository.getCalifUnidades(matricula)
                netFinales = snRepository.getCalifFinal(matricula)
                // We consider success if we got at least one list or no exception
                netSuccess = true
            } catch (e: Exception) {
                android.util.Log.e("AcademicVM", "Network Grades failed", e)
            }

            if (netSuccess && (netParciales.isNotEmpty() || netFinales.isNotEmpty())) {
                _parcialesState.value = AcademicUiState.Success(netParciales)
                _finalesState.value = AcademicUiState.Success(netFinales)
                return@launch
            }

            // 2. Try Local
            try {
                val localParciales = localRepository.getCalifUnidadSync(matricula)
                val localFinales = localRepository.getCalifFinalSync(matricula)

                if (localParciales.isNotEmpty() || localFinales.isNotEmpty()) {
                    val pList = localParciales.map { entity ->
                        MateriaParcial(
                            materia = entity.materia,
                            parciales = entity.parciales
                        )
                    }
                    val fList = localFinales.map { entity ->
                        MateriaFinal(
                            materia = entity.materia,
                            calif = entity.calif
                        )
                    }
                    
                    _parcialesState.value = AcademicUiState.Success(pList)
                    _finalesState.value = AcademicUiState.Success(fList)
                } else {
                    val err = AcademicUiState.Error("No se pudieron obtener calificaciones")
                    _parcialesState.value = err
                    _finalesState.value = err
                }
            } catch (e: Exception) {
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
                AcademicViewModel(snRepository = snRepository, localRepository = localRepository)
            }
        }
    }
}
