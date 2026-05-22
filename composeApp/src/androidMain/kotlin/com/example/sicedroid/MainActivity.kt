package com.example.sicedroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sicedroid.db.AndroidDriverFactory
import com.example.sicedroid.db.LocalDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataSource = LocalDataSource(AndroidDriverFactory(applicationContext))
        setContent {
            App(localDataSource = dataSource)
        }
    }
}
