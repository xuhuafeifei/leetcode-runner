package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.LocalHttpRequestHandler;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.Article;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * support html or markdown show ability
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MarkDownEditor implements FileEditor {
    private final Project project;

    /**
     * 采用JCEF技术+前端vditor框架渲染MarkDown内容
     * <p>
     * MarkDown内容渲染的核心原理是: 通过JCEF技术模拟浏览器, 通过前端框架Vidtor
     * 以HTML的代码形式渲染MarkDown内容, 从而实现MarkDown的文本显示
     * <p>
     * MarkDown内容渲染, 需要HTML文件 + Vditor框架代码. 为了提高渲染速度, 本项目的HTML文件和Vditor框架代码
     * 均由本地提供. 另外, HTML通过<script src="xxx">的形式, 通过网络加载Vditor代码, 因此本项目也会对
     * http请求进行拦截. 详细请参考io.LocalHttpRequestHandler和io.LocalResourceController
     */
    private final JCEFHtmlPanel jcefHtmlPanel;
    /**
     * 显示MarkDown内容的容器
     */
    private final BorderLayoutPanel myComponent;
    /**
     * 项目当前启动的服务地址, 该数据将会写入HTML文件中
     */
    private final String serverPath = "http://localhost:" + BuiltInServerManager.getInstance().getPort() + LocalHttpRequestHandler.PREFIX;
    /**
     * 打开内容的类型
     */
    private final MarkdownContentType contentType;
    private final Map<String, Object> content;
    private final VirtualFile vFile;
    private static boolean add = false;

    /**
     * 不得不夸赞自己的机智, 幸好之前重构预留了足够多的内容, 不然适配deep coding就只能硬编码了
     * @param project project
     * @param content map
     * @param contentType 类型
     */
    public MarkDownEditor(@NotNull Project project, Map<String, Object> content, @NotNull MarkdownContentType contentType) {
        this.project = project;
        this.myComponent = JBUI.Panels.simplePanel();
        this.jcefHtmlPanel = new JCEFHtmlPanel("url");

        this.contentType = contentType;
        this.content = content;
        this.vFile = (VirtualFile) content.get(Constants.VFILE);

        // 自定义链接点击处理器
        // 适配deep coding 的 LC Competition功能
        // md, client全局唯一. 所以adapter只能添加一次
        CefClient client = jcefHtmlPanel.getCefBrowser().getClient();
        if (!add) {
            client.addLifeSpanHandler(createAdapter());
            add = true;
        }

        this.jcefHtmlPanel.loadHTML(loadHTMLContent());

        this.myComponent.addToCenter(jcefHtmlPanel.getComponent());
    }

    /**
     * 根据不同的markdown打开类型创建不同的适配器
     * 适配器拦截了点击事件, 并且根据点击的url类型, 进行不同的处理, 比如打开浏览器或通过LC-Runner预览
     * @return adp
     */
    private CefLifeSpanHandlerAdapter createAdapter() {
        CefLifeSpanHandlerAdapter adp;
        adp = new CefLifeSpanHandlerAdapter() {
            final int article = 0;
            final int question = 1;
            final int other = 2;

            class Info {
                int type;
                String value;
            }

            private Info getType(String url) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                String[] split = url.split("/");
                Info info = new Info();
                if (url.contains("circle/discuss")) {
                    info.type = article;
                    info.value = split[split.length - 1]; // uuid
                } else if (url.contains("problems")) {
                    // 找到problems字段, 他的下一个内容为titleSlug
                    for (int i = 0; i < split.length; i++) {
                        if ("problems".equals(split[i])) {
                            info.type = question;
                            if (i + 1 < split.length) {
                                // 判断后续内容是否存在solutions, 如果是, 则说明是题解, 跳转到web界面
                                for (int j = i + 2; j < split.length; j++) {
                                    if ("solutions".equals(split[j])) {
                                        info.type = other;
                                        return info;
                                    }
                                }
                                info.value = split[i + 1];
                            } else {
                                info.value = "NULL";
                            }
                        }
                    }
                } else {
                    info.type = 2;
                }
                return info;
            }

            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                if (target_url.endsWith(Constants.OPEN_ON_WBE)) {
                    String replace = target_url.replace(Constants.OPEN_ON_WBE, "");
                    openInDesktopBrowser(replace);
                    return true;
                }
                // 判断类型
                Info info = getType(target_url);
                switch (info.type) {
                    case article:
                        openArticle(target_url);
                        break;
                    case question:
                        openQuestion(info, target_url);
                        break;
                    case other:
                        openInDesktopBrowser(target_url);
                        break;
                }
                return true;
            }

            private void openQuestion(Info info, String targetUrl) {
                String titleSlug = info.value;
                if (titleSlug.equals("NULL")) {
                    openInDesktopBrowser(targetUrl);
                } else {
                    // 通过LC-Runner打开题目
                    var q = QuestionService.getInstance(project).getQuestionByTitleSlug(titleSlug, project);
                    if (q != null) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            CodeService.getInstance(project).openCodeEditor(q);
                        });
                    } else {
                        openInDesktopBrowser(targetUrl);
                    }
                }
            }

            private String extractLastSegment(String url) {
                String[] segments = url.split("/");
                return segments[segments.length - 1];
            }

            /**
             * 打开文章
             */
            private void openArticle(String targetUrl) {
                // 截取最后一段内容
                String last = extractLastSegment(targetUrl);
                String filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("0x3f").append("inner").append("[0x3f]-article-" + last).build();
                try {
                    FileUtils.createAndWriteFile(filePath, targetUrl);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
                        assert file != null;
                        OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                        FileEditorManager.getInstance(project).openTextEditor(ofd, false);
                    });
                } catch (IOException e) {
                    LogUtils.warn("article file create failed ! the reason is " + e.getMessage());
                    ApplicationManager.getApplication().invokeLater(() -> {
                        LightVirtualFile file = new LightVirtualFile("[0x3f]-article", targetUrl);
                        OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                        FileEditorManager.getInstance(project).openTextEditor(ofd, false);
                    });
                }
            }

            private void openInDesktopBrowser(String url) {
                try {
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (IOException e) {
                    LogUtils.error(e);
                }
            }
        };
        return adp;
    }

    private String loadHTMLContent() {
        // default template
        String templatePath = "\\template\\template.html";
        URL url = getClass().getResource(FileUtils.unUnifyPath(templatePath));
        if (url == null) {
            throw new RuntimeException("Template not found: " + templatePath);
        }
        try {
            String html = FileUtils.readContentFromFile(url);
            // Update resource paths to use the custom scheme
            html = html.replace("{{serverUrl}}", serverPath);
            switch (contentType) {
                case QUESTION:
                    html = html.replace("{{title}}", MapUtils.getString(content, Constants.FRONTEND_QUESTION_ID) + "." + MapUtils.getString(content, Constants.TRANSLATED_TITLE))
                            .replace("{{tag}}", getTag(MapUtils.getString(content, Constants.DIFFICULTY), MapUtils.getString(content, Constants.STATUS)))
                            .replace("{{webUrl}}", getWebUrl())
                            .replace("{{content}}", MapUtils.getString(content, Constants.QUESTION_CONTENT))
                    ;
                    break;
                case SOLUTION:
                    html = html.replace("{{title}}", "")
                            .replace("{{tag}}", "")
                            .replace("{{webUrl}}", getWebUrl())
                            .replace("{{content}}", MapUtils.getString(content, Constants.SOLUTION_CONTENT))
                    ;
                    break;
                case _0x3f:
                    String articleUrl = MapUtils.getString(content, Constants.ARTICLE_URL);
                    Article article = LeetcodeClient.getInstance(project).queryArticle(articleUrl);
                    articleUrl = "> [灵神原文的链接](" + articleUrl + Constants.OPEN_ON_WBE + ")\n";

                    html = html.replace("{{title}}", article.getTitle())
                            .replace("{{tag}}", "")
                            .replace("{{webUrl}}", articleUrl)
                            .replace("{{content}}", article.getContent())
                    ;
                    break;
                default:

            }
            String vditorTheme = getVditorTheme();
            html = html.replace("\"{{theme}}\"", "\"" + vditorTheme + "\"");
            if (vditorTheme.equals("dark")) {
                html = html.replace("{{css}}", getDarkCss());
            } else {
                html = html.replace("{{css}}", "");
            }
            // handle html
            return html;
        } catch (Exception e) {
            LogUtils.warn("load Markdown content error!!\n" + "content = " + GsonUtils.toJsonStr(content) + "\n\r" +
                    "contentType = " + contentType.toString() + "\n\r" + "serverPath = " + serverPath);
            LogUtils.error("Failed to load Markdown content", e);
            ConsoleUtils.getInstance(project).showError("无法加载 Markdown 内容!\n" + DebugUtils.getStackTraceAsString(e), false, true);
            return "无法加载 Markdown 内容 !!";
        }
    }

    private String getDarkCss() {
        return "<style id=\"ideaStyle\">.vditor--dark{--panel-background-color:rgba(43,43,43,1.00);--textarea-background-color:rgba(43,43,43,1.00);--toolbar-background-color:rgba(60,63,65,1.00);}::-webkit-scrollbar-track {background-color:rgba(43,43,43,1.00);}::-webkit-scrollbar-thumb {background-color:rgba(166,166,166,0.28);}.vditor-reset {font-size:16px;font-family:\"JetBrains Mono\",\"Helvetica Neue\",\"Luxi Sans\",\"DejaVu Sans\",\"Hiragino Sans GB\",\"Microsoft Yahei\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Noto Color Emoji\",\"Segoe UI Symbol\",\"Android Emoji\",\"EmojiSymbols\";color:rgba(169,183,198,1.00);} body{background-color: rgba(43,43,43,1.00);}.vditor-reset a {color: rgba(30, 136, 234);}</style>";
    }

    public String getVditorTheme() {
        // 获取当前编辑器的颜色方案
        EditorColorsManager colorsManager = EditorColorsManager.getInstance();
        EditorColorsScheme globalScheme = colorsManager.getGlobalScheme();

        // 直接获取默认背景颜色
        Color defaultBackground = globalScheme.getDefaultBackground();

        // 判断主题类型
        String vditorTheme;
        if (ColorUtil.isDark(defaultBackground)) {
            vditorTheme = "dark";
        } else {
            vditorTheme = "light";
        }
        return vditorTheme;
    }

    /**
     * 获取当前渲染界面的leetcode web url
     * @return
     */
    private String getWebUrl() {
        if (contentType == MarkdownContentType.SOLUTION) {
            return solutionWebUrl();
        } else if (contentType == MarkdownContentType.QUESTION) {
            return questionWebUrl();
        }
        return "";
    }

    /**
     * 返回题目内容对应的web url
     * @return web url
     */
    private String questionWebUrl() {
        if (content.isEmpty()) {
            return "";
        }
        String titleSlug = MapUtils.getString(content, Constants.TITLE_SLUG);
        if (StringUtils.isBlank(titleSlug)) {
            LogUtils.warn("question web url failed!!\n\r" + "titleSlug = " + titleSlug);
            return "";
        }
        return "<a rel=\"stylesheet\" href=\"" + LeetcodeApiUtils.getQuestionUrl(titleSlug) + Constants.OPEN_ON_WBE + "\">在浏览器上访问</a><p>";
    }

    /**
     * 返回solution的web url
     * @return web url
     */
    private String solutionWebUrl() {
        if (content.isEmpty()) {
            return "";
        }
        String titleSlug = MapUtils.getString(content, Constants.TITLE_SLUG);
        String topicId = MapUtils.getString(content, Constants.TOPIC_ID);
        String solutionSlug = MapUtils.getString(content, Constants.SOLUTION_SLUG);
        // 要求三者全部不为blank
        if (StringUtils.isBlank(titleSlug) ||
                StringUtils.isBlank(topicId) ||
                StringUtils.isBlank(solutionSlug)
        ) {
            LogUtils.warn("solution web url failed!!\n\r" +
                    "titleSlug = " + titleSlug + " " +
                    "topicId = " + topicId + " " +
                    "solutionSlug = " + solutionSlug);
            return "";
        }

//        String html = "<a rel=\"stylesheet\" href=\"" + LeetcodeApiUtils.getSolutionUrl(titleSlug, topicId, solutionSlug) + "\">在浏览器上访问</a><p>";
        return "> [在浏览器上访问](" + LeetcodeApiUtils.getSolutionUrl(titleSlug, topicId, solutionSlug) + Constants.OPEN_ON_WBE + ")";
    }

    public String getTag(String difficulty, String status) {
        /*
          EASY
          MEDIUM
          HARD
        */
        String color;
        String text;
        String[] easy = {"(ˉ▽￣～) 切~~, 有手就行", "洒洒水啦", "简简单单"};
        String[] medium = {"呦, 有点意思", "rua圾题目", "小小中等题"};
        String[] hard = {"溜了溜了", "这啥玩意儿这是", "cv题目"};
        String translatedDifficulty;
        switch (difficulty.toUpperCase()) { // 将输入转换为大写以确保匹配不区分大小写
            case "EASY":
                color = "green";
                text = easy[RandomUtils.nextInt(0, easy.length - 1)];
                translatedDifficulty = "【简单题】";
                break;
            case "MEDIUM":
                color = "orange";
                text = medium[RandomUtils.nextInt(0, medium.length - 1)];
                translatedDifficulty = "【中等题】";
                break;
            case "HARD":
                color = "red";
                text = hard[RandomUtils.nextInt(0, hard.length - 1)];
                translatedDifficulty = "【困难题】";
                break;
            default:
                color = "gray";
                text = "未知";
                translatedDifficulty = "【未知题】";
                break;
        }
        String solvedTag;
        if (StringUtils.isBlank(status)) {
            solvedTag = "";
        } else {
            if ("AC".equals(status)) {
                solvedTag = "<div >已解答 <svg t=\"1739596183696\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"1572\" width=\"16\" height=\"16\"><path d=\"M512 30.72C246.272 30.72 30.72 246.272 30.72 512s215.552 481.28 481.28 481.28 481.28-215.552 481.28-481.28-215.552-481.28-481.28-481.28z m-13.312 632.32L440.32 721.408l-58.368-58.368-145.408-143.872L294.912 460.8 440.32 605.184 728.576 317.44l58.368 58.368-288.256 287.232z\" fill=\"#4CB16D\" p-id=\"1573\"></path></svg></div>";
            } else if ("TRIED".equals(status)) {
                solvedTag = "<div >尝试过 <svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 14 14\" width=\"1em\" height=\"1em\" fill=\"currentColor\" class=\"text-message-warning dark:text-message-warning\"><path d=\"M6.998 7v-.6a.6.6 0 00-.6.6h.6zm.05 0h.6a.6.6 0 00-.6-.6V7zm0 .045v.6a.6.6 0 00.6-.6h-.6zm-.05 0h-.6a.6.6 0 00.6.6v-.6zm5-.045a5 5 0 01-5 5v1.2a6.2 6.2 0 006.2-6.2h-1.2zm-5 5a5 5 0 01-5-5h-1.2a6.2 6.2 0 006.2 6.2V12zm-5-5a5 5 0 015-5V.8A6.2 6.2 0 00.798 7h1.2zm5-5a5 5 0 015 5h1.2a6.2 6.2 0 00-6.2-6.2V2zm2.2 5a2.2 2.2 0 01-2.2 2.2v1.2a3.4 3.4 0 003.4-3.4h-1.2zm-2.2 2.2a2.2 2.2 0 01-2.2-2.2h-1.2a3.4 3.4 0 003.4 3.4V9.2zM4.798 7a2.2 2.2 0 012.2-2.2V3.6a3.4 3.4 0 00-3.4 3.4h1.2zm2.2-2.2a2.2 2.2 0 012.2 2.2h1.2a3.4 3.4 0 00-3.4-3.4v1.2zm0 2.8h.05V6.4h-.05v1.2zm-.55-.6v.045h1.2V7h-1.2zm.6-.555h-.05v1.2h.05v-1.2zm.55.6V7h-1.2v.045h1.2z\" fill=\"#FFA500\"></path></svg></div>";
            } else {
                solvedTag = "";
            }
        }
        return solvedTag + " <span style='color: " + color + ";'>" + text + translatedDifficulty + "</span>";
    }

    @Override
    public @NotNull JComponent getComponent() {
        return this.myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return jcefHtmlPanel.getComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Markdown";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void dispose() {
        jcefHtmlPanel.dispose();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return (T) map.get(key);
    }

    private final Map<Key<?>, Object> map = new HashMap<>();

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        map.put(key, value);
    }

    /**
     * 兼容不同版本, 早期版本super没有实现该方法
     * @return null
     */
    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public VirtualFile getFile() {
        return vFile;
    }
}
