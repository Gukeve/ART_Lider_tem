package com.artleader.mvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.artleader.mvp.data.local.db.AppDatabase
import com.artleader.mvp.data.preferences.SettingsStore
import com.artleader.mvp.data.repository.AuthRepository
import com.artleader.mvp.ui.navigation.AppNavGraph
import com.artleader.mvp.ui.theme.ArtLeaderTheme
import com.artleader.mvp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.build(this)
        val settings = SettingsStore(this)
        val authRepository = AuthRepository(db.userDao())

        setContent {
            val vm: MainViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(authRepository, settings) as T
                }
            })
            ArtLeaderApp(vm)
        }
    }
}

@Composable
private fun ArtLeaderApp(vm: MainViewModel) {
    val settings by vm.settings.collectAsState()
    val navController = rememberNavController()
    ArtLeaderTheme(darkTheme = settings.darkTheme) {
        AppNavGraph(navController = navController, vm = vm)
    }
}
