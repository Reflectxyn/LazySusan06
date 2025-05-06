package com.example.lazy_susan.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.AuthState
import com.example.lazy_susan.AuthViewModel
import com.example.lazy_susan.R

@Composable
fun ProfilePage(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel){

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(route = AppScreen.Profile.name)
            else -> Unit
        }
    }
    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(brush = SolidColor(Color.White), alpha = 0.8f)
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Box(contentAlignment = Alignment.TopCenter, modifier = modifier.fillMaxSize()) {
                Column {
                    Button(
                        onClick = {navController.navigate(route = AppScreen.PresetsPage.name)},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .height(40.dp)
                    ) {
                        Text(text = stringResource(R.string.presets_page))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {navController.navigate(route = AppScreen.BlockedPage.name)},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .height(40.dp)
                    ) {
                        Text(text = stringResource(R.string.blocked))
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
                        Text(text = stringResource(R.string.change_password))
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
                        Text(text = stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}