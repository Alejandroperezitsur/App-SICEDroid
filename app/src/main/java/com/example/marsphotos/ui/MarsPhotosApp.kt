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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.AcademicViewModel
import com.example.marsphotos.ui.screens.CargaScreen
import com.example.marsphotos.ui.screens.GradesScreen
import com.example.marsphotos.ui.screens.KardexScreen
import com.example.marsphotos.ui.screens.LoginScreen
import com.example.marsphotos.ui.screens.LoginViewModel
import com.example.marsphotos.ui.screens.ProfileScreen
import com.example.marsphotos.ui.screens.ProfileViewModel

object Routes {
    const val LOGIN = "login"
    const val PROFILE = "profile"
    const val KARDEX = "kardex"
    const val CARGA = "carga"
    const val GRADES = "grades"
}

@Composable
fun MarsPhotosApp() {
    val context = LocalContext.current
    val sessionManager = remember { (context.applicationContext as MarsPhotosApplication).container.sessionManager }
    val hasSavedSession = remember { sessionManager.isLoggedIn() }
    val savedMatricula = remember { sessionManager.getMatricula() }

    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = if (hasSavedSession) Routes.PROFILE else Routes.LOGIN
        ) {
            composable(Routes.LOGIN) {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)

                if (hasSavedSession) {
                    LaunchedEffect(Unit) {
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
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        loginViewModel.resetState()
                    },
                    onResetForm = loginViewModel::resetForm
                )
            }

            composable(Routes.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)

                LaunchedEffect(Unit) {
                    val matricula = sessionManager.getMatricula()
                    if (matricula.isNotEmpty()) {
                        profileViewModel.loadProfile(matricula)
                    }
                }

                ProfileScreen(
                    profileUiState = profileViewModel.profileUiState,
                    onLogoutClick = {
                        sessionManager.clearSession()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onKardexClick = { navController.navigate(Routes.KARDEX) },
                    onCargaClick = { navController.navigate(Routes.CARGA) },
                    onGradesClick = { navController.navigate(Routes.GRADES) }
                )
            }

            composable(Routes.KARDEX) {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.kardex_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
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

            composable(Routes.CARGA) {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.carga_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
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

            composable(Routes.GRADES) {
                val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.grades_title),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
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
