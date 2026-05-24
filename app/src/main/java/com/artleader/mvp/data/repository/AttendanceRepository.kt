package com.artleader.mvp.data.repository

import com.artleader.mvp.data.local.dao.AttendanceDao
import com.artleader.mvp.data.local.entity.AttendanceEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AttendanceRepository(private val dao: AttendanceDao) {
    fun history(): Flow<List<AttendanceEventEntity>> = dao.observeHistory()
    suspend fun record(tagId: String, employee: String, isEnter: Boolean) {
        dao.insert(AttendanceEventEntity(UUID.randomUUID().toString(), tagId, employee, if (isEnter) "ENTER" else "EXIT", System.currentTimeMillis()))
    }
}
