package com.artleader.mvp.ui.navigation

import androidx.compose.runtime.Composable
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
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(
            onLogin = { navController.navigate("login") },
            onNewEmployee = { navController.navigate("new") }
        ) }
        composable("login") { LoginScreen(vm = vm, onSuccess = { navController.navigate("main") }) }
        composable("new") { NewEmployeeScreen() }
        composable("main") { MainShell(vm, messengerViewModel) }
    }
}
