package com.xhf.leetcode.plugin.service;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
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
            String msg = BundleUtils.i18nHelper("JCEF登录失败, 错误原因为: " + e.getMessage() + "\n请尝试重启idea, 否则系统将采用cookie登录",
                    "JCEF login failed, the reason is: " + e.getMessage() + "\nPlease try to restart idea, otherwise the system will use cookie login");
            ConsoleUtils.getInstance(project).showInfo(msg);
            LogUtils.error("JCEF Login Failed, Start Cookie Login...", e);
            startCookieLogin(project);
        }
    }

    public void doLoginAfter () {
        loginSuccessAfter(project, false);
    }

    private void startCookieLogin(Project project) {
        try {
            new CookieLoginWindow(project).start();
        } catch (Exception e) {
            throw new RuntimeException("Cookie Login Failed", e);
        }
    }

    private void loginSuccessAfter(Project project) {
        loginSuccessAfter(project, true);
    }

    private void loginSuccessAfter(Project project, boolean info) {
        loginFlag = Boolean.TRUE;
        if (info) {
            LogUtils.info("登录成功, 正在查询数据...");
            // 此处不能弹出对话框, 因为对话框会凝固线程. 登录逻辑涉及不少多线程问题, 不适合弹框
            ConsoleUtils.getInstance(Objects.requireNonNull(project)).showInfo(BundleUtils.i18nHelper("登录成功...", "Login Success..."), false);
        }
        // post event
        LCEventBus.getInstance().post(new LoginEvent(project));
        // load data
        QuestionService.getInstance(project).loadAllQuestionData(project);
    }

    /**
     * 只要用户的Cookie存在且合法, 插件就默认登录, 无需用户再次点击
     */
    private boolean loginFlag = Boolean.FALSE;

    /**
     * 判断系统是否登录
     */
    public boolean isLogin() {
        if (loginFlag) {
            return true;
        }
        boolean login = LeetcodeClient.getInstance(project).isLogin();
        if (login) {
            loginFlag = true;
        }
        return login;
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
            this.loginButton = new JButton(BundleUtils.i18n("Login"));
            this.cancelButton = new JButton(BundleUtils.i18n("Cancel"));
            this.helpButton = new JButton(BundleUtils.i18n("Help"));
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
                    JOptionPane.showMessageDialog(contentPane,  BundleUtils.i18nHelper("请输入你的cookie", "Please input your cookie"), BundleUtils.i18n("leetcode.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LeetcodeClient instance = LeetcodeClient.getInstance(project);
                instance.setCookie(new BasicClientCookie2(LeetcodeApiUtils.LEETCODE_SESSION, text));
                if (instance.isLogin()) {
                    loginSuccessAfter(project);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(contentPane, BundleUtils.i18nHelper("Cookie错误, 请重试", "Cookie Error, Please Try Again"), BundleUtils.i18n("leetcode.error"), JOptionPane.ERROR_MESSAGE);
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
            return BundleUtils.i18n("WebAuth");
        }

        @Override
        void loadComponent(JFrame frame) {
            final JBCefBrowser jbcebrowser = new JBCefBrowser(LeetcodeApiUtils.getLeetcodeLogin());
            JBCefClient jbCefClient = jbcebrowser.getJBCefClient();
            CefCookieManager cefCookieManager = jbcebrowser.getJBCefCookieManager().getCefCookieManager();

            // set frame listener
            /*
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // clear all cookies
                    cefCookieManager.deleteCookies("", "");
                    // close jcef browser
                    Disposer.dispose(jbcebrowser);
                }
            });
             */

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
                                LogUtils.simpleDebug("login info exists, do login success after...");
                                // update cookies
                                LeetcodeClient.getInstance(project).clearCookies();
                                // store cookies
                                LeetcodeClient.getInstance(project).setCookies(cookieList);
                                loginSuccessAfter(project);
                                new Thread(() -> {
                                    try {
                                        // clear all cookies
                                        cefCookieManager.deleteCookies("", "");
                                    } finally {
                                        if (frame != null) {
                                            frame.setVisible(false);
                                            frame.dispose();
                                        }
                                    }
                                    // close jcef browser
                                    Disposer.dispose(jbcebrowser);
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
    public void logoutEventListeners(LogoutEvent event) {
        loginFlag = false;
        LeetcodeClient.getInstance(project).clearCookies();
    }

    @Subscribe
    public void secretUpdateEventListener(SecretKeyUpdateEvent event) {
        // 重新加密登录cookie
        LeetcodeClient instance = LeetcodeClient.getInstance(project);
        // 从客户端中获取session
        List<Cookie> leetcodeSession = instance.getLeetcodeSession();
        for (Cookie cookie : leetcodeSession) {
            // 不是bcc2, 该cookie就不是由Runner创建的, skip
            if (! (cookie instanceof BasicClientCookie2)) {
                continue;
            }
            if (cookie.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION)) {
                // 重新加密存储
                String value = cookie.getValue();
                StoreService.getInstance(project).addEncryptCache(StoreService.LEETCODE_SESSION_KEY, value, true);
            }
        }
    }
}
