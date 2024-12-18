package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.JavaTestcaseConvertor;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * 启动Java环境的debug
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebugEnv implements DebugEnv {
    private final Project project;
    /**
     * 核心存储路径. 所有debug相关文件都存储在filePath指定目录下
     */
    private String filePath = "E:\\java_code\\lc-test\\cache\\debug";
    /**
     * Java编写的Main类路径
     */
    private String mainJavaPath = "E:\\java_code\\lc-test\\cache\\debug\\Main.java";
    /**
     * Java编写的Solution类的路径
     */
    private String solutionJavaPath = "E:\\java_code\\lc-test\\cache\\debug\\Solution.java";
    /**
     * 编译后的Main.class路径
     */
    private String mainClassPath = "E:\\java_code\\lc-test\\cache\\debug\\Main.class";
    /**
     * java执行路径
     */
    private String java = "E:\\jdk8\\bin\\java.exe";
    /**
     * javac执行路径
     */
    private String javac = "E:\\jdk8\\bin\\javac.exe";
    /**
     * Solution核心方法名
     */
    private String methodName;

    public JavaDebugEnv(Project project) {
        this.project = project;
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").build();
    }

    @Deprecated // only for test
    public JavaDebugEnv() {
        this.project = null;
//        this.filePath = null;
    }

    /**
     * 构建主类 + cv当前打开的文件
     */
    @Override
    public boolean prepare() {
        return buildToolPrepare() && createSolutionFile() && createMainFile() && buildFile();
    }

    /**
     * 获取编译工具路径
     * @return
     */
    private boolean buildToolPrepare() {
        java = System.getenv("JAVA_HOME") + "/bin/java";
        javac = System.getenv("JAVA_HOME") + "/bin/javac";
        return FileUtils.fileExists(java) && FileUtils.fileExists(javac);
    }

    private boolean buildFile() {
        // 通过java编译mainJavaPath下的Java类
        try {
            // 获取系统javac路径
            String cmd = javac + " -g " + mainJavaPath;
            Runtime.getRuntime().exec(cmd);
            mainClassPath = mainJavaPath.replace("Main.java", "Main.class");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createMainFile() {
        String mainPath = new FileUtils.PathBuilder(filePath).append("Main.java").build();
        this.mainJavaPath = mainPath;
        // 读取Main.template
        String mainContent = FileUtils.readContentFromFile(getClass().getResource("/debug/java/Main.template"));
        // 获取callCode
        mainContent = mainContent.replace("{{callCode}}", getCallCode());
        // 存储文件
        StoreService.getInstance(project).writeFile(mainPath, mainContent);
        return true;
    }

    private String getCallCode() {
        // 分析得到代码片段
        JavaCodeAnalyzer analyzer = new JavaCodeAnalyzer(project);
        AnalysisResult result = analyzer.autoAnalyze();
        this.methodName = result.getMethodName();
        JavaTestcaseConvertor convertor = new JavaTestcaseConvertor("solution", result, project);
        // 得到调用代码
        return convertor.autoConvert();
    }

    private boolean createSolutionFile() {
        // 获取路径
        String solutionPath = new FileUtils.PathBuilder(filePath).append("Solution.java").build();
        this.solutionJavaPath = solutionPath;
        // 获取内容
        String solutionContent = "import java.util.*;\r\n" + ViewUtils.getContentOfCurrentOpenVFile(project);
        // 写文件
        StoreService.getInstance(project).writeFile(solutionPath, solutionContent);
        return true;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void stopDebug() {

    }

    @Override
    public void startDebug() {

    }

    @Override
    public Class<?> getBean(Class<?> clazz) {
        return null;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMainJavaPath() {
        return mainJavaPath;
    }

    public String getSolutionJavaPath() {
        return solutionJavaPath;
    }

    public String getMainClassPath() {
        return mainClassPath;
    }

    public String getJava() {
        return java;
    }

    public String getJavac() {
        return javac;
    }

    public String getMethodName() {
        return methodName;
    }
}
