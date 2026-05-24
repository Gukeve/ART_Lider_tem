package com.artleader.mvp.ui.screens.main.profile

enum class WorkerRole { ADMIN, OPERATOR, DESIGNER, MANAGER }
enum class WorkerRank { TRAINEE, MIDDLE, SENIOR, MASTER }

data class WorkerPerformance(
    val rating: Double,
    val productionPercent: Int,
    val experienceStartDate: String,
    val achievements: List<String>
)
