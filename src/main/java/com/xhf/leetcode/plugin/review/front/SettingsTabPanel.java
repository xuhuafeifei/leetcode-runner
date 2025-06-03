package com.xhf.leetcode.plugin.review.front;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.review.backend.database.DatabaseAdapter;
import com.xhf.leetcode.plugin.review.backend.entity.Card;
import com.xhf.leetcode.plugin.review.backend.database.Sqlite;
import com.xhf.leetcode.plugin.review.utils.ReviewConstants;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SettingsTabPanel extends JPanel {
    private final Project project;
    private final JButton exportBtn;
    private final JButton mergeBtn;

    public SettingsTabPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        // 导出按钮
        exportBtn = new JButton(BundleUtils.i18nHelper("导出", "Export"));
        exportBtn.addActionListener(new ExportActionListener());
        
        // 合并按钮
        mergeBtn = new JButton(BundleUtils.i18nHelper("智能合并", "Smart Merge"));
        mergeBtn.addActionListener(new MergeActionListener());
        
        buttonPanel.add(exportBtn);
        buttonPanel.add(mergeBtn);
        
        add(buttonPanel, BorderLayout.CENTER);
    }

    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 获取当前日期时间作为文件名
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
                String fileName = "export-" + sdf.format(new Date()) + ".json";
                
                // 从数据库获取数据
                List<Card> data = DatabaseAdapter.getInstance(project)
                    .getSqlite()
                    .queryForCards("SELECT * FROM cards");

                // 转换为JSON格式
                String jsonData = new Gson().toJson(data);

                // 获取导出路径
                String exportPath = System.getProperty("user.home") + "/Downloads/" + fileName;

                // 写入文件
                Files.write(Paths.get(exportPath), jsonData.getBytes());

                // 显示成功消息
                ConsoleUtils.getInstance(project).showInfo(
                    BundleUtils.i18nHelper("导出成功: " + exportPath, "Export success: " + exportPath));
            } catch (Exception ex) {
                ConsoleUtils.getInstance(project).showError(
                    BundleUtils.i18nHelper("导出失败: " + ex.getMessage(), "Export failed: " + ex.getMessage()));
            }
        }
    }

    private class MergeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(BundleUtils.i18nHelper("选择导出文件", "Select export file"));

            if (fileChooser.showOpenDialog(SettingsTabPanel.this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = fileChooser.getSelectedFile();
                    String jsonData = new String(Files.readAllBytes(selectedFile.toPath()));

                    // 解析JSON数据
                    List<Card> importData = new Gson().fromJson(jsonData,
                        new TypeToken<List<Card>>(){}.getType());

                    // 获取当前数据库数据
                    List<Card> currentData = DatabaseAdapter.getInstance(project)
                        .getSqlite()
                        .queryForCards("SELECT * FROM cards");

                    // 创建合并后的数据列表
                    Map<Integer, Card> mergedData = new HashMap<>();

                    // 添加当前数据
                    for (Card item : currentData) {
                        mergedData.put(item.getCardId(), item);
                    }

                    // 合并导入数据(只合并back字段)
                    for (Card item : importData) {
                        Integer cardId = item.getCardId();
                        if (mergedData.containsKey(cardId)) {
                            // 只更新back字段
                            Card existing = mergedData.get(cardId);
                            existing.setBack(existing.getBack() + "\n" + item.getBack());
                        } else {
                            // 添加新记录
                            mergedData.put(cardId, item);
                        }
                    }

                    // 更新数据库
                    Sqlite sqlite = DatabaseAdapter.getInstance(project).getSqlite();
                    sqlite.beginTransaction();
                    try {
                        // 清空表
                        sqlite.queryUpdate("DELETE FROM cards");

                        // 插入合并后的数据
                        for (Card item : mergedData.values()) {
                            Map<String, Object> dataMap = getStringObjectMap(item);
                            sqlite.insert("cards", dataMap);
                        }

                        sqlite.commitTransaction();
                        
                        ConsoleUtils.getInstance(project).showInfo(
                            BundleUtils.i18nHelper("合并成功", "Merge success"));
                    } catch (Exception ex) {
                        sqlite.rollbackTransaction();
                        throw ex;
                    }
                } catch (Exception ex) {
                    ConsoleUtils.getInstance(project).showError(
                        BundleUtils.i18nHelper("合并失败: " + ex.getMessage(), "Merge failed: " + ex.getMessage()));
                }
            }
        }

        private @NotNull Map<String, Object> getStringObjectMap(Card item) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("card_id", item.getCardId());
            dataMap.put("front", item.getFront());
            dataMap.put("back", item.getBack());
            dataMap.put("created", item.getCreated());
            dataMap.put("stability", item.getStability());
            dataMap.put("difficulty", item.getDifficulty());
            dataMap.put("elapsed_days", item.getElapsedDays());
            dataMap.put("repetitions", item.getRepetitions());
            dataMap.put("day_interval", item.getDayInterval());
            dataMap.put("state", item.getState());
            dataMap.put("next_repetition", item.getNextRepetition());
            dataMap.put("last_review", item.getLastReview());
            return dataMap;
        }
    }
}