package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LogoAction extends AbstractAction {

    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
        // do noting
        ConsoleUtils.getInstance(project).showInfo(
            BundleUtils.i18nHelper(
                "欢迎使用Leetcode-Runner插件" + "\n\n万水千山总是情, 给个star, 行不行"
                    + "\n\nhttps://github.com/xuhuafeifei/leetcode-runner",
                "Welcome to Leetcode-Runner plugin" + "\n\nThe world is full of water, give me a star, can I go?"
                    + "\n\nhttps://github.com/xuhuafeifei/leetcode-runner"
            ),
            true, true
        );
    }
}
