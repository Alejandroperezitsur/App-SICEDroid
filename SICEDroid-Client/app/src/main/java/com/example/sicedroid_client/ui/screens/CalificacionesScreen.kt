package com.example.sicedroid_client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sicedroid_client.model.CalifFinal
import com.example.sicedroid_client.model.CalifUnidad

private val GradeExcellent = Color(0xFF2E7D32)
private val GradeGood = Color(0xFF689F38)
private val GradeAverage = Color(0xFFFBC02D)
private val GradePoor = Color(0xFFFF9800)
private val GradeFail = Color(0xFFE53935)

@Composable
fun CalificacionesScreen(
    califUnidad: List<CalifUnidad>,
    califFinal: List<CalifFinal>,
    isLoading: Boolean,
    hasPermission: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            !hasPermission -> PermissionRequiredMessage()
            califUnidad.isEmpty() && califFinal.isEmpty() && !isLoading ->
                EmptyDataMessage(message = "No hay calificaciones. Usa la pestaña Permisos para consultar.")
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (califFinal.isNotEmpty()) {
                        item {
                            Text(
                                text = "Calificaciones Finales",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                            )
                        }
                        items(califFinal) { calif -> CalifFinalCard(calif = calif) }
                    }

                    if (califUnidad.isNotEmpty()) {
                        item {
                            Text(
                                text = "Calificaciones por Unidad",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                            )
                        }
                        items(califUnidad) { calif -> CalifUnidadCard(calif = calif) }
                    }
                }
            }
        }
    }
}

@Composable
fun CalifFinalCard(calif: CalifFinal) {
    val gradeColor = getGradeColor(calif.calif.toFloat())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = calif.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (calif.estaAprobada) Icons.Default.CheckCircle else Icons.Default.Grade,
                        contentDescription = null,
                        tint = if (calif.estaAprobada) GradeExcellent else GradeFail,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (calif.estaAprobada) "Aprobada" else "No aprobada",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (calif.estaAprobada) GradeExcellent else GradeFail
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = gradeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = calif.calif.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = gradeColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CalifUnidadCard(calif: CalifUnidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = calif.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                calif.parciales.forEachIndexed { index, grade ->
                    ParcialGradeChip(unitNumber = index + 1, grade = grade)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Promedio:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = String.format("%.1f", calif.promedioParcial), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
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
        Text(text = "U$unitNumber", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getGradeColor(grade: Float): Color {
    return when {
        grade >= 90 -> GradeExcellent
        grade >= 80 -> GradeGood
        grade >= 70 -> GradeAverage
        grade >= 60 -> GradePoor
        else -> GradeFail
    }
}
