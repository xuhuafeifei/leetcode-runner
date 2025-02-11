package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.utils.LoginPass;

/**
 * 清除缓存文件
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LoginPass
public class ClearAction extends AbstractAction {

    @Override
    public void doActionPerformed(Project project, AnActionEvent e) {
        int result = Messages.showOkCancelDialog(
                project,
                "Are you sure you want to delete cache files? This will remove your login status and all cached question content.",
                "Confirm Deletion",
                Messages.getOkButton(),
                Messages.getCancelButton(),
                Messages.getQuestionIcon()
        );

        if (result == Messages.OK) {
            StoreService.getInstance(project).clearCache();
            // post. 通过总线异步通知订阅ClearCacheEvent的订阅者, 处理额外逻辑
            // 比如退出登录状态, 清除LCPanel显示的题目数据等
            LCEventBus.getInstance().post(new ClearCacheEvent(project));
        }
    }
}
