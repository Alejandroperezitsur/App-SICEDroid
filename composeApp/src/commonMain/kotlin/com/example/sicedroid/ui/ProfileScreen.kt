package com.example.sicedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicedroid.model.ProfileStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: ProfileStudent?,
    onLogout: () -> Unit
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF11998e),
            Color(0xFF38ef7d)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Alumno", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (profile == null) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(32.dp))
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF11998e),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = profile.nombre.ifEmpty { "Alumno" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E)
                        )
                        Text(
                            text = profile.matricula,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        InfoRow(label = "Carrera", value = profile.carrera.ifEmpty { "Desconocida" })
                        InfoRow(label = "Semestre", value = profile.semestre.toString())
                        InfoRow(label = "Estatus", value = profile.estatusAlumno.ifEmpty { "INSCRITO" })
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        Text(text = value, color = Color(0xFF11998e), fontWeight = FontWeight.Bold)
    }
}
