package com.xhf.leetcode.plugin.model

/**
 * 竞赛排名
 */
data class UserContestRanking(
    val attendedContestsCount: Int,
    val rating: Double,
    val globalRanking: Int,
    val localRanking: Int,
    val globalTotalParticipants: Int,
    val localTotalParticipants: Int,
    val topPercentage: Double
)