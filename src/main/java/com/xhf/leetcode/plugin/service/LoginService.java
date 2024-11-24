package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.window.LCPanel;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LoginService {
    public static void doLogin(Project project) {
        MyList<Question> myList = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_QUESTION_LIST);
        // do not call isLogin function in this class
        if (LeetcodeClient.getInstance(project).isLogin()) {
            loginSuccessAfter(project, myList);
            return;
        }

        JcefLoginWindow jcefLoginWindow = new JcefLoginWindow(project, myList);
        try {
            jcefLoginWindow.start();
        } catch (Exception e) {
            LogUtils.LOG.error("JCEF Login Failed, Start Cookie Login...", e);
            startCookieLogin(project, myList);
        }
    }

    private static void startCookieLogin(Project project, MyList<Question> myList) {
        try {
            new CookieLoginWindow(project, myList).start();
        } catch (Exception e) {
            throw new RuntimeException("Cookie Login Failed", e);
        }
    }

    private static void loginSuccessAfter(Project project, MyList<Question> myList) {
        // load data
        loginFlag = Boolean.TRUE;
        QuestionService.getInstance().loadAllQuestionData(project, myList);
        LogUtils.LOG.info("login success...");
        ConsoleUtils.getInstance(Objects.requireNonNull(project)).showInfo("login success...");
    }

    private static boolean loginFlag = Boolean.FALSE;

    /**
     * judge whether the client is login
     * <p>
     * there are two cases in this project
     * <p>
     * first, the user isn't logged and the cookie has not been stored in the local file
     * <p>
     * second, the user isn't logged in, but the cookie has been stored in the local file. but the cookie was created by last time,
     * and this time the user did not click the login button, which means the user did not choose to log in
     * <p>
     * loginFlag is a flag to check whether the user has clicked the login button and login succussfully
     *
     */
    public static boolean isLogin(Project project) {
        return loginFlag && LeetcodeClient.getInstance(project).isLogin();
    }

    static abstract class BasicWindow {
        protected Project project;
        protected MyList<Question> myList;
        public BasicWindow(Project project, MyList<Question> myList) {
            this.project = project;
            this.myList = myList;
        }

        protected void start() throws Exception {
            Window activeFrame = IdeFrameImpl.getActiveFrame();
            if (activeFrame == null) {
                return;
            }
            Rectangle bounds = activeFrame.getGraphicsConfiguration().getBounds();
            final JFrame frame = new IdeFrameImpl();
            frame.setTitle(getFrameTitle());
            frame.setAlwaysOnTop(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
            frame.setLayout(new BorderLayout());

            loadComponent(frame);

            frame.setVisible(true);
        }

        abstract String getFrameTitle();

        abstract void loadComponent(JFrame frame) throws Exception;
    }

    static class CookieLoginWindow extends BasicWindow {

        private JPanel contentPane;
        private JTextArea textArea;
        private final JButton loginButton;
        private final JButton cancelButton;
        private final JButton helpButton;

        public CookieLoginWindow(Project project, MyList<Question> myList) {
            super(project, myList);
            this.contentPane = new JPanel();
            this.textArea = new JTextArea();
            this.loginButton = new JButton("Login");
            this.cancelButton = new JButton("Cancel");
            this.helpButton = new JButton("Help");
        }

        @Override
        String getFrameTitle() {
            return "Cookie Login";
        }

        @Override
        void loadComponent(JFrame frame) {
            contentPane = new JPanel(new BorderLayout());
            textArea = new JTextArea(400, 400);
            textArea.setEditable(true);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPane.add(scrollPane, BorderLayout.CENTER);

            // Initialize buttons
            JPanel buttonPanel = new JPanel();
            initLoginBtn(frame);
            initCancelBtn(frame);
            // initHelpBtn(frame);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(loginButton);
            buttonPanel.add(cancelButton);
            // buttonPanel.add(helpButton);

            contentPane.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(contentPane, BorderLayout.CENTER);
        }

        private void initHelpBtn(JFrame frame) {
            helpButton.addActionListener(e -> {
                new HelpDialog(project, true).show();
            });
        }

        private void initCancelBtn(JFrame frame) {
            cancelButton.addActionListener(e -> {
                frame.dispose();
            });
        }

        private void initLoginBtn(JFrame frame) {
            loginButton.addActionListener(e -> {
                String text = textArea.getText();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(contentPane, "Please input your cookie", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LeetcodeClient instance = LeetcodeClient.getInstance(project);
                instance.setCookie(new BasicClientCookie2(LeetcodeApiUtils.LEETCODE_SESSION, text));
                if (instance.isLogin()) {
                    loginSuccessAfter(project, myList);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(contentPane, "Cookie Error, Please Try Again", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        @Deprecated
        static class HelpDialog extends DialogWrapper {
            @Override
            protected void init() {
                super.init();
                getPeer().getWindow().setSize(new Dimension(600, 500));
            }

            protected HelpDialog(@Nullable Project project, boolean canBeParent) {
                super(project, canBeParent);
                init();
                setTitle("Help");
            }

            @Override
            protected @Nullable JComponent createCenterPanel() {
                JPanel panel = new JPanel(new BorderLayout());
                JTextPane helpContentPane = new JTextPane();
                helpContentPane.setContentType("text/html");
                helpContentPane.setText(getHelpContent());
                helpContentPane.setEditable(false);

                JScrollPane scrollPane = new JScrollPane(helpContentPane);
                panel.add(scrollPane, BorderLayout.CENTER);

                return panel;
            }

            private String getHelpContent() {
                URL url = FileUtils.getResourceFileUrl("\\help\\CookieLoginHelp.md");
                return FileUtils.readContentFromFile(url);
            }
        }
    }

    static class JcefLoginWindow extends BasicWindow {

        public JcefLoginWindow(Project project, MyList<Question> myList) {
            super(project, myList);
        }

        @Override
        String getFrameTitle() {
            return "Web Auth";
        }

        @Override
        void loadComponent(JFrame frame) throws Exception {
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
                            if (cookieList.stream().anyMatch(c -> c.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION))) {
                                // update cookies
                                LeetcodeClient.getInstance(project).clearCookies();
                                // store cookies
                                LeetcodeClient.getInstance(project).setCookies(cookieList);
                                loginSuccessAfter(project, myList);
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
