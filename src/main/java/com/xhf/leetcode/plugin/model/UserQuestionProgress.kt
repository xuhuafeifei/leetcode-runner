package com.xhf.leetcode.plugin.model

data class UserQuestionProgress(
    val numAcceptedQuestions: List<QuestionCount>,
    val numFailedQuestions: List<QuestionCount>,
    val numUntouchedQuestions: List<QuestionCount>,
    val userSessionBeatsPercentage: List<UserSessionBeatsPercentage>,
    val totalQuestionBeatsPercentage: Double
)

data class QuestionCount(
    val count: Int,
    val difficulty: String
)

data class UserSessionBeatsPercentage(
    val difficulty: String,
    val percentage: Double
)