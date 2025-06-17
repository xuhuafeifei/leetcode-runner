package com.xhf.leetcode.plugin.model

import com.intellij.openapi.project.Project
import com.xhf.leetcode.plugin.service.QuestionService
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


data class UserProgressQuestionList(
    val totalNum: Int,
    val questions: List<QuestionRecord>
)

/**
 * 提交问题记录
 */
data class QuestionRecord(
    val translatedTitle: String,
    val frontendId: String,
    val title: String,
    val titleSlug: String,
    val difficulty: String,
    val lastSubmittedAt: String?, // 可为 null
    val numSubmitted: Int,
    val questionStatus: String,
    val lastResult: String,
    val topicTags: List<TopicTag>
) {
    override fun toString(): String = toFormattedString()
}

// 辅助函数: 对应QuestionRecord和Question的转换
fun QuestionRecord.toQuestion(project: Project): Question {
    return QuestionService.getInstance(project).getQuestionByFid(frontendId, project)
}

fun QuestionRecord.formatRelativeTime(): String {
    if (lastSubmittedAt == null) return "未知"

    // 使用 DateTimeFormatter 来解析带有时区的时间字符串
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val submitted = try {
        ZonedDateTime.parse(lastSubmittedAt, formatter).toInstant()
    } catch (e: Exception) {
        // 如果带时区的解析失败，尝试直接解析为 Instant
        try {
            Instant.parse(lastSubmittedAt)
        } catch (e: Exception) {
            return "未知时间"
        }
    }

    val now = Instant.now()
    val hoursAgo = ChronoUnit.HOURS.between(submitted, now)

    return when {
        hoursAgo < 24 -> "$hoursAgo 小时前"
        hoursAgo < 48 -> "昨天"
        else -> {
            val date = submitted.atZone(ZoneId.systemDefault()).toLocalDate()
            "${date.year}-${date.monthValue}-${date.dayOfMonth}"
        }
    }
}


fun QuestionRecord.toFormattedString(): String {
    val relativeTime = formatRelativeTime()
    val titleWithId = "$frontendId. $translatedTitle"

    return String.format(
        "%-10s  %-50s  %-10s  %d",
        relativeTime,
        titleWithId,
        lastResult,
        numSubmitted
    )
}