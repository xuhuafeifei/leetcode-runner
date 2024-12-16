package com.xhf.leetcode.plugin.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LocalHttpRequestHandler;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.utils.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.net.URL;
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
     * lc存储service于editor之间沟通的数据信息
     */
    /**
     * 打开内容的类型
     */
    private final MarkdownContentType contentType;
    private final Map<String, Object> content;

    public MarkDownEditor(@NotNull Project project, Map<String, Object> content, @NotNull MarkdownContentType contentType) {
        this.project = project;
        this.myComponent = JBUI.Panels.simplePanel();
        this.jcefHtmlPanel = new JCEFHtmlPanel("url");
        this.contentType = contentType;
        this.content = content;

        this.jcefHtmlPanel.loadHTML(loadHTMLContent());

        this.myComponent.addToCenter(jcefHtmlPanel.getComponent());
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
                            .replace("{{tag}}", getTag(MapUtils.getString(content, Constants.DIFFICULTY)))
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
                default:

            }
            // handle html
            return html;
        } catch (Exception e) {
            LogUtils.warn("load Markdown content!!\n" + "content = " + GsonUtils.toJsonStr(content) + "\n\r" +
                    "contentType = " + contentType.toString() + "\n\r" + "serverPath = " + serverPath);
            LogUtils.error("Failed to load Markdown content", e);
            ConsoleUtils.getInstance(project).showWaring("Failed to load Markdown content");
            return "Failed to load Markdown content !!";
        }
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
        String html = "<a rel=\"stylesheet\" href=\"" + LeetcodeApiUtils.getQuestionUrl(titleSlug) + "\">在浏览器上访问</a><p>";
        return html;
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
        String markdown = "> [在浏览器上访问](" + LeetcodeApiUtils.getSolutionUrl(titleSlug, topicId, solutionSlug)+ ")";
        return markdown;
    }

    public String getTag(String difficulty) {
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
        return "<span style='color: " + color + ";'>" + text + translatedDifficulty + "</span>";
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
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }
}
