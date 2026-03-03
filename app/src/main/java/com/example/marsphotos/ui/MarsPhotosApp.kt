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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.SessionManager
import com.example.marsphotos.ui.screens.AcademicViewModel
import com.example.marsphotos.ui.screens.CargaScreen
import com.example.marsphotos.ui.screens.GradesScreen
import com.example.marsphotos.ui.screens.KardexScreen
import com.example.marsphotos.ui.screens.LoginScreen
import com.example.marsphotos.ui.screens.LoginViewModel
import com.example.marsphotos.ui.screens.ProfileScreen
import com.example.marsphotos.ui.screens.ProfileViewModel

/**
 * Aplicación principal con navegación entre Login y Profile
 * Ahora con persistencia de sesión - si hay sesión guardada, va directo al perfil
 */
@Composable
fun MarsPhotosApp() {
    val context = LocalContext.current
    val sessionManager = remember { (context.applicationContext as MarsPhotosApplication).container.sessionManager }
    
    // Verificar si hay sesión guardada al inicio
    val hasSavedSession = remember { sessionManager.isLoggedIn() }
    val savedMatricula = remember { sessionManager.getMatricula() }
    
    // Estado para controlar la navegación
    var currentScreen by remember { mutableStateOf(if (hasSavedSession) AppScreen.PROFILE else AppScreen.LOGIN) }
    var userMatricula by remember { mutableStateOf(if (hasSavedSession) savedMatricula else "") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.LOGIN -> {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                
                // Si hay sesión guardada, actualizar el ViewModel con las credenciales
                LaunchedEffect(Unit) {
                    if (hasSavedSession) {
                        val credentials = sessionManager.getCredentials()
                        credentials?.let { (mat, pass) ->
                            loginViewModel.updateMatricula(mat)
                            loginViewModel.updateContrasenia(pass)
                        }
                    }
                }
                
                LoginScreen(
                    loginUiState = loginViewModel.loginUiState,
                    matricula = loginViewModel.matricula,
                    contrasenia = loginViewModel.contrasenia,
                    onMatriculaChange = loginViewModel::updateMatricula,
                    onContraseniaChange = loginViewModel::updateContrasenia,
                    onLoginClick = loginViewModel::login,
                    onLoginSuccess = { matricula ->
                        userMatricula = matricula
                        currentScreen = AppScreen.PROFILE
                        loginViewModel.resetState()
                    },
                    onResetForm = loginViewModel::resetForm
                )
            }

            AppScreen.PROFILE -> {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
                
                // Cargar perfil cuando se abre la pantalla
                LaunchedEffect(userMatricula) {
                    if (userMatricula.isNotEmpty()) {
                        profileViewModel.loadProfile(userMatricula)
                    }
                }
                
                ProfileScreen(
                    profileUiState = profileViewModel.profileUiState,
                    onLogoutClick = {
                        // Cerrar sesión y limpiar datos
                        sessionManager.clearSession()
                        currentScreen = AppScreen.LOGIN
                        userMatricula = ""
                    },
                    onKardexClick = { currentScreen = AppScreen.KARDEX },
                    onCargaClick = { currentScreen = AppScreen.CARGA },
                    onGradesClick = { currentScreen = AppScreen.GRADES }
                )
            }

            AppScreen.KARDEX -> {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = com.example.marsphotos.R.string.kardex_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                ) 
                            },
                            navigationIcon = {
                                IconButton(onClick = { currentScreen = AppScreen.PROFILE }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    KardexScreen(
                        viewModel = academicViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            AppScreen.CARGA -> {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = com.example.marsphotos.R.string.carga_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                ) 
                            },
                            navigationIcon = {
                                IconButton(onClick = { currentScreen = AppScreen.PROFILE }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    CargaScreen(
                        viewModel = academicViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            AppScreen.GRADES -> {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = com.example.marsphotos.R.string.grades_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                ) 
                            },
                            navigationIcon = {
                                IconButton(onClick = { currentScreen = AppScreen.PROFILE }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    GradesScreen(
                        viewModel = academicViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Enum para controlar las pantallas de la aplicación
 */
enum class AppScreen {
    LOGIN,
    PROFILE,
    KARDEX,
    CARGA,
    GRADES
}
