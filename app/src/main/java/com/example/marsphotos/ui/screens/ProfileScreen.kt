package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.ui.theme.SICENETTheme
import coil.compose.AsyncImage

/**
 * Pantalla que muestra el perfil académico del estudiante
 */
@Composable
fun ProfileScreen(
    profileUiState: ProfileUiState,
    onBackClick: () -> Unit,
    onKardexClick: () -> Unit,
    onCargaClick: () -> Unit,
    onGradesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top AppBar
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = { Text("Perfil Académico") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Atrás"
                    )
                }
            }
        )

        // Contenido principal
        when (profileUiState) {
            is ProfileUiState.Loading -> {
                LoadingProfileScreen(modifier = Modifier.fillMaxSize())
            }
            is ProfileUiState.Success -> {
                ProfileDetailScreen(
                    profile = profileUiState.profile,
                    onKardexClick = onKardexClick,
                    onCargaClick = onCargaClick,
                    onGradesClick = onGradesClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
            is ProfileUiState.Error -> {
                ProfileErrorScreen(
                    error = profileUiState.message,
                    onRetryClick = onBackClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    profile: ProfileStudent,
    onKardexClick: () -> Unit,
    onCargaClick: () -> Unit,
    onGradesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ... (Información Personal Card - unchanged logic but keep it for context)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Información Personal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.fotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profile.fotoUrl,
                            contentDescription = "Foto",
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        ProfileInfoRow(label = "Matrícula", value = profile.matricula)
                        val fullName = if (profile.apellidos.isNotEmpty()) "${profile.nombre} ${profile.apellidos}" else profile.nombre
                        ProfileInfoRow(label = "Nombre", value = fullName)
                    }
                }
            }
        }

        // Información Académica
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Información Académica", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Divider()
                ProfileInfoRow(label = "Carrera", value = profile.carrera)
                ProfileInfoRow(label = "Estatus Académico", value = profile.estatusAcademico)
                ProfileInfoRow(label = "Especialidad", value = profile.especialidad)
                ProfileInfoRow(label = "Semestre", value = profile.semestre)
                ProfileInfoRow(label = "Promedio", value = profile.promedio)
                ProfileInfoRow(label = "Estado", value = profile.estado)
                ProfileInfoRow(label = "Status Matrícula", value = profile.statusMatricula)
                ProfileInfoRow(label = "Estatus Alumno", value = profile.estatusAlumno)
                Divider()
                ProfileInfoRow(label = "Cdts. Reunidos", value = profile.cdtsReunidos)
                ProfileInfoRow(label = "Cdts. Actuales", value = profile.cdtsActuales)
                ProfileInfoRow(label = "Inscrito", value = profile.inscrito)
                ProfileInfoRow(label = "Reinscripción", value = profile.reinscripcionFecha)
                
                if (profile.sinAdeudos.isNotEmpty()) {
                    Text(text = profile.sinAdeudos, color = Color(0xFF006400), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // Operaciones Académicas Interactiva
        if (profile.operaciones.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Operaciones Académicas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Divider()
                    profile.operaciones.forEach { op ->
                        val onClick = when {
                            op.contains("KARDEX") -> onKardexClick
                            op.contains("CARGA") -> onCargaClick
                            op.contains("CALIFICACIONES") -> onGradesClick
                            else -> { {} }
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = onClick
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = op, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Icon(imageVector = Icons.Filled.Info, contentDescription = "Ver", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}

/**
 * Pantalla de carga para el perfil
 */
@Composable
fun LoadingProfileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cargando perfil...")
    }
}

/**
 * Pantalla de error para el perfil
 */
@Composable
fun ProfileErrorScreen(
    error: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
                    Text("Volver atrás")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileDetailScreenPreview() {
    SICENETTheme {
        ProfileDetailScreen(
            profile = ProfileStudent(
                matricula = "S19120153",
                nombre = "Juan",
                apellidos = "Pérez García",
                carrera = "Ingeniería en Sistemas Computacionales",
                semestre = "6",
                promedio = "8.5",
                estado = "Activo",
                statusMatricula = "Vigente"
            ),
            onKardexClick = {},
            onCargaClick = {},
            onGradesClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingProfileScreenPreview() {
    SICENETTheme {
        LoadingProfileScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileErrorScreenPreview() {
    SICENETTheme {
        ProfileErrorScreen(
            error = "Error al cargar perfil",
            onRetryClick = {}
        )
    }
}
