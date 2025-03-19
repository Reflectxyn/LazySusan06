package com.example.lazy_susan

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lazy_susan.AuthState
import com.example.lazy_susan.AuthViewModel

@Composable
fun ProfilePage(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel){

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(route = AppScreen.Profile.name)
            else -> Unit
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        
    ){
        Text(text = "Accounts Page", fontSize = 32.sp)
        Button(onClick = {

        }) {
            Text(text = "Presets")
        }
        Button(onClick = {

        }) {
            Text(text = "Blocked Lists")
        }
        Button(onClick = {
            navController.navigate(route = AppScreen.ChangePassword.name)

        }) {
            Text(text = "Change Password")
        }
        Button(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign Out")
        }
    }


}

