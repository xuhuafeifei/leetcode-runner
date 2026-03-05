package com.xhf.leetcode.plugin.model

data class UserCalendar(
    val streak: Int,
    val totalActiveDays: Int,
    val submissionCalendar: String,
    val activeYears: List<Int>,
    val monthlyMedals: List<MonthlyMedal>,
    val recentStreak: Int
)

data class MonthlyMedal(
    val name: String,
    val obtainDate: String, // 可转为 ZonedDateTime 或 Date
    val category: String,
    val config: MedalConfig,
    val progress: Any?, // 通常为 null
    val id: Int,
    val year: Int,
    val month: Int
)

data class MedalConfig(
    val icon: String,
    val iconGif: String,
    val iconGifBackground: String
)