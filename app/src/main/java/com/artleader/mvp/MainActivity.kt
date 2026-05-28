package com.artleader.mvp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.artleader.mvp.data.local.db.AppDatabase
import com.artleader.mvp.data.preferences.SessionStore
import com.artleader.mvp.data.preferences.SettingsStore
import com.artleader.mvp.data.repository.AttendanceRepository
import com.artleader.mvp.data.repository.AuthRepository
import com.artleader.mvp.data.repository.BluetoothRepository
import com.artleader.mvp.ui.navigation.AppNavGraph
import com.artleader.mvp.ui.theme.ArtLeaderTheme
import com.artleader.mvp.viewmodel.AttendanceViewModel
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use applicationContext for DB to avoid activity leak
        val db             = AppDatabase.build(applicationContext)
        val settings       = SettingsStore(applicationContext)
        val sessionStore   = SessionStore(applicationContext)
        val authRepository = AuthRepository(db.userDao())

        // Safely obtain BluetoothAdapter via system service (API 18+)
        val btAdapter = (getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

        val btRepo = BluetoothRepository(
            com.artleader.mvp.bluetooth.BluetoothManager(btAdapter),
            db.messageDao(),
            db.conversationDao(),
            db.peerDao()
        )
        val attendanceRepo = AttendanceRepository(db.attendanceDao())

        setContent {
            val vm: MainViewModel = viewModel(
                factory = vmFactory { MainViewModel(authRepository, settings, sessionStore) }
            )
            val messengerVm: MessengerViewModel = viewModel(
                factory = vmFactory { MessengerViewModel(btRepo) }
            )
            val attendanceVm: AttendanceViewModel = viewModel(
                factory = vmFactory { AttendanceViewModel(attendanceRepo) }
            )
            ArtLeaderApp(vm, messengerVm, attendanceVm)
        }
    }
}

private inline fun <reified T : ViewModel> vmFactory(crossinline create: () -> T) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
    }

@Composable
private fun ArtLeaderApp(
    vm: MainViewModel,
    messengerVm: MessengerViewModel,
    attendanceVm: AttendanceViewModel
) {
    val settings by vm.settings.collectAsState()
    val navController = rememberNavController()
    ArtLeaderTheme(darkTheme = settings.darkTheme) {
        AppNavGraph(
            navController       = navController,
            vm                  = vm,
            messengerViewModel  = messengerVm,
            attendanceViewModel = attendanceVm
        )
    }
}