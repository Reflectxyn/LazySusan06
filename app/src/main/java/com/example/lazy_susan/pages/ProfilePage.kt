package com.example.lazy_susan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ProfilePage(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel){

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(route = AppScreen.Profile.name)
            else -> Unit
        }
    }
    Column {
        Spacer(modifier = Modifier.height(72.dp))
        Box(contentAlignment = Alignment.TopCenter, modifier = modifier.fillMaxSize()) {
            Column {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(280.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Presets")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(280.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Blocked")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(route = AppScreen.ChangePassword.name) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(280.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Change Password")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { authViewModel.signout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(280.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Log Out")
                }
            }
        }
    }
}

