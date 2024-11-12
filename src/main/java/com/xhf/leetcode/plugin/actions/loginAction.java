package com.xhf.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.window.LCPanel;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class loginAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        LCPanel.MyList myList = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_QUESTION_LIST);

        if (LeetcodeClient.getInstance(project).isLogin()) {
            // load data
            QuestionService.getInstance().loadAllQuestionData(project, myList);
            LogUtils.LOG.info("login success...");
            ConsoleUtils.getInstance(Objects.requireNonNull(project)).showInfo("login success...");
            return;
        }
        JcefLoginWindow jcefLoginWindow = new JcefLoginWindow(project, myList);
        jcefLoginWindow.start();
    }


    class JcefLoginWindow {
        private Project project;

        private LCPanel.MyList myList;

        public JcefLoginWindow(Project project, LCPanel.MyList myList) {
            this.project = project;
            this.myList = myList;
        }

        public void start() {
            Window activeFrame = IdeFrameImpl.getActiveFrame();
            if (activeFrame == null) {
                return;
            }
            Rectangle bounds = activeFrame.getGraphicsConfiguration().getBounds();
            final JFrame frame = new IdeFrameImpl();
            frame.setTitle("Web Auth");
            frame.setAlwaysOnTop(true);
            frame.setDefaultCloseOperation(2);
            frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
            frame.setLayout(new BorderLayout());

            loadJCEFComponent(frame);

            frame.setVisible(true);
        }

        private void loadJCEFComponent(JFrame frame) {
            final JBCefBrowser jbcebrowser = new JBCefBrowser(LeetcodeApiUtils.getLeetcodeLogin());
            JBCefClient jbCefClient = jbcebrowser.getJBCefClient();
            CefCookieManager cefCookieManager = jbcebrowser.getJBCefCookieManager().getCefCookieManager();

            // set frame listener
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // clear all cookies
                    cefCookieManager.deleteCookies("", "");
                    // close jcef browser
                    Disposer.dispose(jbcebrowser);
                }
            });


            // add cookie listener to check whether the target cookie exists when the page is loaded
            jbCefClient.addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
                    List<Cookie> cookieList = new ArrayList<>();

                    // visit all cookies
                    cefCookieManager.visitAllCookies((cookie, count, total, delete) -> {
                        if (cookie.domain.contains("leetcode")) {
                            BasicClientCookie2 basicClientCookie2 = new BasicClientCookie2(cookie.name, cookie.value);
                            cookieList.add(basicClientCookie2);
                        }
                        if (count == total - 1) {
                            // login info exists!
                            if ( cookieList.stream().anyMatch(c -> c.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION)) ) {
                                // update cookies
                                LeetcodeClient.getInstance(project).clearCookies();
                                // store cookies
                                LeetcodeClient.getInstance(project).setCookies(cookieList);
                                QuestionService.getInstance().loadAllQuestionData(project, myList);
                                ConsoleUtils.getInstance(project).showInfo("login success...");
                                frame.dispose();
                            } else {
                                cookieList.clear();
                            }
                        }
                        return Boolean.TRUE;
                    });
                }
            }, jbcebrowser.getCefBrowser());

            frame.add(jbcebrowser.getComponent(), BorderLayout.CENTER);
        }
    }
}
