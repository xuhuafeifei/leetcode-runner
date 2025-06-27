import com.intellij.ui.JBColor
import java.awt.*
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

fun parseJsonToMap(json: String): Map<Long, Int> {
    return json.removePrefix("{").removeSuffix("}")
        .split(",")
        .mapNotNull {
            val parts = it.split(":").map { p -> p.trim('"', ' ', '\n', '\r') }
            if (parts.size == 2) {
                val timestamp = parts[0].toLongOrNull()
                val count = parts[1].toIntOrNull()
                if (timestamp != null && count != null) timestamp to count else null
            } else null
        }.toMap()
}

data class DayInfo(val date: Date, val count: Int)

class CalendarContributionPanel(data: Map<Long, Int>) : JPanel() {
    private val cellSize = 15
    private val cellGap = 2
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val dayList: List<DayInfo>
    private val startDate: Calendar
    private val endDate: Calendar
    private val weeks: Int

    constructor(json: String) : this(parseJsonToMap(json)) {}

    init {
        toolTipText = ""
        ToolTipManager.sharedInstance().initialDelay = 200

        // 1. 计算起止范围
        val dates = data.keys.map { Date(it * 1000) }.sorted()
        startDate = Calendar.getInstance().apply {
            time = dates.firstOrNull() ?: Date()
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // 向前对齐到周日
        }
        endDate = Calendar.getInstance().apply {
            time = dates.lastOrNull() ?: Date()
        }

        // 2. 构建 dayList
        val tmp = mutableListOf<DayInfo>()
        val cursor = startDate.clone() as Calendar
        while (!cursor.after(endDate)) {
            val ts = cursor.time.time / 1000
            val count = data[ts] ?: 0
            tmp.add(DayInfo(cursor.time, count))
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        dayList = tmp
        weeks = (dayList.size + startDate.get(Calendar.DAY_OF_WEEK) - 1) / 7 + 1

        // 3. 设置组件尺寸
        val width = weeks * (cellSize + cellGap)
        val height = 7 * (cellSize + cellGap)
        preferredSize = Dimension(width, height)
    }

    override fun getToolTipText(e: MouseEvent): String? {
        val col = e.x / (cellSize + cellGap)
        val row = e.y / (cellSize + cellGap)
        val index = col * 7 + row
        return if (index in dayList.indices) {
            val d = dayList[index]
            "${dateFormat.format(d.date)}\nCommits: ${d.count}"
        } else null
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.color = Color.DARK_GRAY

        for ((i, day) in dayList.withIndex()) {
            val col = i / 7
            val row = i % 7
            val px = col * (cellSize + cellGap)
            val py = row * (cellSize + cellGap)
            g2.color = getColor(day.count)
            g2.fillRoundRect(px, py, cellSize, cellSize, 4, 4)
        }
    }

    private fun getColor(count: Int): Color = when {
        count == 0 -> JBColor(Color(210, 208, 208), Color(187, 186, 186))
        count < 2 -> Color(200, 255, 150)
        count < 6 -> Color(140, 210, 100)
        count < 11 -> Color(88, 163, 89)
        else -> Color(56, 120, 70)
    }
}
