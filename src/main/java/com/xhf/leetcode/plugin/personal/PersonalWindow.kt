package com.xhf.leetcode.plugin.personal

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.*
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.xhf.leetcode.plugin.io.http.LeetcodeClient
import com.xhf.leetcode.plugin.model.UserContestRanking
import com.xhf.leetcode.plugin.model.UserQuestionProgress
import com.xhf.leetcode.plugin.utils.BundleUtils
import com.xhf.leetcode.plugin.utils.Constants
import com.xhf.leetcode.plugin.utils.TaskCenter
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*

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

class PersonalWindow(private val project: Project) : JFrame() {

    private val userProfile          = LeetcodeClient.getInstance(project).queryUserProfile()
    private val userStatus           = LeetcodeClient.getInstance(project).queryUserStatus()
    private val userQuestionProgress = LeetcodeClient.getInstance(project).queryUserQuestionProgress()
    private val userContestRanks     = LeetcodeClient.getInstance(project).queryUserContestRanking()

    init {
        title = BundleUtils.i18nHelper("个人中心", "Personal Center")
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        size = Dimension(450, 750)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dispose()
            }
        })

        val content = JPanel(BorderLayout()).apply {
            background = JBColor.PanelBackground
            border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
        }

        content.add(createCircularImagePanel(), BorderLayout.NORTH)
        content.add(createStatsPanel(), BorderLayout.CENTER)
        content.add(createListPanel(), BorderLayout.SOUTH)

        add(content)
        isVisible = true
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
        }

        // 第一行
        val contestPanel = JPanel(GridLayout(1, 3, 15, 10)).apply {
            background = JBColor.PanelBackground
            add(createStatBlock("竞赛分数", userContestRanks.rating.toInt().toString()))
            add(createStatBlock("全球排名", userContestRanks.formatGlobalRank()))
            add(createStatBlock("全国排名", userContestRanks.formatLocalRank()))
        }

        // 第二行（这里加入颜色差异）
        val solvePanel = JPanel(GridLayout(1, 3, 15, 10)).apply {
            background = JBColor.PanelBackground
            add(createColoredStatBlock("简单", userQuestionProgress.formatEasyProgress()  , JBColor(0x3FB950, 0x56D364))) // 绿色
            add(createColoredStatBlock("中等", userQuestionProgress.formatMediumProgress(), JBColor(0xD29922, 0xE3B341))) // 黄色
            add(createColoredStatBlock("困难", userQuestionProgress.formatHardProgress()  , JBColor(0xDA3633, 0xFF6B6B))) // 红色
        }

        panel.add(contestPanel)
        panel.add(Box.createVerticalStrut(20))
        panel.add(solvePanel)
        panel.add(Box.createVerticalStrut(20))

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

    private fun createListPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
            preferredSize = Dimension(400, 300)
            background = JBColor.PanelBackground
        }

        val listModel = DefaultListModel<String>().apply {
            for (i in 1..20) {
                addElement("练习记录 $i")
            }
        }

        val jbList = JBList(listModel).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            font = Font("微软雅黑", Font.PLAIN, 13)
        }

        val scrollPane = JScrollPane(jbList).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
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