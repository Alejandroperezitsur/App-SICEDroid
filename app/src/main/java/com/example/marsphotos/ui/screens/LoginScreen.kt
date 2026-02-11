package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marsphotos.R
import com.example.marsphotos.ui.theme.SICENETTheme

/**
 * Pantalla de login para autenticarse en SICENET
 */
@Composable
fun LoginScreen(
    loginUiState: LoginUiState,
    matricula: String,
    contrasenia: String,
    onMatriculaChange: (String) -> Unit,
    onContraseniaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onResetForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (loginUiState) {
        is LoginUiState.Idle -> {
            LoginFormScreen(
                matricula = matricula,
                contrasenia = contrasenia,
                onMatriculaChange = onMatriculaChange,
                onContraseniaChange = onContraseniaChange,
                onLoginClick = onLoginClick,
                modifier = modifier
            )
        }
        is LoginUiState.Loading -> {
            LoadingLoginScreen(modifier = modifier)
        }
        is LoginUiState.Success -> {
            onLoginSuccess(loginUiState.matricula)
        }
        is LoginUiState.Error -> {
            LoginErrorScreen(
                error = loginUiState.message,
                onRetryClick = onResetForm,
                modifier = modifier
            )
        }
    }
}

/**
 * Formulario de login
 */
@Composable
fun LoginFormScreen(
    matricula: String,
    contrasenia: String,
    onMatriculaChange: (String) -> Unit,
    onContraseniaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordVisible = remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SICENET",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = matricula,
                    onValueChange = onMatriculaChange,
                    label = { Text("Matrícula") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = onContraseniaChange,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(
                                imageVector = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible.value) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}

/**
 * Pantalla de carga durante el login
 */
@Composable
fun LoadingLoginScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Autenticando...")
    }
}

/**
 * Pantalla de error durante el login
 */
@Composable
fun LoginErrorScreen(
    error: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error",
                    fontSize = 20.sp
                )
                
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp)
                )
                
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Intentar de nuevo")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginFormScreenPreview() {
    SICENETTheme {
        LoginFormScreen(
            matricula = "",
            contrasenia = "",
            onMatriculaChange = {},
            onContraseniaChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingLoginScreenPreview() {
    SICENETTheme {
        LoadingLoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginErrorScreenPreview() {
    SICENETTheme {
        LoginErrorScreen(
            error = "Credenciales inválidas",
            onRetryClick = {}
        )
    }
}
