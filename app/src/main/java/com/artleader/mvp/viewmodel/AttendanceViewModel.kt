package com.artleader.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repo: AttendanceRepository) : ViewModel() {
    val history = repo.history().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun onNfcTagScanned(tagId: String, employee: String, isEnter: Boolean) = viewModelScope.launch { repo.record(tagId, employee, isEnter) }
}
