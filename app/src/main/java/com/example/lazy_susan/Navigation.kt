package com.example.lazy_susan

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lazy_susan.pages.HomeScreen
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed

enum class AppScreen(@StringRes val title: Int, @DrawableRes val icon: Int) {
    Featured(title = R.string.featured_page, icon = R.drawable.star),
    Home(title = R.string.app_name, icon = R.drawable.home),
    Filters(title = R.string.filters_page, icon = R.drawable.home),
    Stats(title = R.string.app_name, icon = R.drawable.home),
    History(title = R.string.history_page, icon = R.drawable.history),
    Profile(title = R.string.accounts_page, icon = R.drawable.person),
    Signup(title = R.string.accounts_page, icon = R.drawable.person),
    ProfileHome(title = R.string.accounts_page, icon = R.drawable.person),
    ChangePassword(title = R.string.accounts_page, icon = R.drawable.person)

}

val topLevelRoutes = listOf(
    TopLevelRoute(AppScreen.Featured.name, AppScreen.Featured.icon, Color.White),
    TopLevelRoute(AppScreen.Home.name, AppScreen.Home.icon, PicnicTableRed),
    TopLevelRoute(AppScreen.History.name, AppScreen.History.icon, Color.White),
    TopLevelRoute(AppScreen.Profile.name, AppScreen.Profile.icon, Color.White),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazySusanAppBar(currentScreen: AppScreen) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 4.dp)
            ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = HoneyMustardYellow
        ),
        modifier = Modifier.height(108.dp)
    )
}

@Composable
fun LazySusanNavBar(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BottomNavigation {
        val currentDestination = navBackStackEntry.destination
        topLevelRoutes.forEach { topLevelRoute ->
            BottomNavigationItem(
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(topLevelRoute.background_color)
                    ) {
                        Icon(
                            painter = painterResource(topLevelRoute.icon),
                            contentDescription = topLevelRoute.route,
                            tint = Color.Black,
                            modifier = Modifier.size(69.dp)
                        )
                    }
                       },
                selected = currentDestination.hierarchy.any { it.hasRoute(topLevelRoute.route::class) } == true,
                onClick = {
                    topLevelRoutes.forEach { topLevelRoute ->
                        topLevelRoute.setBackgroundColor(Color.White)
                    }
                    topLevelRoute.setBackgroundColor(PicnicTableRed)
                    navController.navigate(topLevelRoute.route)
                },
                modifier = Modifier
                    .size(98.dp)
                    .border(width = 1.dp, color = Color.Black)
            )
        }
    }
}

@Composable
fun LazySusanApp(
    modifier: Modifier = Modifier, authViewModel: AuthViewModel,
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
        },
        bottomBar = {
            backStackEntry?.let {
                LazySusanNavBar(
                    navController = navController,
                    navBackStackEntry = it
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreen.Featured.name) {
                FeaturedScreen(userId = "99UfGbCweDT62RBhY4Vyuf4czYf2")

            }
            composable(route = AppScreen.Home.name) {
                HomeScreen(modifier, navController)
            }
            composable(route = AppScreen.Filters.name) {

            }
            composable(route = AppScreen.Stats.name) {

            }
            composable(route = AppScreen.History.name) {
                HistoryScreen(userId = "99UfGbCweDT62RBhY4Vyuf4czYf2")

            }
            composable(route = AppScreen.Profile.name) {
                LoginPage(modifier, navController, authViewModel)
            }
            composable(route = AppScreen.Signup.name){
                SignupPage(modifier, navController, authViewModel)
            }
            composable(route = AppScreen.ChangePassword.name){
                ChangePassword(modifier, navController, authViewModel)
            }
            composable(route = AppScreen.ProfileHome.name){
                ProfilePage(modifier, navController, authViewModel)
            }
        }
    }
}