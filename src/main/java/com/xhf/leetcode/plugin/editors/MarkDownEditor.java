package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
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
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.LocalHttpRequestHandler;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.Article;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.MarkdownContentType;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import java.awt.Color;
import java.awt.Desktop;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
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

/**
 * support html or markdown show ability
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MarkDownEditor implements FileEditor {

    private static boolean add = false;
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
    private final String serverPath =
        "http://localhost:" + BuiltInServerManager.getInstance().getPort() + LocalHttpRequestHandler.PREFIX;
    /**
     * 打开内容的类型
     */
    private final MarkdownContentType contentType;
    private final Map<String, Object> content;
    private final VirtualFile vFile;
    private final Map<Key<?>, Object> map = new HashMap<>();

    /**
     * 不得不夸赞自己的机智, 幸好之前重构预留了足够多的内容, 不然适配deep coding就只能硬编码了
     *
     * @param project project
     * @param content map
     * @param contentType 类型
     */
    public MarkDownEditor(@NotNull Project project, Map<String, Object> content,
        @NotNull MarkdownContentType contentType) {
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

    @NotNull
    private static String getString(boolean isPaidOnly) {
        String paidOnlyTag = "";
        if (isPaidOnly) {
            paidOnlyTag = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 29 16\" height=\"1em\" fill=\"currentColor\" class=\"ml-2 h-4 shrink-0\"><path fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M0 5C0 2.23858 2.23858 0 5 0H24C26.7614 0 29 2.23858 29 5V11C29 13.7614 26.7614 16 24 16H5C2.23858 16 0 13.7614 0 11V5Z\" fill=\"#FFA116\"></path><path fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M6.64001 11.3335C6.79952 11.3335 6.93787 11.2749 7.05505 11.1577C7.17224 11.047 7.23083 10.9087 7.23083 10.7427V8.89209H8.62732C9.34347 8.89209 9.95382 8.63818 10.4584 8.13037C10.9662 7.62256 11.2201 7.01221 11.2201 6.29932C11.2201 5.58317 10.9662 4.97119 10.4584 4.46338C9.95382 3.95882 9.34347 3.70654 8.62732 3.70654C7.91443 3.70654 7.30408 3.95882 6.79626 4.46338C6.28845 4.97119 6.03455 5.58317 6.03455 6.29932V10.7427C6.03455 10.9087 6.09314 11.047 6.21033 11.1577C6.32751 11.2749 6.46586 11.3335 6.62537 11.3335H6.64001ZM8.62732 7.6958H7.23083V6.29932C7.23083 5.91195 7.36593 5.58154 7.63611 5.30811C7.90955 5.03467 8.23995 4.89795 8.62732 4.89795C9.01469 4.89795 9.34509 5.03467 9.61853 5.30811C9.88871 5.58154 10.0238 5.91195 10.0238 6.29932C10.0238 6.68669 9.88871 7.01546 9.61853 7.28564C9.34509 7.55908 9.01469 7.6958 8.62732 7.6958ZM13.266 11.1577C13.1488 11.2749 13.0105 11.3335 12.851 11.3335H12.8363C12.6768 11.3335 12.5385 11.2749 12.4213 11.1577C12.3041 11.047 12.2455 10.9087 12.2455 10.7427V4.29736C12.2455 4.1346 12.3041 3.99463 12.4213 3.87744C12.5385 3.76351 12.6768 3.70654 12.8363 3.70654H12.851C13.0105 3.70654 13.1488 3.76351 13.266 3.87744C13.3832 3.99463 13.4418 4.1346 13.4418 4.29736V10.7427C13.4418 10.9087 13.3832 11.047 13.266 11.1577ZM16.9769 11.3335C17.6247 11.3335 18.1797 11.1024 18.642 10.6401C19.1042 10.1779 19.3353 9.62288 19.3353 8.9751V6.3042C19.3353 6.14144 19.2767 6.00146 19.1595 5.88428C19.0489 5.76709 18.9105 5.7085 18.7445 5.7085H18.7347C18.572 5.7085 18.432 5.76709 18.3148 5.88428C18.1976 6.00146 18.139 6.14144 18.139 6.3042V8.9751C18.139 9.29736 18.0267 9.57243 17.8021 9.80029C17.5743 10.0249 17.2992 10.1372 16.9769 10.1372C16.6514 10.1372 16.3747 10.0249 16.1469 9.80029C15.9222 9.57243 15.8099 9.29736 15.8099 8.9751V6.3042C15.8099 6.14144 15.7513 6.00146 15.6342 5.88428C15.517 5.76709 15.3786 5.7085 15.2191 5.7085H15.2045C15.045 5.7085 14.9066 5.76709 14.7894 5.88428C14.6722 6.00146 14.6136 6.14144 14.6136 6.3042V8.9751C14.6136 9.62288 14.8448 10.1779 15.307 10.6401C15.7692 11.1024 16.3259 11.3335 16.9769 11.3335ZM23.1879 10.8354C22.8526 11.1675 22.4506 11.3335 21.9818 11.3335H20.7074C20.5479 11.3335 20.4095 11.2749 20.2924 11.1577C20.1752 11.047 20.1166 10.9087 20.1166 10.7427V10.7329C20.1166 10.5701 20.1752 10.4302 20.2924 10.313C20.4095 10.1958 20.5479 10.1372 20.7074 10.1372H22.0306C22.1739 10.1372 22.2943 10.0884 22.392 9.99072C22.4896 9.89307 22.5385 9.77262 22.5385 9.62939C22.5385 9.45036 22.4457 9.31527 22.2601 9.22412C22.2113 9.20133 21.9379 9.11995 21.4398 8.97998C21.0524 8.86605 20.7644 8.71631 20.5756 8.53076C20.3054 8.26709 20.1703 7.89437 20.1703 7.4126C20.1703 6.94385 20.3363 6.54346 20.6683 6.21143C21.0036 5.87614 21.4056 5.7085 21.8744 5.7085H22.7728C22.9356 5.7085 23.0756 5.76709 23.1927 5.88428C23.3099 6.00146 23.3685 6.14144 23.3685 6.3042V6.31396C23.3685 6.47998 23.3099 6.61833 23.1927 6.729C23.0756 6.84619 22.9356 6.90479 22.7728 6.90479H21.8256C21.6823 6.90479 21.5619 6.95361 21.4642 7.05127C21.3666 7.15218 21.3177 7.27262 21.3177 7.4126C21.3177 7.60791 21.4122 7.75439 21.601 7.85205C21.6661 7.88786 21.9379 7.96924 22.4164 8.09619C22.8038 8.20687 23.0918 8.35173 23.2806 8.53076C23.5508 8.78792 23.6859 9.15413 23.6859 9.62939C23.6859 10.0981 23.5199 10.5002 23.1879 10.8354Z\" fill=\"white\"></path></svg>";
        }
        return paidOnlyTag;
    }

    /**
     * 根据不同的markdown打开类型创建不同的适配器
     * 适配器拦截了点击事件, 并且根据点击的url类型, 进行不同的处理, 比如打开浏览器或通过LC-Runner预览
     *
     * @return adp
     */
    private CefLifeSpanHandlerAdapter createAdapter() {
        CefLifeSpanHandlerAdapter adp;
        adp = new CefLifeSpanHandlerAdapter() {
            final int article = 0;
            final int question = 1;
            final int other = 2;

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
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url,
                String target_frame_name) {
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
                            try {
                                CodeService.getInstance(project).openCodeEditor(q);
                            } catch (FileCreateError e) {
                                LogUtils.error(e);
                                ConsoleUtils.getInstance(project).showError(
                                    BundleUtils.i18n("code.service.file.create.error") + "\n" + e.getMessage(), true,
                                    true);
                            }
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
                String filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("0x3f")
                    .append("inner").append("[0x3f]-article-" + last).build();
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

            class Info {

                int type;
                String value;
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
                    html = html.replace("{{title}}",
                            MapUtils.getString(content, Constants.FRONTEND_QUESTION_ID) + "." + MapUtils.getString(content,
                                Constants.TRANSLATED_TITLE))
                        .replace("{{tag}}", getTag(MapUtils.getString(content, Constants.DIFFICULTY),
                            MapUtils.getString(content, Constants.STATUS)))
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
            LogUtils.warn(
                "load Markdown content error!!\n" + "content = " + com.xhf.leetcode.plugin.utils.MapUtils.toString(
                    content) + "\n\r" +
                    "contentType = " + contentType.toString() + "\n\r" + "serverPath = " + serverPath);
            LogUtils.error(BundleUtils.i18n("editor.focus.load.error"), e);
            ConsoleUtils.getInstance(project)
                .showError(BundleUtils.i18n("editor.focus.load.error") + "\n" + DebugUtils.getStackTraceAsString(e),
                    false, true);
            return BundleUtils.i18n("editor.focus.load.error");
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
     *
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
        return "<a rel=\"stylesheet\" href=\"" + LeetcodeApiUtils.getQuestionUrl(titleSlug) + Constants.OPEN_ON_WBE
            + "\">" + BundleUtils.i18n("leetcode.web") + "</a><p>";
    }

    /**
     * 返回solution的web url
     *
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
        return "> [" + BundleUtils.i18n("leetcode.web") + "](" + LeetcodeApiUtils.getSolutionUrl(titleSlug, topicId,
            solutionSlug) + Constants.OPEN_ON_WBE + ")";
    }

    public String getTag(String difficulty, String status) {
        /*
          EASY
          MEDIUM
          HARD
        */
        String color;
        String text;
        String[] easy = {BundleUtils.i18n("editor.markdown.easy1"), BundleUtils.i18n("editor.markdown.easy2"),
            BundleUtils.i18n("editor.markdown.easy3")};
        String[] medium = {BundleUtils.i18n("editor.markdown.medium1"), BundleUtils.i18n("editor.markdown.medium2"),
            BundleUtils.i18n("editor.markdown.medium3")};
        String[] hard = {BundleUtils.i18n("editor.markdown.hard1"), BundleUtils.i18n("editor.markdown.hard2"),
            BundleUtils.i18n("editor.markdown.hard3")};
        String translatedDifficulty;
        switch (difficulty.toUpperCase()) { // 将输入转换为大写以确保匹配不区分大小写
            case "EASY":
                color = "green";
                text = easy[RandomUtils.nextInt(0, easy.length - 1)];
                translatedDifficulty = "【" + BundleUtils.i18n("editor.markdown.easy") + "】";
                break;
            case "MEDIUM":
                color = "orange";
                text = medium[RandomUtils.nextInt(0, medium.length - 1)];
                translatedDifficulty = "【" + BundleUtils.i18n("editor.markdown.medium") + "】";
                break;
            case "HARD":
                color = "red";
                text = hard[RandomUtils.nextInt(0, hard.length - 1)];
                translatedDifficulty = "【" + BundleUtils.i18n("editor.markdown.hard") + "】";
                break;
            default:
                color = "gray";
                text = BundleUtils.i18nHelper("未知", "unknown");
                translatedDifficulty = "【未知题】";
                break;
        }

        boolean isPaidOnly = MapUtils.getBooleanValue(content, Constants.IS_PAID_ONLY, false);
        String paidOnlyTag = getString(isPaidOnly);

        String solvedTag;
        if (StringUtils.isBlank(status)) {
            solvedTag = "";
        } else {
            if ("AC".equals(status)) {
                solvedTag = BundleUtils.i18n("leetcode.solved")
                    + "<svg t=\"1739596183696\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"1572\" width=\"16\" height=\"16\"><path d=\"M512 30.72C246.272 30.72 30.72 246.272 30.72 512s215.552 481.28 481.28 481.28 481.28-215.552 481.28-481.28-215.552-481.28-481.28-481.28z m-13.312 632.32L440.32 721.408l-58.368-58.368-145.408-143.872L294.912 460.8 440.32 605.184 728.576 317.44l58.368 58.368-288.256 287.232z\" fill=\"#4CB16D\" p-id=\"1573\"></path></svg>";
            } else if ("TRIED".equals(status)) {
                solvedTag = BundleUtils.i18n("leetcode.tried")
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 14 14\" width=\"1em\" height=\"1em\" fill=\"currentColor\" class=\"text-message-warning dark:text-message-warning\"><path d=\"M6.998 7v-.6a.6.6 0 00-.6.6h.6zm.05 0h.6a.6.6 0 00-.6-.6V7zm0 .045v.6a.6.6 0 00.6-.6h-.6zm-.05 0h-.6a.6.6 0 00.6.6v-.6zm5-.045a5 5 0 01-5 5v1.2a6.2 6.2 0 006.2-6.2h-1.2zm-5 5a5 5 0 01-5-5h-1.2a6.2 6.2 0 006.2 6.2V12zm-5-5a5 5 0 015-5V.8A6.2 6.2 0 00.798 7h1.2zm5-5a5 5 0 015 5h1.2a6.2 6.2 0 00-6.2-6.2V2zm2.2 5a2.2 2.2 0 01-2.2 2.2v1.2a3.4 3.4 0 003.4-3.4h-1.2zm-2.2 2.2a2.2 2.2 0 01-2.2-2.2h-1.2a3.4 3.4 0 003.4 3.4V9.2zM4.798 7a2.2 2.2 0 012.2-2.2V3.6a3.4 3.4 0 00-3.4 3.4h1.2zm2.2-2.2a2.2 2.2 0 012.2 2.2h1.2a3.4 3.4 0 00-3.4-3.4v1.2zm0 2.8h.05V6.4h-.05v1.2zm-.55-.6v.045h1.2V7h-1.2zm.6-.555h-.05v1.2h.05v-1.2zm.55.6V7h-1.2v.045h1.2z\" fill=\"#FFA500\"></path></svg>";
            } else {
                solvedTag = "";
            }
        }

        String tag = "";
        if (StringUtils.isNotBlank(paidOnlyTag) || StringUtils.isNotBlank(solvedTag)) {
            tag = "<div>" + solvedTag + " " + paidOnlyTag + "</div>";
        }

        return tag + " <span style='color: " + color + ";'>" + text + translatedDifficulty + "</span>";
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

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        map.put(key, value);
    }

    /**
     * 兼容不同版本, 早期版本super没有实现该方法
     *
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
