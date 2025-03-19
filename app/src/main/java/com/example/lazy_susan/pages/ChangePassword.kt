package com.example.lazy_susan

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lazy_susan.AuthState
import com.example.lazy_susan.AuthViewModel

@Composable
fun ChangePassword(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel){

    var email by remember {
        mutableStateOf("")
    }


    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Change Password", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = email, onValueChange = {email = it}, label = { Text(text = "Email") })

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                navController.navigate("login")
            }){
                Text(text ="Cancel")
            }
            Button(onClick = {
                authViewModel.changePassword(email)
                navController.navigate(route = AppScreen.ProfileHome.name)
            })
            {
                Text(text = "Send")
            }

            Spacer(modifier = Modifier.height(8.dp))

        }
    }}