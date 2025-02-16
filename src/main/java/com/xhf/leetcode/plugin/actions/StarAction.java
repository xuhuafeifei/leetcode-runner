package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.xhf.leetcode.plugin.comp.CarouselSingleText;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.LoginPass;
import com.xhf.leetcode.plugin.utils.RatePass;
import com.xhf.leetcode.plugin.utils.SettingPass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LoginPass
@SettingPass
@RatePass
public class StarAction extends AbstractAction {
    @Override
    protected void doActionPerformed(Project project, AnActionEvent e) {
//        JOptionPane.showOptionDialog(
//                null,
//                cs,
//                "给个star吧, 上帝",
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                new Object[]{"确定", "取消"},
//                "确定"
//        );

        new MyCustomDialog().showAndGet();
    }

    public static class MyCustomDialog extends DialogWrapper {

        private JTextField textField;

        public MyCustomDialog() {
            super(true);
            setTitle("给个star吧, 上帝");
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            CarouselSingleText cs = new CarouselSingleText();
            cs.addImage(getClass().getResource("/icons/star1.jpg"));
            cs.addImage(getClass().getResource("/icons/star2.jpg"));
            cs.setPreferredSize(new Dimension(400, 300));
            try {
                JEditorPane jep = getjEditorPane();
                cs.updateTextPane(jep);
            } catch (IOException ex) {
                LogUtils.warn(DebugUtils.getStackTraceAsString(ex));
            }

            return cs;
        }

        @NotNull
        private JEditorPane getjEditorPane() throws IOException {
            JEditorPane jep = new JEditorPane();
            jep.setContentType("text/html");
            jep.setText("<p><a href='https://github.com/xuhuafeifei/leetcode-runner' target='_blank' class='url'>给个star吧, 上帝</a></p>");
            jep.setEditable(false);
            jep.addHyperlinkListener(e1 -> {
                if (e1.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        java.awt.Desktop.getDesktop().browse(e1.getURL().toURI());
                    } catch (Exception ex) {
                        LogUtils.warn(DebugUtils.getStackTraceAsString(ex));
                    }
                }
            });
            return jep;
        }

        public String getText() {
            return textField.getText();
        }
    }
}
