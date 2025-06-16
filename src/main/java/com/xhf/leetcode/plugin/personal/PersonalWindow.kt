package com.xhf.leetcode.plugin.personal

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.*
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBScrollBar
import com.intellij.ui.components.JBScrollPane
import com.xhf.leetcode.plugin.actions.utils.ActionUtils
import com.xhf.leetcode.plugin.io.http.LeetcodeClient
import com.xhf.leetcode.plugin.model.*
import com.xhf.leetcode.plugin.utils.BundleUtils
import com.xhf.leetcode.plugin.utils.TaskCenter
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*

import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class QuestionTableModel(private val data: List<QuestionRecord>) : AbstractTableModel() {

    private val columnNames = arrayOf("编号 & 题目", "难度", "最近提交", "提交次数")

    override fun getRowCount(): Int = data.size
    override fun getColumnCount(): Int = columnNames.size

    override fun getColumnName(column: Int): String = columnNames[column]

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val q = data[rowIndex]
        return when (columnIndex) {
            0 -> "${q.frontendId}. ${q.translatedTitle}"
            1 -> q.difficulty
            2 -> q.formatRelativeTime()
            3 -> q.numSubmitted
            else -> null
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0, 1, 2 -> String::class.java
            3 -> Int::class.java
            else -> Object::class.java
        }
    }

    override fun isCellEditable(p0: Int, p1: Int): Boolean = false
}

// 辅助函数：数字加千分位
fun Int.toLocaleString(): String {
    return this.toString().reversed().chunked(3).joinToString(",").reversed()
}

// 辅助函数：格式化竞赛排名
fun UserContestRanking.formatGlobalRank(): String {
    return "${globalRanking.toLocaleString()} / ${globalTotalParticipants.toLocaleString()}"
}

fun UserContestRanking.formatLocalRank(): String {
    return "${localRanking.toLocaleString()} / ${localTotalParticipants.toLocaleString()}"
}

fun UserQuestionProgress.getEasyTotalCount(): Int {
    return numAcceptedQuestions.first { it.difficulty == "EASY" }.count + numFailedQuestions.first { it.difficulty == "EASY" }.count + numUntouchedQuestions.first { it.difficulty == "EASY" }.count
}

fun UserQuestionProgress.getMediumTotalCount(): Int {
    return numAcceptedQuestions.first { it.difficulty == "MEDIUM" }.count + numFailedQuestions.first { it.difficulty == "MEDIUM" }.count + numUntouchedQuestions.first { it.difficulty == "MEDIUM" }.count
}

fun UserQuestionProgress.getHardTotalCount(): Int {
    return numAcceptedQuestions.first { it.difficulty == "HARD" }.count + numFailedQuestions.first { it.difficulty == "HARD" }.count + numUntouchedQuestions.first { it.difficulty == "HARD" }.count
}

// 辅助函数, 获取题目总数
fun UserQuestionProgress.getTotalQuestions(): Int {
    return numAcceptedQuestions.sumOf { it.count } + numFailedQuestions.sumOf { it.count } + numUntouchedQuestions.sumOf { it.count }
}

// 辅助函数: 格式化做题进度
fun UserQuestionProgress.formatEasyProgress(): String {
    return numAcceptedQuestions.first { it.difficulty == "EASY" }.let { "${it.count} / ${getEasyTotalCount()}" }
}

fun UserQuestionProgress.formatMediumProgress(): String {
    return numAcceptedQuestions.first { it.difficulty == "MEDIUM" }.let { "${it.count} / ${getMediumTotalCount()}" }
}

fun UserQuestionProgress.formatHardProgress(): String {
    return numAcceptedQuestions.first { it.difficulty == "HARD" }.let { "${it.count} / ${getHardTotalCount()}" }
}

class PersonalWindow(project: Project) : JFrame() {


    private val client = LeetcodeClient.getInstance(project)

    // 延迟初始化，先不做耗时请求
    private lateinit var userProfile: LeetcodeUserProfile
    private lateinit var userStatus: UserStatus
    private lateinit var userQuestionProgress: UserQuestionProgress
    private lateinit var userContestRanks: UserContestRanking
    private lateinit var userProgressQuestions: UserProgressQuestionList

