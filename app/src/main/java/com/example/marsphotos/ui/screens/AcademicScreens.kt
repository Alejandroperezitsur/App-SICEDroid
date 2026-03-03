package com.example.marsphotos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaParcial
import com.example.marsphotos.ui.theme.GradeAverage
import com.example.marsphotos.ui.theme.GradeExcellent
import com.example.marsphotos.ui.theme.GradeFail
import com.example.marsphotos.ui.theme.GradeGood
import com.example.marsphotos.ui.theme.GradePoor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================
// PANTALLA KARDEX
// ============================================
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
            is AcademicUiState.Loading -> ModernLoadingScreen()
            is AcademicUiState.Error -> ModernErrorScreen(message = state.message)
            is AcademicUiState.Success -> {
                Column {
                    ModernLastUpdateLabel(timestamp = state.lastUpdate, isOffline = state.isOffline)
                    ModernKardexList(kardex = state.data)
                }
            }
        }
    }
}

// ============================================
// PANTALLA CARGA ACADÉMICA
// ============================================
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
            is AcademicUiState.Loading -> ModernLoadingScreen()
            is AcademicUiState.Error -> ModernErrorScreen(message = state.message)
            is AcademicUiState.Success -> {
                Column {
                    ModernLastUpdateLabel(timestamp = state.lastUpdate, isOffline = state.isOffline)
                    ModernCargaList(carga = state.data)
                }
            }
        }
    }
}

// ============================================
// PANTALLA CALIFICACIONES
// ============================================
@Composable
fun GradesScreen(
    viewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val parcialesState by viewModel.parcialesState.collectAsState()
    val finalesState by viewModel.finalesState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(id = com.example.marsphotos.R.string.grades_tab_parciales),
        stringResource(id = com.example.marsphotos.R.string.grades_tab_finales)
    )

    LaunchedEffect(Unit) {
        viewModel.loadGrades()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TabRow moderno
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                when (val state = parcialesState) {
                    is AcademicUiState.Loading -> ModernLoadingScreen()
                    is AcademicUiState.Error -> ModernErrorScreen(message = state.message)
                    is AcademicUiState.Success -> {
                        Column {
                            ModernLastUpdateLabel(timestamp = state.lastUpdate, isOffline = state.isOffline)
                            ModernParcialesList(parciales = state.data)
                        }
                    }
                }
            }
            1 -> {
                when (val state = finalesState) {
                    is AcademicUiState.Loading -> ModernLoadingScreen()
                    is AcademicUiState.Error -> ModernErrorScreen(message = state.message)
                    is AcademicUiState.Success -> {
                        Column {
                            ModernLastUpdateLabel(timestamp = state.lastUpdate, isOffline = state.isOffline)
                            ModernFinalesList(finales = state.data)
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// COMPONENTES MODERNOS REUTILIZABLES
// ============================================

@Composable
fun ModernLastUpdateLabel(timestamp: Long, isOffline: Boolean = false, modifier: Modifier = Modifier) {
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
fun ModernLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "Cargando datos...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernErrorScreen(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ============================================
// LISTAS MODERNIZADAS
// ============================================

@Composable
fun ModernKardexList(kardex: List<MateriaKardex>, modifier: Modifier = Modifier) {
    if (kardex.isEmpty()) {
        ModernEmptyState(
            message = stringResource(id = com.example.marsphotos.R.string.empty_kardex),
            icon = Icons.Filled.School
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(kardex) { materia ->
                ModernKardexCard(materia = materia)
            }
        }
    }
}

@Composable
fun ModernKardexCard(materia: MateriaKardex) {
    val califString = materia.calificacion.toString()
    val califColor = getGradeColor(califString)
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = materia.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = califColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = califString,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = califColor,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(icon = Icons.Filled.School, text = "Clave: ${materia.clave}")
                InfoChip(icon = Icons.Filled.CalendarToday, text = materia.periodo)
            }
            
            Text(
                text = "Acreditación: ${materia.acreditacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernCargaList(carga: List<MateriaCarga>, modifier: Modifier = Modifier) {
    if (carga.isEmpty()) {
        ModernEmptyState(
            message = stringResource(id = com.example.marsphotos.R.string.empty_carga),
            icon = Icons.Filled.Schedule
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(carga) { materia ->
                ModernCargaCard(materia = materia)
            }
        }
    }
}

@Composable
fun ModernCargaCard(materia: MateriaCarga) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título de la materia
            Text(
                text = materia.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Docente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = materia.docente,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Horarios
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (materia.lunes.isNotBlank()) DayScheduleRow("Lunes", materia.lunes)
                if (materia.martes.isNotBlank()) DayScheduleRow("Martes", materia.martes)
                if (materia.miercoles.isNotBlank()) DayScheduleRow("Miércoles", materia.miercoles)
                if (materia.jueves.isNotBlank()) DayScheduleRow("Jueves", materia.jueves)
                if (materia.viernes.isNotBlank()) DayScheduleRow("Viernes", materia.viernes)
                if (materia.sabado.isNotBlank()) DayScheduleRow("Sábado", materia.sabado)
            }
            
            // Grupo y créditos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Grupo: ${materia.grupo} • ${materia.creditos} créditos",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DayScheduleRow(day: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernParcialesList(parciales: List<MateriaParcial>, modifier: Modifier = Modifier) {
    if (parciales.isEmpty()) {
        ModernEmptyState(
            message = stringResource(id = com.example.marsphotos.R.string.empty_parciales),
            icon = Icons.Filled.Star
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(parciales) { materia ->
                ModernParcialCard(materia = materia)
            }
        }
    }
}

@Composable
fun ModernParcialCard(materia: MateriaParcial) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = materia.materia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Calificaciones en fila horizontal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                materia.parciales.forEachIndexed { index, calif ->
                    ParcialGradeChip(
                        unitNumber = index + 1,
                        grade = calif
                    )
                }
            }
        }
    }
}

