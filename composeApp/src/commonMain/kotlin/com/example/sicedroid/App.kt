package com.example.sicedroid

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.sicedroid.db.LocalDataSource
import com.example.sicedroid.network.SiceApiService
import com.example.sicedroid.network.SNRepository
import com.example.sicedroid.model.*
import com.example.sicedroid.ui.*

private val SicenetGreen = Color(0xFF1B5E20)
private val SicenetGreenLight = Color(0xFFC8E6C9)

expect fun currentTimeMillis(): Long

enum class Screen {
    LOGIN, PROFILE, ACADEMIC_HOME, KARDEX, CARGA, CALIFICACIONES
}

@Composable
fun App(localDataSource: LocalDataSource) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SicenetGreen,
            primaryContainer = SicenetGreenLight,
            onPrimary = Color.White,
            onPrimaryContainer = Color(0xFF002105),
            secondary = Color(0xFF00695C),
            secondaryContainer = Color(0xFFB2DFDB),
            surface = Color(0xFFF8F9FA),
            background = Color(0xFFF8F9FA),
            onSurface = Color(0xFF1C1B1F),
            onSurfaceVariant = Color(0xFF43483F),
            error = Color(0xFFB3261E),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        )
    ) {
        val coroutineScope = rememberCoroutineScope()
        val apiService = remember { SiceApiService() }
        val repository = remember { SNRepository(apiService) }

        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var matricula by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var showSuccess by remember { mutableStateOf(false) }
        var profileData by remember { mutableStateOf<ProfileStudent?>(null) }
        var lastUpdate by remember { mutableStateOf(0L) }

        var kardexData by remember { mutableStateOf<List<MateriaKardex>>(emptyList()) }
        var cargaData by remember { mutableStateOf<List<MateriaCarga>>(emptyList()) }
        var parcialesData by remember { mutableStateOf<List<MateriaParcial>>(emptyList()) }
        var finalesData by remember { mutableStateOf<List<MateriaFinal>>(emptyList()) }
        var academicLoading by remember { mutableStateOf(false) }

        var isCheckingSession by remember { mutableStateOf(true) }

        val currentMatricula = matricula

        LaunchedEffect(Unit) {
            val session = localDataSource.getSession()
            if (session != null) {
                matricula = session.matricula
                password = session.password
                isLoading = true
                val success = repository.acceso(session.matricula, session.password)
                if (success) {
                    showSuccess = true
                    lastUpdate = currentTimeMillis()
                    localDataSource.saveSession(session.matricula, session.password)
                    val profile = repository.profile(session.matricula)
                    profileData = profile
                    localDataSource.saveProfile(session.matricula, profile)
                    isLoading = false
                    currentScreen = Screen.PROFILE
                } else {
                    localDataSource.clearSession()
                    matricula = ""
                    password = ""
                    isLoading = false
                }
            }
            isCheckingSession = false
        }

        fun resetLogin() {
            localDataSource.clearSession()
            localDataSource.clearAll(currentMatricula)
            currentScreen = Screen.LOGIN
            showSuccess = false
            matricula = ""
            password = ""
            profileData = null
            loginError = false
            errorMessage = ""
            lastUpdate = 0L
            kardexData = emptyList()
            cargaData = emptyList()
            parcialesData = emptyList()
            finalesData = emptyList()
        }

        fun doLogin() {
            if (matricula.isNotBlank() && password.isNotBlank()) {
                isLoading = true
                loginError = false
                errorMessage = ""
                coroutineScope.launch {
                    val success = repository.acceso(matricula, password)
                    if (success) {
                        showSuccess = true
                        lastUpdate = currentTimeMillis()
                        localDataSource.saveSession(matricula, password)
                        val profile = repository.profile(matricula)
                        profileData = profile
                        localDataSource.saveProfile(matricula, profile)
                        isLoading = false
                        currentScreen = Screen.PROFILE
                    } else {
                        isLoading = false
                        loginError = true
                        errorMessage = "Credenciales inválidas. Verifica tu matrícula y contraseña."
                    }
                }
            }
        }

        fun loadAcademicData() {
            kardexData = localDataSource.getKardex(currentMatricula)
            cargaData = localDataSource.getCarga(currentMatricula)
            parcialesData = localDataSource.getCalifUnidad(currentMatricula)
            finalesData = localDataSource.getCalifFinal(currentMatricula)

            academicLoading = true
            coroutineScope.launch {
                try {
                    val k = repository.getKardex(currentMatricula)
                    val c = repository.getCarga(currentMatricula)
                    val p = repository.getCalifUnidades(currentMatricula)
                    val f = repository.getCalifFinal(currentMatricula)
                    kardexData = k
                    cargaData = c
                    parcialesData = p
                    finalesData = f
                    localDataSource.saveKardex(currentMatricula, k)
                    localDataSource.saveCarga(currentMatricula, c)
                    localDataSource.saveCalifUnidad(currentMatricula, p)
                    localDataSource.saveCalifFinal(currentMatricula, f)
                } catch (_: Exception) { }
                academicLoading = false
            }
        }

        if (isCheckingSession) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Cargando sesión...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else when (currentScreen) {
            Screen.LOGIN -> {
                var passwordVisible by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = isLoading,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "login_loading"
                    ) { loading ->
                        if (loading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(56.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 4.dp
                                        )
                                        Text(
                                            "Iniciando sesión...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            LoginContent(
                                matricula = matricula,
                                password = password,
                                passwordVisible = passwordVisible,
                                loginError = loginError,
                                errorMessage = errorMessage,
                                onMatriculaChange = { matricula = it },
                                onPasswordChange = { password = it },
                                onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                                onLogin = { doLogin() },
                                onClear = {
                                    loginError = false
                                    errorMessage = ""
                                    matricula = ""
                                    password = ""
                                }
                            )
                        }
                    }
                }
            }

            Screen.PROFILE -> {
                ProfileScreen(
                    profile = profileData,
                    lastUpdate = lastUpdate,
                    onLogout = { resetLogin() },
                    onAcademicHome = {
                        loadAcademicData()
                        currentScreen = Screen.ACADEMIC_HOME
                    }
                )
            }

            Screen.ACADEMIC_HOME -> {
                AcademicHomeScreen(
                    isLoading = academicLoading,
                    onKardexClick = { currentScreen = Screen.KARDEX },
                    onCargaClick = { currentScreen = Screen.CARGA },
                    onCalificacionesClick = { currentScreen = Screen.CALIFICACIONES },
                    onBack = { currentScreen = Screen.PROFILE }
                )
            }

            Screen.KARDEX -> {
                KardexScreen(
                    materias = kardexData,
                    isLoading = false,
                    onBack = { currentScreen = Screen.ACADEMIC_HOME }
                )
            }

            Screen.CARGA -> {
                CargaScreen(
                    materias = cargaData,
                    isLoading = false,
                    onBack = { currentScreen = Screen.ACADEMIC_HOME }
                )
            }

            Screen.CALIFICACIONES -> {
                CalificacionesScreen(
                    parciales = parcialesData,
                    finales = finalesData,
                    isLoading = false,
                    onBack = { currentScreen = Screen.ACADEMIC_HOME }
                )
            }
        }
    }
}

@Composable
private fun LoginContent(
    matricula: String,
    password: String,
    passwordVisible: Boolean,
    loginError: Boolean,
    errorMessage: String,
    onMatriculaChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onLogin: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SICEDroid",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sistema de Control Escolar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!loginError) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = matricula,
                        onValueChange = onMatriculaChange,
                        label = { Text("Matrícula") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = onPasswordVisibleToggle) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Entrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Error de Autenticación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Reintentar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = matricula,
                        onValueChange = onMatriculaChange,
                        label = { Text("Matrícula") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = onPasswordVisibleToggle) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onClear,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Limpiar")
                        }
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Entrar")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Instituto Tecnológico Superior del Sur de Guanajuato",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
