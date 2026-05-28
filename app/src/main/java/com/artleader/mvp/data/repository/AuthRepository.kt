package com.artleader.mvp.data.repository

import com.artleader.mvp.data.local.dao.UserDao
import com.artleader.mvp.data.local.entity.UserEntity

class AuthRepository(private val userDao: UserDao) {
    private val seed = listOf(
        UserEntity("yulia", "1234", "Юля", "Operator"),
        UserEntity("artem", "1234", "Артем", "Manager"),
        UserEntity("viktor", "1234", "Виктор", "Operator"),
        UserEntity("danil", "1234", "Данил", "Operator"),
        UserEntity("sergey", "1234", "Сергей", "Operator"),
        UserEntity("vitalik", "1234", "Виталик", "Admin"),
        UserEntity("dima1", "1234", "Дима1", "Operator"),
        UserEntity("dima2", "1234", "Дима2", "Operator"),
        UserEntity("dima3", "1234", "Дима3", "Operator"),
        UserEntity("sanya", "1234", "Саня", "Operator"),
        UserEntity("gena", "1234", "Гена", "Operator")
    )

    suspend fun ensureSeed() = userDao.upsertAll(seed)
    suspend fun login(login: String, password: String) = userDao.login(login, password)
}
suspend fun getUser(login: String): UserEntity? {
    return userDao.getUser(login)
}

suspend fun updateAvatar(login: String, avatar: String) {
    userDao.updateAvatar(login, avatar)
}