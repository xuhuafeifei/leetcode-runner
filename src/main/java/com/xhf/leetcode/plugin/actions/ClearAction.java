package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LogoutEvent;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.utils.LoginPass;

/**
 * ��������ļ�
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
                "��ȷ��Ҫɾ�������ļ���? ��������ʹ��Leetcode-RunnerʱϵͳĬ�ϻ����һ����Ŀ����, ͬʱ�˳���¼״̬, ��ղ���������Ϣ",
                "ȷ��ɾ��",
                Messages.getOkButton(),
                Messages.getCancelButton(),
                Messages.getQuestionIcon()
        );

        if (result == Messages.OK) {
            StoreService.getInstance(project).clearCache();
            // post. ͨ�������첽֪ͨ����ClearCacheEvent�Ķ�����, ��������߼�
            // �����˳���¼״̬, ���LCPanel��ʾ����Ŀ���ݵ�
            LCEventBus.getInstance().post(new LogoutEvent());
            LCEventBus.getInstance().post(new ClearCacheEvent(project));
        }
    }
}
