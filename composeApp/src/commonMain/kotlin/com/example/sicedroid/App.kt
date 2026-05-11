package com.example.sicedroid

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.sicedroid.network.SiceApiService
import com.example.sicedroid.network.SNRepository
import com.example.sicedroid.model.ProfileStudent
import com.example.sicedroid.ui.ProfileScreen

@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF11998e),
            secondary = Color(0xFF38ef7d),
            surface = Color(0xFF1E1E1E),
            background = Color(0xFF121212)
        )
    ) {
        val coroutineScope = rememberCoroutineScope()
        val apiService = remember { SiceApiService() }
        val repository = remember { SNRepository(apiService) }

        var matricula by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var showSuccess by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }
        
        var profileData by remember { mutableStateOf<ProfileStudent?>(null) }
        var showProfile by remember { mutableStateOf(false) }

        if (showProfile) {
            ProfileScreen(profile = profileData, onLogout = {
                showProfile = false
                showSuccess = false
                matricula = ""
                password = ""
                profileData = null
            })
            return@MaterialTheme
        }

        val backgroundGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF11998e),
                Color(0xFF38ef7d)
            )
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp).fillMaxWidth()
                ) {
                    // Logo & Header
                    AnimatedVisibility(
                        visible = !showSuccess,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(100.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(20.dp).fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "SICEDroid",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sistema de Control Escolar",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login Card (Glassmorphism)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 450.dp)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(modifier = Modifier.padding(28.dp)) {
                            if (!showSuccess) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Bienvenido",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    
                                    OutlinedTextField(
                                        value = matricula,
                                        onValueChange = { matricula = it },
                                        label = { Text("Matrícula") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                            focusedLabelColor = Color.White,
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                            focusedLabelColor = Color.White,
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        isError = loginError
                                    )
                                    
                                    if (loginError) {
                                        Text("Credenciales inválidas o error de red", color = Color(0xFFFFCCCC), fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (matricula.isNotBlank() && password.isNotBlank()) {
                                                isLoading = true
                                                loginError = false
                                                coroutineScope.launch {
                                                    val success = repository.acceso(matricula, password)
                                                    if (success) {
                                                        showSuccess = true
                                                        isLoading = false
                                                        // Fetch profile immediately after success
                                                        val profile = repository.profile(matricula)
                                                        profileData = profile
                                                        showProfile = true
                                                    } else {
                                                        isLoading = false
                                                        loginError = true
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF11998e)
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF11998e))
                                        } else {
                                            Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                }
                            } else {
                                // Success State (transition)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "¡Acceso Exitoso!",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Cargando tu información académica...",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                        color = Color.White,
                                        trackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
