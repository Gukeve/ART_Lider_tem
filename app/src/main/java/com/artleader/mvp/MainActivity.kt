package com.artleader.mvp

import android.bluetooth.BluetoothAdapter
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
import com.artleader.mvp.bluetooth.BluetoothManager
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
        val db = AppDatabase.build(this)
        val settings = SettingsStore(this)
        val authRepository = AuthRepository(db.userDao())
        val sessionStore = SessionStore(this)
        val btRepo = BluetoothRepository(BluetoothManager(BluetoothAdapter.getDefaultAdapter()), db.messageDao(), db.conversationDao(), db.peerDao())
        val attendanceRepo = AttendanceRepository(db.attendanceDao())

        setContent {
            val vm: MainViewModel = viewModel(factory = factory { MainViewModel(authRepository, settings, sessionStore) })
            val messengerVm: MessengerViewModel = viewModel(factory = factory { MessengerViewModel(btRepo) })
            val attendanceVm: AttendanceViewModel = viewModel(factory = factory { AttendanceViewModel(attendanceRepo) })
            ArtLeaderApp(vm, messengerVm, attendanceVm)
        }
    }
}

private inline fun <reified T : ViewModel> factory(crossinline create: () -> T) = object : ViewModelProvider.Factory {
    override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
}

@Composable
private fun ArtLeaderApp(vm: MainViewModel, messengerVm: MessengerViewModel, attendanceVm: AttendanceViewModel) {
    val settings by vm.settings.collectAsState()
    val navController = rememberNavController()
    ArtLeaderTheme(darkTheme = settings.darkTheme) {
        AppNavGraph(navController = navController, vm = vm, messengerViewModel = messengerVm, attendanceViewModel = attendanceVm)
    }
}
