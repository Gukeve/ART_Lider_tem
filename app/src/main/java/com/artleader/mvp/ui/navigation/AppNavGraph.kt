package com.artleader.mvp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.artleader.mvp.ui.screens.auth.LoginScreen
import com.artleader.mvp.ui.screens.auth.WelcomeScreen
import com.artleader.mvp.ui.screens.main.MainShell
import com.artleader.mvp.ui.screens.newemployee.NewEmployeeScreen
import com.artleader.mvp.viewmodel.AttendanceViewModel
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    vm: MainViewModel,
    messengerViewModel: MessengerViewModel,
    attendanceViewModel: AttendanceViewModel
) {
    val session by vm.session.collectAsState()

    LaunchedEffect(session.username, session.displayName) {
        messengerViewModel.updateIdentity(session.username, session.displayName)
    }

    // Determine start destination based on current session state.
    // MainViewModel.init calls lockOnAppLaunch() so on cold start isLoggedIn == false.
    // After login / biometric unlock isLoggedIn becomes true and nav reacts reactively.
    val start = if (session.isLoggedIn) "main" else "welcome"

    NavHost(navController = navController, startDestination = start) {

        composable("welcome") {
            WelcomeScreen(
                onLogin       = { navController.navigate("login") },
                onNewEmployee = { navController.navigate("new") },
                session       = session,
                onBiometric   = {
                    vm.unlockWithBiometric { ok ->
                        if (ok) {
                            navController.navigate("main") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                vm        = vm,
                onSuccess = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("new") {
            NewEmployeeScreen()
        }

        composable("main") {
            MainShell(vm, messengerViewModel, attendanceViewModel)
        }
    }
}