@Composable
fun ParcialGradeChip(unitNumber: Int, grade: String) {
    val califValue = grade.toFloatOrNull() ?: 0f
    val color = when {
        califValue >= 90 -> GradeExcellent
        califValue >= 80 -> GradeGood
        califValue >= 70 -> GradeAverage
        califValue >= 60 -> GradePoor
        else -> GradeFail
    }
    
    val isZero = grade == "0" || grade.isEmpty()
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isZero) MaterialTheme.colorScheme.surfaceVariant else color.copy(alpha = 0.15f),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isZero) "-" else grade,
                    fontWeight = FontWeight.Bold,
                    color = if (isZero) MaterialTheme.colorScheme.onSurfaceVariant else color,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(modifier = Modifier.padding(top = 4.dp))
        Text(
            text = "U$unitNumber",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernFinalesList(finales: List<MateriaFinal>, modifier: Modifier = Modifier) {
    if (finales.isEmpty()) {
        ModernEmptyState(
            message = stringResource(id = com.example.marsphotos.R.string.empty_finales),
            icon = Icons.Filled.Star
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(finales) { materia ->
                ModernFinalCard(materia = materia)
            }
        }
    }
}

@Composable
fun ModernFinalCard(materia: MateriaFinal) {
    val color = getGradeColor(materia.calif.toString())
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
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
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = materia.calif.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
        }
    }
}

// ============================================
// COMPONENTES AUXILIARES
// ============================================

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.padding(end = 4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernEmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Función para obtener color según calificación
fun getGradeColor(grade: String): Color {
    val calif = grade.toFloatOrNull() ?: 0f
    return when {
        calif >= 90 -> GradeExcellent
        calif >= 80 -> GradeGood
        calif >= 70 -> GradeAverage
        calif >= 60 -> GradePoor
        else -> GradeFail
    }
}
