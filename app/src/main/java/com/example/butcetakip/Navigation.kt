package com.example.butcetakip

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.butcetakip.ui.add.AddTransactionScreen
import com.example.butcetakip.ui.auth.AuthScreen
import com.example.butcetakip.ui.dashboard.DashboardScreen
import com.example.butcetakip.ui.list.TransactionListScreen
import com.example.butcetakip.ui.main.FinanceViewModel
import com.example.butcetakip.ui.settings.SettingsScreen
import com.example.butcetakip.ui.settings.ProfileScreen
import com.example.butcetakip.ui.stats.StatisticsScreen
import com.example.butcetakip.ui.ai.AiChatScreen
import com.example.butcetakip.ui.ai.AiChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.butcetakip.ui.components.GlassBottomNavBar
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Oturum kontrolü: Firebase'de kullanıcı var mı?
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) NavigationKeys.DASHBOARD else NavigationKeys.AUTH

    val bottomBarDestinations = listOf(
        NavigationKeys.DASHBOARD,
        NavigationKeys.STATISTICS,
        NavigationKeys.SETTINGS
    )
    val showBottomBar = currentDestination?.route in bottomBarDestinations

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { -50 }, animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { -50 }, animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { 50 }, animationSpec = tween(300)) }
        ) {
            composable(NavigationKeys.AUTH) {
                AuthScreen(
                    onAuthSuccess = {
                        // Firestore'dan verileri yükle
                        com.example.butcetakip.data.DataRepository.syncFromFirestore()
                        navController.navigate(NavigationKeys.DASHBOARD) {
                            popUpTo(NavigationKeys.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavigationKeys.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(NavigationKeys.ADD_TRANSACTION) },
                    onSeeAllClick = { navController.navigate(NavigationKeys.TRANSACTION_LIST) },
                    onAiClick = { navController.navigate(NavigationKeys.AI_CHAT) }
                )
            }
            composable(NavigationKeys.STATISTICS) {
                StatisticsScreen(viewModel = viewModel)
            }
            composable(NavigationKeys.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onProfileClick = { navController.navigate(NavigationKeys.PROFILE_SETTINGS) },
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(NavigationKeys.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavigationKeys.PROFILE_SETTINGS) {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavigationKeys.ADD_TRANSACTION) {
                AddTransactionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavigationKeys.TRANSACTION_LIST) {
                TransactionListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavigationKeys.AI_CHAT) {
                val aiViewModel: AiChatViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                AiChatScreen(
                    viewModel = aiViewModel,
                    transactions = state.allTransactions,
                    onAddTransaction = { viewModel.addTransaction(it) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = {
                        navController.navigate(NavigationKeys.SETTINGS) {
                            popUpTo(NavigationKeys.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        
        if (showBottomBar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                GlassBottomNavBar(
                    currentRoute = currentDestination?.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
