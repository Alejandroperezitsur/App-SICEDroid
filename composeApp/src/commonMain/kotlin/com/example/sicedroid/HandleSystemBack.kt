package com.example.sicedroid

import androidx.compose.runtime.Composable

@Composable
expect fun HandleSystemBack(enabled: Boolean, onBack: () -> Unit)
