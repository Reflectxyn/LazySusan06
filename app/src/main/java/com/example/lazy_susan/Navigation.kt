package com.example.lazy_susan

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lazy_susan.ui.theme.HoneyMustardYellow

enum class AppScreen(@StringRes val title: Int) {
    Home(title = R.string.app_name)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazySusanAppBar(
    modifier: Modifier = Modifier,
    currentScreen: AppScreen
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 44.dp)
            ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = HoneyMustardYellow
        ),
        modifier = Modifier.height(100.dp)
    )
}

@Composable
fun LazySusanApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: AppScreen.Home.name
    )

    Scaffold(
        topBar = {
            Column {
                LazySusanAppBar(
                    currentScreen = currentScreen
                )
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = size.width, y = 0f),
                        strokeWidth = 10f
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreen.Home.name) {
                HomeScreen()
            }
        }
    }
}