package com.xhf.leetcode.plugin.service;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.bus.LoginEvent;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
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
@Service(Service.Level.PROJECT)
@LCSubscriber(events = {ClearCacheEvent.class})
public final class LoginService {

    private final Project project;

    public LoginService(Project project) {
        this.project = project;
        LCEventBus.getInstance().register(this);
    }

    public static LoginService getInstance(Project project) {
        return project.getService(LoginService.class);
    }

    public void doLogin() {
        // do not call isLogin function in this class
        if (LeetcodeClient.getInstance(project).isLogin()) {
            loginSuccessAfter(project);
            return;
        }

        JcefLoginWindow jcefLoginWindow = new JcefLoginWindow(project);
        try {
            jcefLoginWindow.start();
        } catch (Exception e) {
            ConsoleUtils.getInstance(project).showInfo("JCEF登录失败, 错误原因为: " + e.getMessage() + "\n请尝试重启idea, 否则系统将采用cookie登录");
            LogUtils.error("JCEF Login Failed, Start Cookie Login...", e);
            startCookieLogin(project);
        }
    }

    private void startCookieLogin(Project project) {
        try {
            new CookieLoginWindow(project).start();
        } catch (Exception e) {
            throw new RuntimeException("Cookie Login Failed", e);
        }
    }

    private void loginSuccessAfter(Project project) {
        loginFlag = Boolean.TRUE;
        LogUtils.info("login success, try to loading data");
        // 此处不能弹出对话框, 因为对话框会凝固线程. 登录逻辑涉及不少多线程问题, 不适合弹框
        ConsoleUtils.getInstance(Objects.requireNonNull(project)).showInfo("login success...", false);
        // post event
        LCEventBus.getInstance().post(new LoginEvent(project));
        // load data
        QuestionService.getInstance().loadAllQuestionData(project);
    }

    private boolean loginFlag = Boolean.FALSE;

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
    public boolean isLogin() {
        return loginFlag && LeetcodeClient.getInstance(project).isLogin();
    }

    abstract static class BasicWindow {
        protected Project project;
        public BasicWindow(Project project) {
            this.project = project;
        }

        protected void start() throws Exception {
            final JFrame frame = new JFrame();
            frame.setTitle(getFrameTitle());
            frame.setAlwaysOnTop(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            // 设置在正中心

            // 获取屏幕尺寸
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

            // 计算frame应该出现的位置
            int x = (screenSize.width - frame.getWidth()) / 4;
            int y = (screenSize.height - frame.getHeight()) / 4;

            // 设置frame的位置
            frame.setLocation(x, y);
            frame.setSize(screenSize.width / 2, screenSize.height / 2);

            // frame.setBounds(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);

            frame.setLayout(new BorderLayout());

            loadComponent(frame);

            frame.setVisible(true);
        }

        abstract String getFrameTitle();

        abstract void loadComponent(JFrame frame) throws Exception;
    }

    class CookieLoginWindow extends BasicWindow {

        private JPanel contentPane;
        private JTextArea textArea;
        private final JButton loginButton;
        private final JButton cancelButton;
        private final JButton helpButton;

        public CookieLoginWindow(Project project) {
            super(project);
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

            var scrollPane = new JBScrollPane(textArea);
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
                    loginSuccessAfter(project);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(contentPane, "Cookie Error, Please Try Again", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        @Deprecated
        class HelpDialog extends DialogWrapper {
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

                JBScrollPane scrollPane = new JBScrollPane(helpContentPane);
                panel.add(scrollPane, BorderLayout.CENTER);

                return panel;
            }

            private String getHelpContent() {
                URL url = FileUtils.getResourceFileUrl("\\help\\CookieLoginHelp.md");
                return FileUtils.readContentFromFile(url);
            }
        }
    }

    class JcefLoginWindow extends BasicWindow {

        public JcefLoginWindow(Project project) {
            super(project);
        }

        @Override
        String getFrameTitle() {
            return "Web Auth";
        }

        @Override
        void loadComponent(JFrame frame) {
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
                                loginSuccessAfter(project);
                                new Thread(() -> {
                                    frame.setVisible(false);
                                    frame.dispose();
                                }).start();
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

    // 清除本地登陆状态的标志位
    @Subscribe
    public void clearCacheListeners(ClearCacheEvent event) {
        loginFlag = false;

    }
}
