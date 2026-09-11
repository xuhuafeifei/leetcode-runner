package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xhf.leetcode.plugin.actions.utils.ActionUtils;
import com.xhf.leetcode.plugin.bus.CodeSubmitEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.RePositionEvent;
import com.xhf.leetcode.plugin.bus.TimeStopEvent;
import com.xhf.leetcode.plugin.bus.TodayQuestionOkEvent;
import com.xhf.leetcode.plugin.comp.TestCaseDialog;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.PythonCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.editors.SplitTextEditorWithPreview;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.BaseCodeResult;
import com.xhf.leetcode.plugin.model.DeepCodingInfo;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.model.RunCode;
import com.xhf.leetcode.plugin.model.RunCodeResult;
import com.xhf.leetcode.plugin.model.SubmitCodeResult;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.setting.LanguageConvertor;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.Safe;
import com.xhf.leetcode.plugin.utils.TaskCenter;
import com.xhf.leetcode.plugin.utils.UnSafe;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.TimerWindow;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CodeService {

    public static String DIR = "temp";

    // 单例
    private static volatile CodeService instance;
    private final Project project;

    public CodeService(Project project) {
        this.project = project;
    }

    // 单例
    public static CodeService getInstance(Project project) {
        if (instance == null) {
            synchronized (CodeService.class) {
                if (instance == null) {
                    instance = new CodeService(project);
                }
            }
        }
        return instance;
    }

    /**
     * fill question and open code editor with preview content function
     *
     * @param question question
     */
    public void openCodeEditor(Question question) throws FileCreateError {
        // 判断题目是否付费，以及用户VIP身份
        if (question.getIsPaidOnly() && !LoginService.getInstance(project).isPremium()) {
            ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("vip.question"), false, true);
            return;
        }
        QuestionService.getInstance(project).fillQuestion(question, project);

        String codeFilePath;
        codeFilePath = storeLeetcodeEditorAndGetStorePath(question, null);

        // open code editor and load content
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(codeFilePath);
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    /**
     * deep coding 模式 创建代码
     *
     * @param question question
     * @param deepCodingInfo dci
     */
    public void openCodeEditor(Question question, DeepCodingInfo deepCodingInfo) throws FileCreateError {
        QuestionService.getInstance(project).fillQuestion(question, project);

        String codeFilePath;
        codeFilePath = storeLeetcodeEditorAndGetStorePath(question, deepCodingInfo);

        // open code editor and load content
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(codeFilePath);
            if (file != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    private String storeLeetcodeEditorAndGetStorePath(@NotNull Question question,
        @Nullable DeepCodingInfo deepCodingInfo) throws FileCreateError {
        String codeFilePath = createCodeFile(question);
        analysisAndCreateFile(question);

        AppSettings app = AppSettings.getInstance();
        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), app.getLangType());
        if (deepCodingInfo != null) {
            le.setDeepCodingInfo(deepCodingInfo);
        }

        // store path info
        StoreService.getInstance(project).addCache(codeFilePath, le);

        return codeFilePath;
    }

    /**
     * 创建Question对应的编码文件, 创建并存储LeetcodeEditor, 并返回文件路径
     * <p>
     * 该方法创建Question对应文件时会判断当前系统的 reposition设置. 如果是默认打开方式, 则会通过系统的langType判断文件后缀
     * 否则则会通过入参fileLangType判断文件代表类型
     *
     * @param question question
     * @param deepCodingInfo dci
     * @param fileLangType 文件代表的语言类型
     * @return 文件存储路径
     */
    private String storeLeetcodeEditorAndGetStorePath(@NotNull Question question,
        @Nullable DeepCodingInfo deepCodingInfo, String fileLangType) throws FileCreateError {
        AppSettings app = AppSettings.getInstance();
        String reposition = app.getReposition();
        String fileName = question.getFileName();

        if (StringUtils.isBlank(reposition)) {
            // todo: 修改打开逻辑. 如果是默认打开方式
            fileName = fileName + app.getFileTypeSuffix();
        } else {
            LangType langType = LangType.getType(app.getLangType());
            if (langType == null) {
                LogUtils.error(
                    BundleUtils.i18n("leetcode.status.unknown") + " langType! langType = " + app.getLangType());
                fileName = fileName + app.getFileTypeSuffix();
            } else {
                if (LanguageConvertor.isEqual(reposition, AppSettings.REPOSITION_DEFAULT)) {
                    langType = LangType.getType(fileLangType);
                    if (langType == null) {
                        String msg = BundleUtils.i18nHelper(
                            "当前打开文件代表的fileLangType无法识别! fileLangType = " + fileLangType,
                            "Current file represents the fileLangType cannot be recognized! fileLangType = "
                                + fileLangType);
                        LogUtils.error(msg);
                        // event 传入的文件langType有问题, 回退到系统默认文件后缀
                        fileName = fileName + app.getFileTypeSuffix();
                    } else {
                        fileName = fileName + langType.getSuffix();
                    }
                } else if (LanguageConvertor.isEqual(reposition, AppSettings.REPOSITION_SETTING)) {
                    fileName = fileName + langType.getSuffix();
                } else {
                    LogUtils.error(
                        BundleUtils.i18n("leetcode.status.unknown") + " reposition! reposition = " + reposition + "\n"
                            + AppSettings.getInstance().toString());
                    fileName = fileName + app.getFileTypeSuffix();
                }
            }
        }

        // create code file
        String codeFilePath = createCodeFile(question, fileName);
        analysisAndCreateFile(question);

        LeetcodeEditor le = buildLeetcodeEditor(question, question.getTranslatedContent(), app.getLangType());
        if (deepCodingInfo != null) {
            le.setDeepCodingInfo(deepCodingInfo);
        }

        // store path info
        StoreService.getInstance(project).addCache(codeFilePath, le);

        return codeFilePath;
    }

    /**
     * 分析代码, 同时创建文件
     * 主要服务于包含ListNode, TreeNode的题目
     *
     * @param question question
     */
    private void analysisAndCreateFile(Question question) {
        String codeSnippets = question.getCodeSnippets();
        if (StringUtils.isBlank(codeSnippets)) {
            return;
        }
        // 分析代码
        LangType langType = LangType.getType(AppSettings.getInstance().getLangType());
        if (langType == null) {
            LogUtils.warn("langType is null !!!");
            return;
        }
        switch (langType) {
            case JAVA:
                AnalysisResult a1 = new JavaCodeAnalyzer(project).analyze(codeSnippets);
                if (contains(a1, "ListNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/java/ListNode.java")),
                        "ListNode.java");
                }
                if (contains(a1, "TreeNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/java/TreeNode.java")),
                        "TreeNode.java");
                }
                return;
            case PYTHON3:
                AnalysisResult a2 = new PythonCodeAnalyzer(project).analyze(codeSnippets);
                if (contains(a2, "ListNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/python/ListNode.py")),
                        "ListNode.py");
                }
                if (contains(a2, "TreeNode")) {
                    createCodeFile(FileUtils.readContentFromFile(getClass().getResource("/debug/python/TreeNode.py")),
                        "TreeNode.py");
                }
                return;
            default:
                LogUtils.warn("当前语言并不支持自动创建ListNode或TreeNode, langType = " + langType.getLangType());
        }
    }

    private boolean contains(AnalysisResult ar, String paramTypeName) {
        for (String parameterType : ar.getParameterTypes()) {
            if (parameterType.contains(paramTypeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 该方法会关闭当前打开文件, 重新打开
     * 之所以关闭, 是因为只有关闭后, 才能够使用系统提供的编辑器显示
     */
    public void reOpenCodeEditor(Question question, VirtualFile file, String langType) throws FileCreateError {
        QuestionService.getInstance(project).fillQuestion(question, project);

        // restore
        String codeFilePath = storeLeetcodeEditorAndGetStorePath(question, null, langType);

        // open code editor and load content
        openCodeEditor(file, codeFilePath);
    }

    public void reOpenCodeEditor(Question question, VirtualFile file, String langType, DeepCodingInfo deepCodingInfo)
        throws FileCreateError {
        QuestionService.getInstance(project).fillQuestion(question, project);

        // restore
        String codeFilePath;
        codeFilePath = storeLeetcodeEditorAndGetStorePath(question, deepCodingInfo, langType);

        openCodeEditor(file, codeFilePath);
    }

    private void openCodeEditor(VirtualFile file, String codeFilePath) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ViewUtils.closeVFile(file, project);
            VirtualFile newfile = LocalFileSystem.getInstance().findFileByPath(codeFilePath);
            if (newfile != null) {
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, newfile);
                FileEditorManager.getInstance(project).openTextEditor(ofd, false);
            }
        });
    }

    private LeetcodeEditor buildLeetcodeEditor(Question question, String translatedContent, String lang) {
        LeetcodeEditor le = new LeetcodeEditor();
        le.setLang(lang);
        le.setQuestionId(question.getQuestionId());
        le.setTitleSlug(question.getTitleSlug());
        le.setExampleTestcases(question.getExampleTestcases());
        le.setDefaultTestcases(question.getExampleTestcases());
        le.setFrontendQuestionId(question.getFrontendQuestionId());
        // le.setMarkdownPath(markdownFilePath);
        le.setMarkdownContent(translatedContent);
        le.setTranslatedTitle(question.getTranslatedTitle());
        le.setDifficulty(question.getDifficulty());
        le.setDebugTestcase(question.getExampleTestcases());
        le.setStatus(question.getStatus());
        le.setPaidOnly(question.getIsPaidOnly());
        return le;
    }

    @Deprecated // not used
    private String createContentFile(Question question) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
            .append("content")
            .append(question.getFileName() + ".md")
            .build();

        try {
            if (FileUtils.fileExists(filePath)) {
                return filePath;
            }
            FileUtils.createAndWriteFile(filePath, question.getTranslatedContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }

    /**
     * 根据文件名称 + 文件内容, 创建文件
     *
     * @param content content
     * @param fileName fileName
     * @return filePath
     */
    private String createCodeFile(String content, String fileName) {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
            // .append("temp")
            .append(fileName)
            .build();

        try {
            FileUtils.createAndWriteFile(filePath, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 刷新文件系统
        LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
        return filePath;
    }

    private String createCodeFile(Question question) throws FileCreateError {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
            // .append("temp")
            .append(getCodeFileName(question))
            .build();

        try {
            if (FileUtils.fileExists(filePath)) {
                return filePath;
            }
            String content = buildQuestionComment(question) + question.getCodeSnippets();
            FileUtils.createAndWriteFile(filePath, content);
        } catch (IOException e) {
            throw new FileCreateError(e);
        }

        return filePath;
    }

    /**
     * 构建题目描述注释, 以注释形式写入代码文件头部
     * 方便用户在刷题时看到题目描述, 不用切回 LeetCode 页面
     */
    private String buildQuestionComment(Question question) {
        String commentSymbol = LangType.getCommentSymbol(AppSettings.getInstance().getLangType());
        if (commentSymbol == null) {
            commentSymbol = "//";
        }
        StringBuilder sb = new StringBuilder();
        // 标题
        sb.append(commentSymbol).append(" ").append(question.getTranslatedTitle()).append("\n");
        sb.append(commentSymbol).append("\n");
        // 题目描述 (HTML 转纯文本)
        String desc = question.getTranslatedContent();
        if (desc != null && !desc.isEmpty()) {
            // 去掉 HTML 标签
            String plain = desc.replaceAll("<[^>]+>", "");
            // 去掉连续空行
            plain = plain.replaceAll("\\n{2,}", "\\n");
            for (String line : plain.split("\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    sb.append(commentSymbol).append(" ").append(trimmed).append("\n");
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String createCodeFile(Question question, String fileName) throws FileCreateError {
        String filePath = AppSettings.getInstance().getFilePath();
        filePath = new FileUtils.PathBuilder(filePath)
            .append(fileName)
            .build();

        try {
            if (FileUtils.fileExists(filePath)) {
                return filePath;
            }
            FileUtils.createAndWriteFile(filePath, question.getCodeSnippets());
        } catch (IOException e) {
            throw new FileCreateError(e);
        }

        return filePath;
    }

    /**
     * 通过question创建文件名
     *
     * @param question question
     * @return 获取文件名
     */
    public String getCodeFileName(Question question) {
        return question.getFileName() + AppSettings.getInstance().getFileTypeSuffix();
    }

    /**
     * 通过文件名称反解析question.fid
     *
     * @param filePath 打开的虚拟文件的路径
     * @return frontedQuestionId
     */
    public String parseFidFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseFrontendQuestionId(fileName);
    }

    /**
     * 通过vFile反解析question.fid
     *
     * @param file idea打开的虚拟文件
     * @return frontedQuestionId
     */
    public String parseFidFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseFidFromFileName(filePath);
    }

    /**
     * 通过文件名称反解析question.titleSlug
     *
     * @param filePath 打开的虚拟文件的路径
     * @return 解析出titleSlug
     */
    public String parseTitleSlugFromFileName(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseTitleSlug(fileName);
    }

    /**
     * 通过vFile反解析question.titleSlug
     *
     * @param file idea打开的虚拟文件
     * @return 解析titleSlug
     */
    public String parseTitleSlugFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseTitleSlugFromFileName(filePath);
    }

    /**
     * 通过文件名反解析question的langType
     *
     * @param filePath 打开的虚拟文件的路径
     */
    private String parseLangType(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        return Question.parseLangType(fileName);
    }

    /**
     * 通过vFile反解析langType
     *
     * @param file idea打开的虚拟文件
     * @return langType
     */
    @Safe
    public @Nullable String parseLangTypeFromVFile(VirtualFile file) {
        String filePath = ViewUtils.getUnifyFilePathByVFile(file);
        return parseLangType(filePath);
    }

    /**
     * 通过当前打开的vFile 反解析langType
     *
     * @param project project
     * @return langType
     */
    @UnSafe("不安全的方法, 可能会产生弹框, 导致阻塞")
    public String parseLangTypeFromCVFile(Project project) {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            ViewUtils.getDialogWrapper(BundleUtils.i18n("code.service.no.file.open"));
            return null;
        }
        return parseLangTypeFromVFile(cFile);
    }

    private RunCode buildRunCode(LeetcodeEditor lc, String codeContent) {
        // build run code
        RunCode runCode = new RunCode();
        runCode.setFrontendQuestionId(lc.getFrontendQuestionId());
        runCode.setQuestionId(lc.getQuestionId());
        // runCode.setLang(lc.getLang());
        /*
            一切提交代码类型按照setting中的设置确定
         */
        String langType = AppSettings.getInstance().getLangType();
        runCode.setLang(langType);
        // 处理codeContent, 截取代码片段
        String coreCode = Question.getCoreCodeSnippets(codeContent, langType);
        /*
            这里额外做一层保障, 如果用户随意删改lineStart和endStart, 那么最终得到的coreCode可能会出现问题
            如果截取为空, 则不做任何截取操作
         */
        runCode.setTypeCode(StringUtils.isBlank(coreCode) ? codeContent : coreCode);
        runCode.setTitleSlug(lc.getTitleSlug());
        runCode.setDataInput(lc.getExampleTestcases());
        return runCode;
    }

    /**
     * run code with a teat case through a leetcode platform
     */
    public void runCode() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            String msg = BundleUtils.i18n("code.service.no.leetcode.editor.open");
            ConsoleUtils.getInstance(project).showWaring(
                msg,
                false,
                true,
                msg,
                BundleUtils.i18n("action.leetcode.unknown.error"),
                ConsoleDialog.ERROR
            );
            return;
        }
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, BundleUtils.i18n("action.leetcode.plugin.editor.RunCodeAction"), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    // todo: 记得删除
                    LogUtils.simpleDebug("run code: " + runCode);
                    RunCodeResult rcr = LeetcodeClient.getInstance(project).runCode(runCode);
                    // todo: 记得删除
                    LogUtils.simpleDebug("rcr = " + rcr);
                    // show it
                    AbstractResultBuilder<RunCodeResult> rcrb = createRunCodeResultBuilder(runCode.getDataInput(), rcr,
                        project);
                    boolean correctAnswer = rcrb.isCorrectAnswer();
                    if (correctAnswer) {
                        ConsoleUtils.getInstance(project)
                            .showInfo(rcrb.build(), true, true, BundleUtils.i18n("leetcode.status.ac"),
                                BundleUtils.i18n("leetcode.result"), ConsoleDialog.INFO);
                    } else {
                        ConsoleUtils.getInstance(project)
                            .showInfo(rcrb.build(), true, true, "Oh No! " + BundleUtils.i18n("leetcode.status.notac"),
                                BundleUtils.i18n("leetcode.result"), ConsoleDialog.ERROR);
                    }
                }
            });
    }

    public void submitCode() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get file content
        String codeContent = editor.getFileContent();
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByEditor(editor, project);

        if (lc == null) {
            String msg = BundleUtils.i18n("code.service.no.leetcode.editor.open");
            ConsoleUtils.getInstance(project).showWaring(
                msg,
                false,
                true,
                msg,
                BundleUtils.i18n("leetcode.error"),
                ConsoleDialog.ERROR
            );
            return;
        }

        // build run code
        RunCode runCode = buildRunCode(lc, codeContent);

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, BundleUtils.i18n("action.leetcode.plugin.editor.SubmitCodeAction"),
                false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    SubmitCodeResult scr = LeetcodeClient.getInstance(project).submitCode(runCode);
                    // show it
                    AbstractResultBuilder<SubmitCodeResult> scrb = createSubmitCodeResultBuilder(scr, project);
                    boolean correctAnswer = scrb.isCorrectAnswer();
                    if (correctAnswer) {
                        String todaySlug = StoreService.getInstance(project)
                            .getCache(StoreService.LEETCODE_TODAY_QUESTION_KEY, String.class);
                        if (StringUtils.isNotBlank(todaySlug) && todaySlug.equals(runCode.getTitleSlug())) {
                            // 延迟两秒发送事件, 异步刷新. 放置跟新太快导致查询到Leetcode的老数据
                            // todo: 延迟刷新会不会存在问题? 需要测试
                            TaskCenter.getInstance().createTask(() -> {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ignored) {
                                }
                                // 通知
                                LCEventBus.getInstance().post(new TodayQuestionOkEvent());
                            }).invokeLater();
                        }
                        ConsoleUtils.getInstance(project).showInfo(BundleUtils.i18n("leetcode.status.ac"), true, true,
                            BundleUtils.i18n("leetcode.status.ac"), BundleUtils.i18n("leetcode.result"),
                            ConsoleDialog.INFO);
                    } else {
                        ConsoleUtils.getInstance(project)
                            .showInfo(BundleUtils.i18n("leetcode.status.notac"), true, true,
                                "Oh No! " + BundleUtils.i18n("leetcode.status.notac"),
                                BundleUtils.i18n("leetcode.result"),
                                ConsoleDialog.INFO);
                    }

                    ConsoleUtils.getInstance(project).showInfo(scrb.build());
                    // update question
                    QuestionService.getInstance(project)
                        .updateQuestionStatusByFqid(project, runCode.getFrontendQuestionId(), scrb.isCorrectAnswer());
                    // post
                    LCEventBus.getInstance().post(new CodeSubmitEvent(project));
                }
            });
    }

    /**
     * 重新定位question
     */
    public void rePosition() {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            ViewUtils.getDialogWrapper(BundleUtils.i18n("code.service.no.file.open"));
            return;
        }
        String filePath = ViewUtils.getUnifyFilePathByVFile(cFile);
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        if (fileName.startsWith("[0x3f]")) {
            // 重新打开当前文件
            TaskCenter.getInstance().createEDTTask(() -> {
                ViewUtils.closeVFile(cFile, project);
                VirtualFile newfile = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (newfile != null) {
                    OpenFileDescriptor ofd = new OpenFileDescriptor(project, newfile);
                    FileEditorManager.getInstance(project).openTextEditor(ofd, false);
                }
            }).invokeLater();
            return;
        }
        // 获取当前打开文件的fid
        String fid = parseFidFromVFile(cFile);

        // 获取当前打开文件的titleSlug
        String titleSlug = parseTitleSlugFromVFile(cFile);
        // 获取当前打开文件的语言类型
        String langType = parseLangTypeFromVFile(cFile);
        if (fid == null || titleSlug == null) {
            ViewUtils.getDialogWrapper(BundleUtils.i18n("code.service.not.support.reposition"));
            return;
        }
        if (!LangType.contains(langType)) {
            String msg = BundleUtils.i18nHelper("当前文件类型不支持. 你的文件类型是 = " + langType
                    + "\n"
                    + "支持的文件类型是 : " + LangType.getAllLangType() + "\n"
                    + "请移除当前文件并重新选择问题",
                "Unsupported File Type. Your File Type is = " + langType
                    + "\n"
                    + "Supported File Types are : " + LangType.getAllLangType() + "\n"
                    + "Please Remove Current File And Reselect Question"
                    + "\n"
            );
            ViewUtils.getDialogWrapper(msg);
            return;
        }
        // 遍历myList
        LCEventBus.getInstance().post(new RePositionEvent(fid, titleSlug, cFile, langType));
    }

    /**
     * 获取默认代码, 并写入当前打开文件
     * <p>
     * 该方法会通过当前打开的文件解析出fid和titleSlug, 并且根据这两个值获取默认代码
     */
    public void getDefaultContent() {
        VirtualFile cFile = ViewUtils.getCurrentOpenVirtualFile(project);
        if (cFile == null) {
            ViewUtils.getDialogWrapper(BundleUtils.i18n("code.service.no.file.open"));
            return;
        }
        String titleSlug = parseTitleSlugFromVFile(cFile);
        String langType = parseLangTypeFromVFile(cFile);
        // 插件设置时, 设定的langType
        String settingLangType = AppSettings.getInstance().getLangType();

        // 文件所代表的code类型
        if (!LangType.contains(langType)) {
            String msg = BundleUtils.i18nHelper(
                "当前代码文件类型无法识别." +
                    " \r\n" +
                    "插件将从插件设置中加载代码类型.\r\n你的代码文件类型 = " + langType +
                    "\r\n设置的语言类型 = " + settingLangType,
                "current code file type can not be identified." +
                    "\r\n" +
                    "plugin will load content type from your plugin setting.\r\nyour code file type = " + langType +
                    "Setting LangType = " + settingLangType
            );
            ViewUtils.getDialogWrapper(msg);
        } else if (!LangType.equals(langType, settingLangType)) {
            String msg = BundleUtils.i18nHelper(
                "你确定要加载插件设置中的代码类型? " + "\r\n" +
                    "你的代码文件类型 = " + langType + "\r\n" +
                    "设置的语言类型 = " + settingLangType + "\r\n"
                ,
                "Are you sure to load content from setting?" + "\r\n" +
                    "Your code file type = " + langType + "\r\n" +
                    "Setting LangType = " + settingLangType + "\r\n"
            );

            int result = ViewUtils.getDialogWrapper(
                msg,
                BundleUtils.i18n("code.service.load.default.title")
            ).getExitCode();

            if (result == DialogWrapper.OK_EXIT_CODE) {
                return;
            }
        }
        // 通过titleSlug查询question的content
        Question question = QuestionService.getInstance(project).queryQuestionInfo(titleSlug, project);

        /*
            查询默认代码, 并写入当前打开的文件
         */
        if (question != null && StringUtils.isNotBlank(question.getCodeSnippets())) {
            String defaultCode = question.getCodeSnippets();

            boolean flag = ViewUtils.writeContentToVFile(cFile, defaultCode);
            if (flag) {
                String msg = BundleUtils.i18nHelper("加载默认代码成功!", "load default content success!");
                ViewUtils.getDialogWrapper(msg);
            } else {
                String msg = BundleUtils.i18nHelper("加载默认代码失败!", "load default content error!");
                ViewUtils.getDialogWrapper(msg);
            }

        } else {
            String msg = BundleUtils.i18nHelper("未找到题目信息, 重定位失败!",
                "question not found, reposition failed!");
            ViewUtils.getDialogWrapper(msg + titleSlug);
        }
    }

    /**
     * 创建负责输出RunCode结果的类
     *
     * @param dataInput 输入测试案例
     * @param cr RunCode结果, 来自于leetcode platform
     * @return 返回builder, 用于build创建string, 输出到LCConsole
     */
    private AbstractResultBuilder<RunCodeResult> createRunCodeResultBuilder(String dataInput, RunCodeResult cr,
        Project project) {
        return new AbstractResultBuilder<>(cr, project) {
            @Override
            protected void createBody() {
                String totalTestcases = cr.getTotalTestcases();
                if (StringUtils.isBlank(totalTestcases)) {
                    return;
                }
                int total = Integer.parseInt(totalTestcases);
                for (int i = 0; i < total; i++) {
                    sb.append(splitter).append(BundleUtils.i18nHelper("测试案例 ", "test case ")).append(i + 1)
                        .append(": ").append(cr.getCompareResult().charAt(i) == '1' ? "✅" : "❌").append(splitter)
                        .append("\n");
                    // extract std_output
                    extractStdoutput(i);
                    // extract input
                    extractInput(i, total);
                    // extract answer
                    extractAnswer(i);
                    // extract expected answer
                    extractExpectedAnswer(i);
                }
            }

            private void extractExpectedAnswer(int i) {
                List<String> expectedCodeAnswer = cr.getExpectedCodeAnswer();
                if (i >= expectedCodeAnswer.size()) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("期待的答案:", "expected answer:")).append("\n");
                sb.append(expectedCodeAnswer.get(i)).append("\n");
            }

            private void extractAnswer(int i) {
                List<String> codeAnswer = cr.getCodeAnswer();
                if (i >= codeAnswer.size()) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("运行结果:", "result:")).append("\n");
                sb.append(codeAnswer.get(i)).append("\n");
            }

            private void extractStdoutput(int i) {
                List<String> stdOutputList = cr.getStdOutputList();
                if (i >= stdOutputList.size()) {
                    return;
                }
                if (StringUtils.isBlank(stdOutputList.get(i))) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("标准输出:", "std output:")).append("\n");
                sb.append(stdOutputList.get(i)).append("\n");
            }

            private void extractInput(int i, int total) {
                if (StringUtils.isBlank(dataInput)) {
                    return;
                }
                String[] input = dataInput.split("\n");
                if (i >= input.length) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("输入:", "input:")).append("\n");
                int size = input.length / total;
                int start = i * size, end = start + size;
                for (int k = start; k < end; ++k) {
                    sb.append(input[k]).append("\n");
                }
            }
        };
    }

    /**
     * 创建负责输出SubmitCode结果的类
     *
     * @param scr SubmitCodeResult, 来自于leetcode platform
     * @return 返回builder, 用于build创建string, 输出到LCConsole
     */
    private AbstractResultBuilder<SubmitCodeResult> createSubmitCodeResultBuilder(SubmitCodeResult scr,
        Project project) {
        return new AbstractResultBuilder<>(scr, project) {
            @Override
            protected void createBody() {
                boolean correctAnswer = isCorrectAnswer();
                if (correctAnswer) {
                    return;
                }
                sb.append(splitter).append(BundleUtils.i18nHelper("上一个测试案例", "last test case")).append(": ")
                    .append("❌").append(splitter).append("\n");
                // extract std_output
                extractStdoutput();
                // extract input
                extractInput();
                // extract answer
                extractAnswer();
                // extract expected answer
                extractExpectedAnswer();
            }

            @Override
            protected void appendToCorrectHeadAfter() {
                LCEventBus.getInstance().post(new TimeStopEvent());
                TimerWindow timerWindow = ActionUtils.getTimerWindow();
                if (timerWindow != null) {
                    sb.append(BundleUtils.i18nHelper("解题花费时间: ", "solve time: ")).append(timerWindow.getTime())
                        .append("\n");
                }
            }

            private void extractExpectedAnswer() {
                String expectedOutput = cr.getExpectedOutput();
                if (StringUtils.isBlank(expectedOutput)) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("期待的答案:", "expected answer:")).append("\n");
                sb.append(expectedOutput).append("\n");
            }

            private void extractAnswer() {
                String codeOutput = cr.getCodeOutput();
                if (StringUtils.isBlank(codeOutput)) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("运行结果:", "result:")).append("\n");
                sb.append(codeOutput).append("\n");
            }

            private void extractStdoutput() {
                String stdOutput = cr.getStdOutput();
                if (StringUtils.isBlank(stdOutput)) {
                    return;
                }

                sb.append(BundleUtils.i18nHelper("标准输出:", "std output:")).append("\n");
                sb.append(stdOutput).append("\n");
            }

            private void extractInput() {
                String lastTestcase = cr.getLastTestcase();
                if (StringUtils.isBlank(lastTestcase)) {
                    return;
                }

                String[] split = lastTestcase.split("\n");
                sb.append(BundleUtils.i18nHelper("输入:", "input:")).append("\n");
                for (String s : split) {
                    sb.append(s).append("\n");
                }
            }
        };
    }

    /**
     * open test case dialog
     */
    public void openTestCasesDialog() {
        /* get file editor */
        SplitTextEditorWithPreview editor = ViewUtils.getFileEditor(project, SplitTextEditorWithPreview.class);

        // get example test cases
        String path = ViewUtils.getUnifyFilePathByVFile(Objects.requireNonNull(editor.getFile()));
        LeetcodeEditor lc = StoreService.getInstance(project).getCache(path, LeetcodeEditor.class);

        if (lc == null) {
            String msg = BundleUtils.i18n("code.service.no.leetcode.editor.open");
            ConsoleUtils.getInstance(project).showWaring(
                msg,
                false,
                true,
                msg,
                BundleUtils.i18nHelper("测试案例设置错误", "Test Cases Set Error"),
                ConsoleDialog.ERROR
            );
            return;
        }

        // check testcase data input
        if (lc.getExampleTestcases() == null || lc.getDefaultTestcases() == null) {
            // load example
            Question q = QuestionService.getInstance(project).queryQuestionInfo(lc.getTitleSlug(), project);
            if (StringUtils.isBlank(q.getExampleTestcases())) {
                throw new RuntimeException("No example test cases found...");
            }
            lc.setExampleTestcases(q.getExampleTestcases());
            lc.setDefaultTestcases(q.getExampleTestcases());
            // restore
            StoreService.getInstance(project).addCache(path, lc);
        }

        // create dialog
        new TestCaseDialog(
            lc.getExampleTestcases(), path, project
        ).show();
    }

    /**
     * 抽象结果构建器，用于构建结果字符串以在控制台显示RunCode/SubmitCode的结果
     * <p>
     * RunCodeResult 封装了在 leetcode 平台上运行提交的解决方案代码所获得的结果。
     * <p>
     * 然而，为了确保可扩展性，在设计中提取了一个抽象类，以适应 SubmitCodeResult 和 RunCodeResult 的输出需求。
     */
    private abstract static class AbstractResultBuilder<T extends BaseCodeResult> {

        protected final StringBuilder sb = new StringBuilder();
        protected final String splitter = "--------------";
        private final Project project;
        private final String codeTypeSplitter = "===============";
        private final Pattern pattern = Pattern.compile("Line (\\d+):");
        protected T cr; // code result

        public AbstractResultBuilder(T cr, Project project) {
            this.cr = cr;
            this.project = project;
        }

        public String build() {
            createHead();
            createBody();
            return sb.toString();
        }

        protected boolean isCorrectAnswer() {
            return Objects.equals(cr.getTotalCorrect(), cr.getTotalTestcases()) &&
                StringUtils.isNotBlank(cr.getTotalCorrect()) &&
                StringUtils.isNotBlank(cr.getTotalTestcases());
        }

        /*
         * 创建头部:
         * ========================== example 1 ============================
         *  ✅ Accept...
         *  total test cases: 3
         *  total correct: 3
         *
         * ========================== example 2 ============================
         *  ❌ Wrong Answer...
         *  total test cases: 3
         *  total correct: 0
         *
         * ========================== example 3 ============================
         * ❌ Runtime Error...
         *  NameError: name 'abab' is not defined
         *      abab
         *  Line 5 in semiOrderedPermutation (Solution.py)
         *            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         *      ret = Solution().semiOrderedPermutation(param_1)
         *  Line 31 in _driver (Solution.py)
         *      _driver()
         *  Line 46 in <module> (Solution.py)
         */
        private void createHead() {
            boolean correctAnswer = isCorrectAnswer();
            sb.append("\n").append(codeTypeSplitter).append(" ").append("⚙ ")
                .append(BundleUtils.i18nHelper("设置的代码类型", "setting code type")).append(" : ")
                .append(AppSettings.getInstance().getLangType()).append(" ").append(codeTypeSplitter).append("\n\n");
            if (correctAnswer) {
                // true
                sb.append("✅ ").append(BundleUtils.i18nHelper("通过", "Accept")).append("...").append("\n");
                sb.append("⏰: ").append(cr.getDisplayRuntime()).append(" ms ").append(" 💽: ")
                    .append(cr.getStatusMemory()).append("\n");
                sb.append(BundleUtils.i18nHelper("全部的测试案例数量", "total test cases number")).append(": ")
                    .append(cr.getTotalTestcases()).append("\n");
                sb.append(BundleUtils.i18nHelper("通过的测试案例数量", "total ac   cases number")).append(": ")
                    .append(cr.getTotalCorrect()).append("\n");
                appendToCorrectHeadAfter();
            } else {
                boolean runSuccess = cr.getRunSuccess();
                if (runSuccess) {
                    // true
                    sb.append("❌ ").append(BundleUtils.i18nHelper("错误", "Wrong Answer")).append(" ...").append("\n");
                    sb.append(BundleUtils.i18nHelper("全部的测试案例数量", "total test cases number")).append(": ")
                        .append(cr.getTotalTestcases()).append("\n");
                    sb.append(BundleUtils.i18nHelper("通过的测试案例数量", "total ac   cases number")).append(": ")
                        .append(cr.getTotalCorrect()).append("\n");
                } else {
                    // run error
                    if ("Runtime Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ ").append(BundleUtils.i18n("leetcode.status.re")).append("...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullRuntimeError(), Question.getLineUpperOffset(project)))
                            .append("\n");
                    } else if ("Compile Error".equals(cr.getStatusMsg())) {
                        sb.append("❌ ").append(BundleUtils.i18n("leetcode.status.ce")).append("...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullCompileError(), Question.getLineUpperOffset(project)))
                            .append("\n");
                    } else if ("Time Limit Exceeded".equals(cr.getStatusMsg())) {
                        sb.append("❌ ").append(BundleUtils.i18n("leetcode.status.tle")).append("...").append("\n");
                        sb.append(DebugUtils.matchLines(cr.getFullCompileError(), Question.getLineUpperOffset(project)))
                            .append("\n");
                    } else {
                        // throw new RuntimeException("unknown leetcode error...");
                        sb.append("❌ ").append(cr.getStatusMsg()).append("\n");
                    }
                }
            }
        }

        /**
         * 在答案正确的工况下, 将内容添加到head尾部. 在父类添加方法中, 会自动判断是否存在换行符. 如果没有, 父类会添加
         */
        protected void appendToCorrectHeadAfter() {

        }

        /**
         * 处理错误行信息: 比如
         * ❌ Compile Error...
         * Line 36: error: not a statement
         * a
         * ^
         * 我要提取 Line 36, 然后对36进行行号校正. 因为存在注释偏移
         */
        private String handleErrorInfo(String error) {
            // 正则表达式提取行号
            Matcher matcher = pattern.matcher(error);

            if (matcher.find()) {
                // 提取行号
                String lineNumberStr = matcher.group(1);
                int lineNumber = Integer.parseInt(lineNumberStr);
                // 对行号进行操作
                int newLineNumber = Question.getLineUpperOffset(project);  // 示例：将行号加10
                return error.replaceFirst("Line " + lineNumberStr + ":", "Line " + newLineNumber + ":");
            } else {
                return error;
            }
        }

        /**
         * 创建body
         * ========================== example 1 ============================
         * --------------CASE 1: ✅--------------
         * Input:
         * [2,1,4,3]
         * Code Answer:
         * 2
         * Expect Answer:
         * 2
         * --------------CASE 2: ✅--------------
         * Input:
         * [2,4,1,3]
         * Code Answer:
         * 3
         * Expect Answer:
         * 3
         * --------------CASE 3: ✅--------------
         * Input:
         * [1,3,4,2,5]
         * Code Answer:
         * 0
         * Expect Answer:
         * 0
         * <p>
         * ========================== example 2 ============================
         * --------------CASE 1: ❌--------------
         * Input:
         * [2,1,4,3]
         * Code Answer:
         * 4
         * Expect Answer:
         * 2
         * --------------CASE 2: ❌--------------
         * Input:
         * [2,4,1,3]
         * Code Answer:
         * 5
         * Expect Answer:
         * 3
         * --------------CASE 3: ❌--------------
         * Input:
         * [1,3,4,2,5]
         * Code Answer:
         * 2
         * Expect Answer:
         * 0
         * <p>
         * ========================== example 3 ============================
         * null
         */
        protected abstract void createBody();
    }
}