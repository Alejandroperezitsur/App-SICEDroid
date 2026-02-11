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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaParcial

@Composable
fun KardexScreen(
    viewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.kardexState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadKardex()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AcademicUiState.Loading -> LoadingScreen()
            is AcademicUiState.Error -> ErrorScreen(message = state.message)
            is AcademicUiState.Success -> KardexList(kardex = state.data)
        }
    }
}

@Composable
fun CargaScreen(
    viewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.cargaState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCarga()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AcademicUiState.Loading -> LoadingScreen()
            is AcademicUiState.Error -> ErrorScreen(message = state.message)
            is AcademicUiState.Success -> CargaList(carga = state.data)
        }
    }
}

@Composable
fun GradesScreen(
    viewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val parcialesState by viewModel.parcialesState.collectAsState()
    val finalesState by viewModel.finalesState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Parciales", "Finales")

    LaunchedEffect(Unit) {
        viewModel.loadGrades()
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                when (val state = parcialesState) {
                    is AcademicUiState.Loading -> LoadingScreen()
                    is AcademicUiState.Error -> ErrorScreen(message = state.message)
                    is AcademicUiState.Success -> ParcialesList(parciales = state.data)
                }
            }
            1 -> {
                when (val state = finalesState) {
                    is AcademicUiState.Loading -> LoadingScreen()
                    is AcademicUiState.Error -> ErrorScreen(message = state.message)
                    is AcademicUiState.Success -> FinalesList(finales = state.data)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun KardexList(kardex: List<MateriaKardex>, modifier: Modifier = Modifier) {
    if (kardex.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay materias en el Kardex")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(kardex) { materia ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = materia.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Clave: ${materia.clave}")
                            Text("Calif: ${materia.calificacion}", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Periodo: ${materia.periodo}")
                            Text("Acreditación: ${materia.acreditacion}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargaList(carga: List<MateriaCarga>, modifier: Modifier = Modifier) {
    if (carga.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay carga académica actual")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(carga) { materia ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = materia.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Docente: ${materia.docente}", style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text("Horario:", fontWeight = FontWeight.SemiBold)
                        if (materia.lunes.isNotBlank()) Text("Lunes: ${materia.lunes}")
                        if (materia.martes.isNotBlank()) Text("Martes: ${materia.martes}")
                        if (materia.miercoles.isNotBlank()) Text("Miércoles: ${materia.miercoles}")
                        if (materia.jueves.isNotBlank()) Text("Jueves: ${materia.jueves}")
                        if (materia.viernes.isNotBlank()) Text("Viernes: ${materia.viernes}")
                        if (materia.sabado.isNotBlank()) Text("Sábado: ${materia.sabado}")
                        
                        Text("Grupo: ${materia.grupo}", modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
    }
}

@Composable
fun ParcialesList(parciales: List<MateriaParcial>, modifier: Modifier = Modifier) {
    if (parciales.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay calificaciones parciales disponibles")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(parciales) { materia ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = materia.materia, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            materia.parciales.forEachIndexed { index, calif ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("U${index + 1}", fontSize = 12.sp, color = Color.Gray)
                                    Text(calif, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinalesList(finales: List<MateriaFinal>, modifier: Modifier = Modifier) {
    if (finales.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay calificaciones finales disponibles")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(finales) { materia ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = materia.materia, 
                            modifier = Modifier.weight(1f), 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = materia.calif, 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
