package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class EditorMenuGroup extends DefaultActionGroup {

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (project == null || file == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        String key = ViewUtils.getUnifyFilePathByVFile(file);
        boolean allow = StoreService.getInstance(project).contains(key);

        e.getPresentation().setEnabledAndVisible(allow);
    }

}
