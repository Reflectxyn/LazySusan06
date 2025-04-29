package com.example.lazy_susan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase once at app start
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                LazySusanApp(
                    modifier = Modifier.padding(innerPadding),
                    authViewModel = authViewModel
                )
            }
        }
    }
}