    private val contentPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = JBColor.PanelBackground
        border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
    }

    private val loadingPanel = JBLoadingPanel(BorderLayout(), project)

    init {
        title = BundleUtils.i18nHelper("个人中心", "Personal Center")
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        size = Dimension(500, 650)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                ActionUtils.disposePersonalWindow()
            }
        })

        contentPanel.add(loadingPanel, BorderLayout.CENTER)
        add(JBScrollPane(contentPanel), BorderLayout.CENTER)
        isVisible = true

        loadDataAsync()
    }

    private fun loadDataAsync() {
        loadingPanel.startLoading()

        TaskCenter.getInstance().createTask {
            val userProfileFuture = TaskCenter.getInstance().createFutureTask {
                userProfile = client.queryUserProfile()
                userStatus = client.queryUserStatus()
            }

            val userStatsFuture = TaskCenter.getInstance().createFutureTask {
                userQuestionProgress = client.queryUserQuestionProgress()
                userContestRanks = client.queryUserContestRanking()
                userProgressQuestions = client.queryUserProgressQuestionList()
            }

            userProfileFuture.invokeAndGet()
            userStatsFuture.invokeAndGet()

            SwingUtilities.invokeLater {
                updateUIAfterDataLoaded()
            }
        }.invokeLater()
    }

    private fun updateUIAfterDataLoaded() {
        contentPanel.removeAll()

        contentPanel.add(createCircularImagePanel())
        contentPanel.add(Box.createVerticalStrut(20))
        contentPanel.add(createStatsPanel())
        contentPanel.add(Box.createVerticalStrut(20))
        contentPanel.add(createTablePanel())

        contentPanel.revalidate()
        contentPanel.repaint()
        loadingPanel.stopLoading()
    }

    private fun createCircularImagePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = JBColor.PanelBackground
        panel.border = BorderFactory.createEmptyBorder(20, 0, 20, 0)

        // 头像
        val imageLabel = CircularImageLabel().apply {
            preferredSize = Dimension(150, 150)
            maximumSize = Dimension(150, 150)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        // 异步加载图片
        TaskCenter.getInstance().createTask {
            try {
                val imageUrl = URL(userProfile.userAvatar)
                val image = ImageIO.read(imageUrl)?.getScaledInstance(150, 150, Image.SCALE_SMOOTH)

                SwingUtilities.invokeLater {
                    imageLabel.setImage(image)
                    imageLabel.repaint()
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    imageLabel.setImage(createDefaultAvatar())
                    imageLabel.repaint()
                }
            }
        }.invokeLater()

        panel.add(imageLabel)
        // 头像与用户名之间间隔
        panel.add(Box.createVerticalStrut(10))

        // 用户名与图标
        val namePanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)).apply {
            background = JBColor.PanelBackground
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val nameLabel = JLabel(userProfile.realName).apply {
            font = Font("微软雅黑", Font.BOLD, 18)
            foreground = JBColor.foreground()
        }

        namePanel.add(nameLabel)
        if (userStatus.isPremium) {
            // 加载 svg 图标
            getIcon("/icons/plusX2.svg", javaClass).apply {
                JLabel(this).apply {
                    preferredSize = Dimension(44, 24)
                    namePanel.add(this)
                }
            }
        }

        panel.add(namePanel)

        return panel
    }

    private fun createStatsPanel(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = JBColor.PanelBackground
            alignmentX = Component.CENTER_ALIGNMENT
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                BorderFactory.createEmptyBorder(0, 0, 15, 0)
            )
        }

        // 竞赛数据行
        val contestRow = JPanel().apply {
            layout = FlowLayout(FlowLayout.CENTER, 15, 0)
            background = JBColor.PanelBackground
            alignmentX = Component.CENTER_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, 70)
        }

        contestRow.add(createStatBlock("竞赛分数", userContestRanks.rating.toInt().toString()))
        contestRow.add(createStatBlock("全球排名", userContestRanks.formatGlobalRank()))
        contestRow.add(createStatBlock("全国排名", userContestRanks.formatLocalRank()))

        // 解题数据行
        val solvedRow = JPanel().apply {
            layout = FlowLayout(FlowLayout.CENTER, 15, 0)
            background = JBColor.PanelBackground
            alignmentX = Component.CENTER_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, 70)
            border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
        }

        solvedRow.add(createColoredStatBlock("简单", userQuestionProgress.formatEasyProgress(), JBColor(0x3FB950, 0x56D364)))
        solvedRow.add(createColoredStatBlock("中等", userQuestionProgress.formatMediumProgress(), JBColor(0xD29922, 0xE3B341)))
        solvedRow.add(createColoredStatBlock("困难", userQuestionProgress.formatHardProgress(), JBColor(0xDA3633, 0xFF6B6B)))

        panel.add(contestRow)
        panel.add(Box.createVerticalStrut(15))
        panel.add(solvedRow)

        return panel
    }

    private fun createStatBlock(title: String, value: String): JPanel {
        return createColoredStatBlock(title, value, JBColor.foreground())
    }

    private fun createColoredStatBlock(title: String, value: String, color: Color): JPanel {
        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(100, 60)
            background = JBColor.PanelBackground
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )

            val titleLabel = JLabel(title, SwingConstants.CENTER).apply {
                font = Font("微软雅黑", Font.PLAIN, 12)
                foreground = JBColor.foreground()
            }
            val valueLabel = JLabel(value, SwingConstants.CENTER).apply {
                font = Font("微软雅黑", Font.BOLD, 15)
                foreground = color
            }

            add(titleLabel, BorderLayout.NORTH)
            add(valueLabel, BorderLayout.CENTER)
        }
    }

    private fun createDefaultAvatar(): Image {
        val bufferedImage = BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB)
        val g2d = bufferedImage.createGraphics()
        g2d.color = JBColor.GRAY
        g2d.fillOval(0, 0, 150, 150)
        g2d.color = JBColor.DARK_GRAY
        g2d.drawOval(0, 0, 150, 150)
        g2d.dispose()
        return bufferedImage
    }

    private fun createTablePanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = JBColor.PanelBackground
        }

        val tableModel = QuestionTableModel(userProgressQuestions.questions)
        val table = JTable(tableModel).apply {
            // 表格样式
            setShowGrid(false)
            intercellSpacing = Dimension(0, 0)
            rowHeight = 32
            selectionBackground = JBColor(0xE7F5FF, 0x2D333B)
            selectionForeground = JBColor.foreground()

            // 表头样式
            tableHeader.apply {
                font = Font("微软雅黑", Font.BOLD, 13)
                background = JBColor(0xF6F8FA, 0x2D333B)
                foreground = JBColor.foreground()
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
            }

            // 默认单元格渲染器
            DefaultTableCellRenderer().apply {
                font = Font("微软雅黑", Font.PLAIN, 13)
                background = JBColor.PanelBackground
                foreground = JBColor.foreground()
                border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
            }

            // 难度列特殊样式
            columnModel.getColumn(1).cellRenderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable, value: Any, isSelected: Boolean,
                    hasFocus: Boolean, row: Int, column: Int
                ): Component {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

                    horizontalAlignment = SwingConstants.CENTER
                    font = Font("微软雅黑", Font.BOLD, 12)

                    when (value.toString()) {
                        "EASY" -> foreground = JBColor(0x3FB950, 0x56D364)
                        "MEDIUM" -> foreground = JBColor(0xD29922, 0xE3B341)
                        "HARD" -> foreground = JBColor(0xDA3633, 0xFF6B6B)
                    }

                    border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    return this
                }
            }
        }

        // 列宽设置
        with(table.columnModel) {
            getColumn(0).preferredWidth = 200  // 题目
            getColumn(1).preferredWidth = 80   // 难度
            getColumn(2).preferredWidth = 120  // 最近提交
            getColumn(3).preferredWidth = 80   // 提交次数
        }

        // 滚动面板
        val scrollPane = JScrollPane(table).apply {
            background = JBColor.PanelBackground
            border = BorderFactory.createEmptyBorder()
            viewportBorder = BorderFactory.createEmptyBorder()
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }

        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    private inner class CircularImageLabel : JLabel() {
        private var image: Image? = null

        fun setImage(image: Image?) {
            this.image = image
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val diameter = 150.coerceAtMost(width.coerceAtMost(height))
            val x = (width - diameter) / 2
            val y = (height - diameter) / 2

            val clipArea = java.awt.geom.Ellipse2D.Double(x.toDouble(), y.toDouble(), diameter.toDouble(), diameter.toDouble())
            g2d.clip = clipArea

            image?.let {
                g2d.drawImage(it, x, y, diameter, diameter, this)
            }
        }
    }
}