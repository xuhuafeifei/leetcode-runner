package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.window.LCPanel;
import com.xhf.leetcode.plugin.window.LCToolWindowFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefCookieManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LoginService {
    public static void doLogin(Project project) {
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
     * @return
     */
    public static boolean isLogin(Project project) {
        return loginFlag && LeetcodeClient.getInstance(project).isLogin();
    }

    static class JcefLoginWindow {
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
                             if (cookieList.stream().anyMatch(c -> c.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION))) {
                                 // set login flag
                                 loginFlag = Boolean.TRUE;
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
