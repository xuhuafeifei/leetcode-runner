package com.xhf.leetcode.plugin.window;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.DataKeys;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCPanel extends SimpleToolWindowPanel implements DataProvider {
    private MyList questionList;

    private Project project;

    public LCPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;

        final ActionManager actionManager = ActionManager.getInstance();

        initLeetcodeClient();

        // get action toolbar
        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.lcActionsToolbar"),
                true);

        // build question list
        questionList = new MyList();
        questionList.setCellRenderer(new QuestionCellRender());
        questionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int idx = questionList.locationToIndex(point);
                Question question = questionList.getModel().getElementAt(idx);
                CodeService.openCodeEditor(question, project);
            }
        });

        // store to action toolbar
        actionToolbar.setTargetComponent(questionList);
        setToolbar(actionToolbar.getComponent());
        setContent(new JBScrollPane(questionList));
    }

    private void initLeetcodeClient() {
        LeetcodeClient.init(this.project);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_QUESTION_LIST.is(dataId)) {
            return questionList;
        }
        return super.getData(dataId);
    }

    public static class MyList extends JBList<Question> {
        public void setListData(List<Question> totalQuestion) {
            Question[] data = new Question[totalQuestion.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = totalQuestion.get(i);
            }
            this.setListData(data);
        }
    }

    public static class QuestionCellRender extends DefaultListCellRenderer {
        private static Color Level1 = new Color(92, 184, 92);
        private static Color Level2 = new Color(240, 173, 78);
        private static Color Level3 = new Color(217, 83, 79);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Question question = (Question) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(list, question.toString(), index, isSelected, cellHasFocus);
            switch (question.getDifficulty()) {
                case "EASY": label.setForeground(Level1); break;
                case "MEDIUM": label.setForeground(Level2); break;
                case "HARD": label.setForeground(Level3); break;
            }
            return label;
        }
    }

}
