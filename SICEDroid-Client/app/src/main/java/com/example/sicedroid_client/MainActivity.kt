package com.example.sicedroid_client

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sicedroid_client.ui.screens.HomeScreen
import com.example.sicedroid_client.ui.theme.SICEDroidClientTheme
import com.example.sicedroid_client.viewmodel.AcademicViewModel

/**
 * MainActivity de la aplicación cliente SICEDroid.
 * Implementa la solicitud de permisos en tiempo de ejecución para acceder al Content Provider.
 */
class MainActivity : ComponentActivity() {

    // Launcher para solicitar permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions["com.example.marsphotos.provider.READ"] ?: false
        val writeGranted = permissions["com.example.marsphotos.provider.WRITE"] ?: false

        if (readGranted) {
            Toast.makeText(this, "Permiso de lectura concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de lectura denegado. La app no podrá consultar datos.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar permisos al iniciar la aplicación
        requestProviderPermissions()

        setContent {
            SICEDroidClientTheme {
                val viewModel: AcademicViewModel = viewModel()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPermissions = {
                            // Si el usuario vuelve a la pestaña de permisos, podemos re-verificar
                            requestProviderPermissions()
                        }
                    )
                }
            }
        }
    }

    private fun requestProviderPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Permisos personalizados de SICEDroid
        permissionsToRequest.add("com.example.marsphotos.provider.READ")
        permissionsToRequest.add("com.example.marsphotos.provider.WRITE")
        
        // En Android 13+ (API 33+) también se podría necesitar POST_NOTIFICATIONS si el cliente las usa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}
