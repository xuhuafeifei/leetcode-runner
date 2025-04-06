package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
