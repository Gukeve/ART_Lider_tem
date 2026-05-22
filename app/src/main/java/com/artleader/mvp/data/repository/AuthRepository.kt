package com.artleader.mvp.data.repository

import com.artleader.mvp.data.local.dao.UserDao
import com.artleader.mvp.data.local.entity.UserEntity

class AuthRepository(private val userDao: UserDao) {
    private val seed = listOf(
        UserEntity("admin", "admin123", "Администратор", "Admin", "1992-05-18"),
        UserEntity("designer", "des123", "Дизайнер", "Designer"),
        UserEntity("operator", "op123", "Оператор", "Operator"),
        UserEntity("manager", "man123", "Менеджер", "Manager")
    )

    suspend fun ensureSeed() = userDao.upsertAll(seed)
    suspend fun login(login: String, password: String) = userDao.login(login, password)
}
