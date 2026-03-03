package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.ui.theme.SICENETTheme
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LastUpdateLabel(timestamp: Long, isOffline: Boolean = false, modifier: Modifier = Modifier) {
    if (timestamp > 0 || isOffline) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = if (timestamp > 0) sdf.format(Date(timestamp)) else "Sin fecha"
        
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isOffline) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOffline) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(end = 6.dp))
                }
                Text(
                    text = if (isOffline) "Modo offline - Datos de: $dateStr" else "Última actualización: $dateStr",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOffline) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profileUiState: ProfileUiState,
    onLogoutClick: () -> Unit,
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
            title = { 
                Text(
                    text = stringResource(id = com.example.marsphotos.R.string.profile_screen_title),
                    color = MaterialTheme.colorScheme.onPrimary
                ) 
            },
            actions = {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        when (profileUiState) {
            is ProfileUiState.Loading -> {
                LoadingProfileScreen(modifier = Modifier.fillMaxSize())
            }
            is ProfileUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    LastUpdateLabel(timestamp = profileUiState.lastUpdate, isOffline = profileUiState.isOffline)
                    ProfileDetailScreen(
                        profile = profileUiState.profile,
                        onKardexClick = onKardexClick,
                        onCargaClick = onCargaClick,
                        onGradesClick = onGradesClick,
                        onLogoutClick = onLogoutClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            is ProfileUiState.Error -> {
                ProfileErrorScreen(
                    error = profileUiState.message,
                    onRetryClick = onLogoutClick,
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
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Card de Información Personal con diseño moderno
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(id = com.example.marsphotos.R.string.profile_section_personal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.fotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profile.fotoUrl,
                            contentDescription = "Foto",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.padding(end = 16.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileInfoRow(
                            label = stringResource(id = com.example.marsphotos.R.string.profile_label_matricula),
                            value = profile.matricula,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val fullName = if (profile.apellidos.isNotEmpty()) "${profile.nombre} ${profile.apellidos}" else profile.nombre
                        ProfileInfoRow(
                            label = stringResource(id = com.example.marsphotos.R.string.profile_label_nombre),
                            value = fullName,
                            isHighlighted = true
                        )
                    }
                }
            }
        }

        // Card de Información Académica
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(id = com.example.marsphotos.R.string.profile_section_academic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_carrera),
                    value = profile.carrera
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_estatus_academico),
                    value = profile.estatusAcademico
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_especialidad),
                    value = profile.especialidad
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_semestre),
                    value = profile.semestre.toString(),
                    isHighlighted = true
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_promedio),
                    value = profile.promedio,
                    isHighlighted = true
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_estado),
                    value = profile.estado
                )
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_cdts_reunidos),
                    value = profile.cdtsReunidos.toString()
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_cdts_actuales),
                    value = profile.cdtsActuales.toString()
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_inscrito),
                    value = profile.inscrito
                )
                ProfileInfoRow(
                    label = stringResource(id = com.example.marsphotos.R.string.profile_label_reinscripcion),
                    value = profile.reinscripcionFecha
                )
                
                if (profile.sinAdeudos.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = profile.sinAdeudos,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Card de Operaciones Académicas con botones mejorados
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(id = com.example.marsphotos.R.string.profile_section_operaciones),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                // Botones de operaciones con iconos
                OperationButton(
                    title = "KARDEX",
                    subtitle = "Consultar historial académico",
                    icon = Icons.Filled.Assignment,
                    onClick = onKardexClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                
                OperationButton(
                    title = "CARGA ACADÉMICA",
                    subtitle = "Ver materias inscritas",
                    icon = Icons.Filled.MenuBook,
                    onClick = onCargaClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
                
                OperationButton(
                    title = "CALIFICACIONES",
                    subtitle = "Parciales y finales",
                    icon = Icons.Filled.Grade,
                    onClick = onGradesClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
        
        // Botón de cerrar sesión al final
        FilledTonalButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = stringResource(id = com.example.marsphotos.R.string.profile_operations_icon_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LoadingProfileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(id = com.example.marsphotos.R.string.profile_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(id = com.example.marsphotos.R.string.login_error_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
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
                nombre = "Juan Carlos",
                apellidos = "Pérez García",
                carrera = "Ingeniería en Sistemas Computacionales",
                semestre = 6,
                promedio = "8.5",
                estado = "Activo",
                statusMatricula = "Vigente"
            ),
            onKardexClick = {},
            onCargaClick = {},
            onGradesClick = {},
            onLogoutClick = {}
        )
    }
}
