package com.xhf.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class QuestionListener extends AbstractMouseAdapter {

    private final MyList<Question> questionList;

    public QuestionListener(MyList<Question> questionList, Project project) {
        super(project);
        this.questionList = questionList;
    }

    /**
     * 创建题目
     */
    @Override
    protected void doubleClicked(MouseEvent e) {
        Point point = e.getPoint(); // 获取鼠标双击坐标
        int idx = questionList.locationToIndex(point); // 将坐标转换为列表索引
        Question question = questionList.getModel().getElementAt(idx);  // 获取对应题目数据
        try {
            CodeService.getInstance(project).openCodeEditor(question); // 打开代码编辑器
        } catch (FileCreateError ex) {
            LogUtils.error(ex);
            ConsoleUtils.getInstance(project)
                .showError(BundleUtils.i18n("code.service.file.create.error") + "\n" + ex.getMessage(), true, true);
        }
    }
}
