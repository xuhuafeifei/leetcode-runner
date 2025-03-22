package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.xhf.leetcode.plugin.review.backend.model.QueryDimModel;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.service.MockRQServiceImpl;
import com.xhf.leetcode.plugin.review.backend.service.ReviewQuestionService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 每日复习计划tab
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TotalReviewPlanTabPanel extends JPanel {
    private final Project project;
    private final ReviewQuestionService service;
    private JBTable reviewTable;
    private DefaultTableModel tableModel;
    private List<ReviewQuestion> reviewQuestions;
    
    public TotalReviewPlanTabPanel(Project project) {
        this.project = project;
        this.service = MockRQServiceImpl.getInstance();
        setLayout(new BorderLayout());
        loadContent();
    }

    private void loadContent() {
        // 获取复习题目列表
        reviewQuestions = service.getTotalReviewQuestion(new QueryDimModel());
        
        // 创建表格模型 - 只有两列
        String[] columnNames = {
                BundleUtils.i18n("review.table.title"),
                BundleUtils.i18n("review.table.next.review")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };
        
        // 填充数据
        for (ReviewQuestion question : reviewQuestions) {
            tableModel.addRow(new Object[]{
                question.getTitle(),
                question.getNextReview()
            });
        }
        
        // 创建表格
        reviewTable = new JBTable(tableModel);
        
        // 设置表格属性
        reviewTable.setRowHeight(30);
        reviewTable.setShowGrid(true);
        reviewTable.setStriped(true);
        reviewTable.getTableHeader().setReorderingAllowed(false);
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置颜色渲染器
        reviewTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    ReviewQuestion question = reviewQuestions.get(row);
                    String difficulty = question.getDifficulty();
                    
                    // 根据难度设置颜色
                    if ("EASY".equalsIgnoreCase(difficulty)) {
                        c.setForeground(Constants.GREEN_COLOR);
                    } else if ("MEDIUM".equalsIgnoreCase(difficulty)) {
                        c.setForeground(Constants.YELLOW_COLOR);
                    } else if ("HARD".equalsIgnoreCase(difficulty)) {
                        c.setForeground(Constants.RED_COLOR);
                    }
                }
                
                return c;
            }
        });
        
        // 设置复习时间的颜色渲染器
        reviewTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            private final Color URGENT_COLOR = new Color(255, 69, 0);  // 红橙色
            private final Color SOON_COLOR = new Color(255, 140, 0);   // 深橙色
            private final Color NORMAL_COLOR = new Color(0, 128, 0);   // 深绿色
            private final Color FUTURE_COLOR = new Color(30, 144, 255); // 道奇蓝

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String nextReview = (String) value;
                    // 这里可以根据实际的日期格式进行解析和判断
                    // 目前简单根据字符串包含的关键词来设置颜色
                    if (nextReview.contains("今天") || nextReview.contains("today")) {
                        c.setForeground(URGENT_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (nextReview.contains("明天") || nextReview.contains("tomorrow")) {
                        c.setForeground(SOON_COLOR);
                    } else if (nextReview.contains("后天") || nextReview.contains("day after tomorrow")) {
                        c.setForeground(NORMAL_COLOR);
                    } else {
                        c.setForeground(FUTURE_COLOR);
                    }
                }
                
                return c;
            }
        });
        
        // 添加鼠标点击监听器
        reviewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = reviewTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 点击行时触发的逻辑
                    // TODO: 实现行点击处理逻辑
                    ReviewQuestion question = reviewQuestions.get(selectedRow);
                    System.out.println("Selected: " + question.getTitle());
                }
            }
        });
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton(BundleUtils.i18n("review.button.delete"));
        deleteButton.addActionListener(e -> {
            int selectedRow = reviewTable.getSelectedRow();
            if (selectedRow >= 0) {
                // 删除按钮的逻辑
                // TODO: 实现删除逻辑
                System.out.println("Delete row: " + selectedRow);
                tableModel.removeRow(selectedRow);
                reviewQuestions.remove(selectedRow);
            }
        });
        buttonPanel.add(deleteButton);
        
        // 将表格和按钮添加到面板
        add(new JBScrollPane(reviewTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
