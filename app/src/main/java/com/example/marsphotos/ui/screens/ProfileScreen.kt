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
import androidx.compose.ui.res.stringResource
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.ui.theme.SICENETTheme
import coil.compose.AsyncImage

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
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = { Text(stringResource(id = com.example.marsphotos.R.string.profile_screen_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )

        when (profileUiState) {
            is ProfileUiState.Loading -> {
                LoadingProfileScreen(modifier = Modifier.fillMaxSize())
            }
            is ProfileUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    LastUpdateLabel(timestamp = profileUiState.lastUpdate)
                    ProfileDetailScreen(
                        profile = profileUiState.profile,
                        onKardexClick = onKardexClick,
                        onCargaClick = onCargaClick,
                        onGradesClick = onGradesClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                }
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(id = com.example.marsphotos.R.string.profile_section_personal), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_matricula), value = profile.matricula)
                        val fullName = if (profile.apellidos.isNotEmpty()) "${profile.nombre} ${profile.apellidos}" else profile.nombre
                        ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_nombre), value = fullName)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(id = com.example.marsphotos.R.string.profile_section_academic), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Divider()
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_carrera), value = profile.carrera)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_estatus_academico), value = profile.estatusAcademico)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_especialidad), value = profile.especialidad)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_semestre), value = profile.semestre.toString())
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_promedio), value = profile.promedio)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_estado), value = profile.estado)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_status_matricula), value = profile.statusMatricula)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_estatus_alumno), value = profile.estatusAlumno)
                Divider()
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_cdts_reunidos), value = profile.cdtsReunidos.toString())
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_cdts_actuales), value = profile.cdtsActuales.toString())
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_inscrito), value = profile.inscrito)
                ProfileInfoRow(label = stringResource(id = com.example.marsphotos.R.string.profile_label_reinscripcion), value = profile.reinscripcionFecha)
                
                if (profile.sinAdeudos.isNotEmpty()) {
                    Text(text = profile.sinAdeudos, color = Color(0xFF006400), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

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
                    Text(text = stringResource(id = com.example.marsphotos.R.string.profile_section_operaciones), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                                Icon(imageVector = Icons.Filled.Info, contentDescription = stringResource(id = com.example.marsphotos.R.string.profile_operations_icon_content_description), tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp)
    }
}

@Composable
fun LoadingProfileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(id = com.example.marsphotos.R.string.profile_loading))
    }
}

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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = com.example.marsphotos.R.string.login_error_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = error, modifier = Modifier.padding(16.dp))
                Button(onClick = onRetryClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(stringResource(id = com.example.marsphotos.R.string.profile_error_back))
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
                semestre = 6,
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
