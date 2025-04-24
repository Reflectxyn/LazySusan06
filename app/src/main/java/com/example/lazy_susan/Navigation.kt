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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lazy_susan.pages.AwardsScreen
import com.example.lazy_susan.pages.FiltersScreen
import com.example.lazy_susan.pages.HomeScreen
import com.example.lazy_susan.pages.MapsScreen
import com.example.lazy_susan.pages.PresetPage
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class AppScreen(@StringRes val title: Int, @DrawableRes val icon: Int) {
    Featured(title = R.string.featured_page, icon = R.drawable.star),
    Home(title = R.string.app_name, icon = R.drawable.home),
    Filters(title = R.string.filters_page, icon = R.drawable.home),
    Maps(title = R.string.map_page, icon = R.drawable.home),
    Awards(title = R.string.app_name, icon = R.drawable.home),
    History(title = R.string.history_page, icon = R.drawable.history),
    Profile(title = R.string.accounts_page, icon = R.drawable.person),
    Signup(title = R.string.accounts_page, icon = R.drawable.person),
    ProfileHome(title = R.string.accounts_page, icon = R.drawable.person),
    ChangePassword(title = R.string.accounts_page, icon = R.drawable.person),
    PresetsPage(title = R.string.accounts_page, icon = R.drawable.person)
}

enum class TabPage(@StringRes val route: Int, @DrawableRes val icon: Int) {
    Featured(AppScreen.Featured.title, AppScreen.Featured.icon),
    Home(AppScreen.Home.title, AppScreen.Home.icon),
    History(AppScreen.History.title, AppScreen.History.icon),
    Profile(AppScreen.Profile.title, AppScreen.Profile.icon),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazySusanAppBar(currentScreen: Int) {
    val subject = when(currentScreen) {
        0 -> TabPage.Featured
        1 -> TabPage.Home
        2 -> TabPage.History
        3 -> TabPage.Profile
        else -> TabPage.Home
    }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(subject.route),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 24.dp)
            )
                },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = HoneyMustardYellow
        ),
        modifier = Modifier.height(108.dp)
    )
}

@Composable
fun LazySusanApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val pagerState = rememberPagerState(initialPage = 1) { TabPage.entries.size }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(pagerState.currentPage) }

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Scaffold(
        topBar = {
            Column {
                LazySusanAppBar(
                    currentScreen = pagerState.currentPage
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
            TabHome(
                selectedTabIndex = pagerState.currentPage,
                onSelectedTab = { scope.launch {
                    pagerState.scrollToPage(it.ordinal)
                } }
            )
        }
    ) { innerPadding ->
        Column {
            HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = route?.contains(AppScreen.Maps.name) == false,
                    modifier = Modifier.padding(innerPadding)
            ) { currentPage ->
                when (currentPage) {
                    0 -> FeaturedScreen(userId = "99UfGbCweDT62RBhY4Vyuf4czYf2")
                    1 -> HomeNav(navController)
                    2 -> HistoryScreen(userId = "99UfGbCweDT62RBhY4Vyuf4czYf2")
                    3 -> AccountNav(modifier, navController, authViewModel)
                }
            }

        }
    }
}

@Composable
fun TabHome(selectedTabIndex: Int, onSelectedTab:(TabPage) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        indicator = { TabIndicator(it, selectedTabIndex) }
    ) {
        TabPage.entries.forEachIndexed { index, tabPage ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = {
                    onSelectedTab(tabPage)
                },
                modifier = Modifier
                    .size(98.dp)
                    .border(width = 1.dp, color = Color.Black)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(tabPage.icon),
                    contentDescription = stringResource(tabPage.route),
                    tint = Color.Black,
                    modifier = Modifier.size(69.dp)
                )
            }
        }
    }
}

@Composable
fun TabIndicator(tabPosition: List<TabPosition>, index: Int) {
    val width = tabPosition[index].width
    val offsetX = tabPosition[index].left
    val subject = when(index) {
        0 -> TabPage.Featured
        1 -> TabPage.Home
        2 -> TabPage.History
        3 -> TabPage.Profile
        else -> TabPage.Home
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.BottomStart)
            .offset(x = offsetX)
            .width(width)
            .fillMaxSize()
            .background(color = PicnicTableRed)
            .border(width = 1.dp, color = Color.Black)
    ) {
        Icon(
            painter = painterResource(subject.icon),
            contentDescription = stringResource(subject.route),
            tint = Color.Black,
            modifier = Modifier.size(69.dp)
        )
    }
}

@Composable
fun HomeNav(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = AppScreen.Home.name) {
            HomeScreen(navController)
        }
        composable(route = AppScreen.Filters.name) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            FiltersScreen(navController, userId = userId)
        }
        composable(
            route = "${AppScreen.Maps.name}/{radiusInMiles}",
            arguments = listOf(navArgument("radiusInMiles") { type = NavType.IntType })
        ) { backStackEntry ->
            val radiusInMiles = backStackEntry.arguments?.getInt("radiusInMiles") ?: 2
            MapsScreen(radiusInMiles)
        }
        composable(route = AppScreen.Awards.name){
            AwardsScreen()
        }
    }
}

@Composable
fun AccountNav(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val startDestination =
        if(authViewModel.authState.observeAsState().value == AuthState.Authenticated) {
            AppScreen.ProfileHome.name
        } else {
            AppScreen.Profile.name
        }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = AppScreen.Profile.name) {
            LoginPage(navController, authViewModel)
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
        composable(route = AppScreen.PresetsPage.name) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PresetPage(userId = userId, navController)
        }
        composable(route = AppScreen.Filters.name) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            FiltersScreen(navController, userId = userId)
        }
        composable(
            route = "${AppScreen.Filters.name}/{presetId}",
            arguments = listOf(navArgument("presetId") { defaultValue = "" })
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getString("presetId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            FiltersScreen(navController = navController, userId = userId, presetId = presetId)
        }
        composable(
            route = "${AppScreen.Maps.name}/{radiusInMiles}",
            arguments = listOf(navArgument("radiusInMiles") { type = NavType.IntType })
        ) { backStackEntry ->
            val radiusInMiles = backStackEntry.arguments?.getInt("radiusInMiles") ?: 2
            MapsScreen(radiusInMiles)
        }
    }
}