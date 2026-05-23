package com.artleader.mvp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.artleader.mvp.ui.screens.auth.LoginScreen
import com.artleader.mvp.ui.screens.auth.WelcomeScreen
import com.artleader.mvp.ui.screens.main.MainShell
import com.artleader.mvp.ui.screens.newemployee.NewEmployeeScreen
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

@Composable
fun AppNavGraph(navController: NavHostController, vm: MainViewModel, messengerViewModel: MessengerViewModel) {
    val session by vm.session.collectAsState()
    val start = if (session.isLoggedIn) "main" else "welcome"
    NavHost(navController = navController, startDestination = start) {
        composable("welcome") {
            WelcomeScreen(
                onLogin = { navController.navigate("login") },
                onNewEmployee = { navController.navigate("new") },
                session = session,
                onBiometric = { if (session.isLoggedIn) navController.navigate("main") }
            )
        }
        composable("login") { LoginScreen(vm = vm, onSuccess = { navController.navigate("main") }) }
        composable("new") { NewEmployeeScreen() }
        composable("main") { MainShell(vm, messengerViewModel) }
    }
}
