package com.xhf.leetcode.plugin.window.deepcoding;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.comp.MySearchConditionPanel;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.listener.AbstractMouseAdapter;
import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.model.DeepCodingInfo;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.render.QuestionCellRender;
import com.xhf.leetcode.plugin.search.engine.CompetitionQuestionEngine;
import com.xhf.leetcode.plugin.search.engine.SearchEngine;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.TaskCenter;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import com.xhf.leetcode.plugin.window.AbstractSearchPanel;
import com.xhf.leetcode.plugin.window.deepcoding.filter.CQAlgorithmFilter;
import com.xhf.leetcode.plugin.window.deepcoding.filter.CQDifficultyFilter;
import com.xhf.leetcode.plugin.window.deepcoding.filter.CQFilterChain;
import com.xhf.leetcode.plugin.window.deepcoding.filter.CQRateFilter;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Leetcode 竞赛
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCCompetitionPanel extends AbstractSearchPanel<CompetitionQuestion> {
    /**
     * 用于deep coding info 存储的信息, 可以表示当前文件是通过 deep coding下的哪个模式打开
     */
    public static final String LC_COMPETITION = "LC-competition";
    /**
     * 用于 panel tab 显示信息
     */
    public static final String LC_COMPETITION_TEXT = "LC-竞赛题";

    private final MyList<CompetitionQuestion> questionList;
    private final CompetitionQuestionEngine searchEngine;
    private final CQFilterChain filterChain;
    private static List<CompetitionQuestion> competitionList;

    public LCCompetitionPanel(Project project) {
        super(project);
        this.questionList = new MyList<>();
        this.filterChain = new CQFilterChain();
        this.searchEngine = CompetitionQuestionEngine.getInstance(project);
        initMyList();
        super.init();
//        super.unLock();
        LCEventBus.getInstance().register(this);
    }

    @Override
    protected void addToSearchBarBefore(JPanel searchBar) {
        // 创建 JEditorPane 实例并设置其内容类型为 text/html
        JEditorPane editorPane = new JEditorPane();
        editorPane.setMargin(JBUI.emptyInsets());
        editorPane.setBorder(BorderFactory.createEmptyBorder());
        editorPane.setContentType("text/html");
        editorPane.setText("<p>  当前界面数据来自于<a href='https://github.com/huxulm/lc-rating'>github开源项目lc-rating</a> <font color=red>感谢!</font></p><p>  当前界面题解来自于<a href='https://space.bilibili.com/206214'>bilibili@灵茶山艾府</a> <font color=red>感谢!</font></p>");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);

        // 处理超链接点击事件
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                // 打开默认浏览器访问该链接
                try {
                    Desktop.getDesktop().browse(java.net.URI.create(e.getURL().toString()));
                } catch (IOException ex) {
                    LogUtils.error(ex);
                }
            }
        });

        searchBar.add(editorPane);
    }

    @Override
    protected void addToConditionGroup(JPanel conditionGroup) {
        JLabel label = new JLabel("灵神题单");
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        var convert = getMapOptionConverter();

        var conditionComb = new ComboBox<>(convert.getOptions());
        conditionComb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        conditionComb.setSelectedIndex(-1); // 没有选择项时，显示默认文本

        conditionComb.addActionListener(new ActionListener() {
            private String previousSelection = "";
            private final String filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getFilePath()).append("0x3f").build();
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) conditionComb.getSelectedItem();

                // 判断当前选择的项是否与之前的选择项相同
                if (selectedItem == null || selectedItem.equals(previousSelection)) {
                    // 如果选择的是之前的项，清空选择
                    conditionComb.setSelectedIndex(-1);
                    // 清除记录的选中项
                    previousSelection = null;
                } else {
                    // 否则，更新选中的项
                    previousSelection = selectedItem;
//                    // 触发创建题解事件
//                    String fileName = new FileUtils.PathBuilder(filePath).append("[0x3f]-" + selectedItem).build();
//                    try {
//                        FileUtils.createAndWriteFile(fileName, convert.doConvert(selectedItem));
//                    } catch (IOException ex) {
//                        LogUtils.error(ex);
//                        ConsoleUtils.getInstance(project).showError("灵神题单创建错误! 错误原因 = " + ex.getMessage(), false, true);
//                    }

                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        LightVirtualFile file = new LightVirtualFile("[0x3f]-" + selectedItem, convert.doConvert(selectedItem));
                        OpenFileDescriptor ofd = new OpenFileDescriptor(project, file);
                        FileEditorManager.getInstance(project).openTextEditor(ofd, false);
                    });
                }
            }
        });

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.add(label);
        jPanel.add(conditionComb);

        conditionGroup.add(jPanel);
    }

    @NotNull
    private static MySearchConditionPanel.MapOptionConverter getMapOptionConverter() {
        var convert = new MySearchConditionPanel.MapOptionConverter(11);
        convert.addPair("滑动窗口", "https://leetcode.cn/circle/discuss/0viNMK");
        convert.addPair("二分查找", "https://leetcode.cn/circle/discuss/SqopEo");
        convert.addPair("单调栈", "https://leetcode.cn/circle/discuss/9oZFK9");
        convert.addPair("网格图", "https://leetcode.cn/circle/discuss/YiXPXW");
        convert.addPair("位运算", "https://leetcode.cn/circle/discuss/dHn9Vk");
        convert.addPair("图论算法", "https://leetcode.cn/circle/discuss/01LUak");
        convert.addPair("动态规划", "https://leetcode.cn/circle/discuss/tXLS3i");
        convert.addPair("数据结构", "https://leetcode.cn/circle/discuss/mOr1u6");
        convert.addPair("数学", "https://leetcode.cn/circle/discuss/IYT3ss");
        convert.addPair("贪心", "https://leetcode.cn/circle/discuss/g6KTKL");
        convert.addPair("树和二叉树", "https://leetcode.cn/circle/discuss/K0n2gO");
        convert.addPair("字符串", "https://leetcode.cn/circle/discuss/SJFwQI");
        return convert;
    }

    /**
     * 初始化MyList
     */
    private void initMyList() {
        TaskCenter.getInstance().createTask(() -> {
            competitionList = QuestionService.getInstance().loadCompetitionQuestionData();
            // 处理完善数据信息
            List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);
            for (CompetitionQuestion c : competitionList) {
                String fid = c.getFid();
                int idx = Integer.parseInt(fid) -1;
                Question question = totalQuestion.get(idx);
                if (! question.getTitleSlug().equals(c.getTitleSlug())) {
                    LogUtils.warn("fid = " + fid + " 的Competition无法和对应fid的Question匹配! " + " competition titleSlug = " + c.getTitleSlug() + " question titleSlug = " + question.getTitleSlug());
                    continue;
                }
                // 填充完成情况
                c.setStatus(question.getStatus());
            }
            this.questionList.setListData(competitionList);

            questionList.setCellRenderer(new QuestionCellRender());
            questionList.addMouseListener(new AbstractMouseAdapter(project) {
                @Override
                protected void doubleClicked(MouseEvent e) {
                    Point point = e.getPoint();
                    int idx = questionList.locationToIndex(point);
                    DeepCodingInfo dci = new DeepCodingInfo(LC_COMPETITION, questionList.getModel().getSize(), idx);
                    CompetitionQuestion cq = questionList.getModel().getElementAt(idx);
                    CodeService.getInstance(project).openCodeEditor(cq.toQuestion(project), dci);
                }
            });

            JBScrollPane jbScrollPane = new JBScrollPane(questionList);
            this.add(jbScrollPane, BorderLayout.CENTER);
            this.setContent(jbScrollPane);
        }).invokeLater();
    }

    @Override
    protected MyList<CompetitionQuestion> getDataList() {
        return this.questionList;
    }

    @Override
    protected SearchEngine<CompetitionQuestion> getSearchEngine() {
        return this.searchEngine;
    }

    @Override
    protected FilterChain<CompetitionQuestion> getFilterChain() {
        return filterChain;
    }

    @Override
    protected List<MySearchConditionPanel<CompetitionQuestion>> getSearchCondition() {
        List<MySearchConditionPanel<CompetitionQuestion>> list = new ArrayList<>();
        list.add(new MySearchConditionPanel<CompetitionQuestion>(super::updateText, "竞赛分") {
            @Override
            public OptionConvert createConvert() {
                ArrayOptionConverter arr = new ArrayOptionConverter(7);
                arr.addPair(" <= 1200", "0-1200");
                arr.addPair("1200 - 1400", "1200-1400");
                arr.addPair("1400 - 1600", "1400-1600");
                arr.addPair("1600 - 1900", "1600-1900");
                arr.addPair("1900 - 2100", "1900-2100");
                arr.addPair("2100 - 2400", "2100-2400");
                arr.addPair(" >= 2400", "2400-10000");
                return arr;
            }
            @Override
            public Filter<CompetitionQuestion, String> createFilter() {
                return new CQRateFilter();
            }
        });

        list.add(new MySearchConditionPanel<CompetitionQuestion>(super::updateText, "Algorithm") {
            @Override
            public OptionConvert createConvert() {
                var converter = new MapOptionConverter(70);
                String[] options = new String[]{"数组","字符串","排序","矩阵","模拟","枚举","字符串匹配","计数排序","桶排序","基数排序","动态规划","贪心","深度优先搜索","二分查找","广度优先搜索","回溯","递归","分治","记忆化搜索","归并排序","快速选择","哈希表","树","二叉树","堆（优先队列）","栈","图","链表","单调栈","有序集合","队列","二叉搜索树","拓扑排序","最短路","单调队列","双向链表","最小生成树","强连通分量","欧拉回路","双连通分量","并查集","字典树","线段树","树状数组","后缀数组","位运算","双指针","前缀和","计数","滑动窗口","状态压缩","哈希函数","滚动哈希","扫描线","数学","数论","几何","组合数学","博弈","随机化","概率与统计","水塘抽样","拒绝采样","数据库","设计","数据流","交互","脑筋急转弯","迭代器","多线程","Shell"};
                for (String option : options) {
                    converter.addPair(option);
                }
                return converter;
            }
            @Override
            public Filter<CompetitionQuestion, String> createFilter() {
                return new CQAlgorithmFilter();
            }
        });

        list.add(new MySearchConditionPanel<CompetitionQuestion>(super::updateText, "Difficulty") {

            @Override
            public OptionConvert createConvert() {
                ArrayOptionConverter converter = new ArrayOptionConverter(3);
                converter.addPair("简单", "EASY");
                converter.addPair("中等", "MEDIUM");
                converter.addPair("困难", "HARD");
                return converter;
            }

            @Override
            public Filter<CompetitionQuestion, String> createFilter() {
                return new CQDifficultyFilter();
            }
        });
        return list;
    }

    @Override
    protected List<CompetitionQuestion> getUpdateData() {
        return competitionList;
    }

    public static List<CompetitionQuestion> getCompetitionList() {
        return competitionList;
    }

    @Override
    public MyList<CompetitionQuestion> getMyList() {
        return this.questionList;
    }

    @Subscribe
    public void rePositionEventListeners(RePositionEvent event) {
        // 检查当前Panel是否需要监听事件
        if (! super.doCheck(LC_COMPETITION_TEXT)) {
            return;
        }
        // 必须清除所有筛选条件
        this.clear();

        ViewUtils.rePositionInDeepCoding(event, questionList, project, LC_COMPETITION);
    }

    @Subscribe
    public void loginEventListener(LoginEvent listener) {
        // 提前调用indexLock(). 因为登录后必定要重新加载所有题目数据, 从而rebuild engine's index
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
    }

    /**
     * 当数据加载完毕后, 执行渲染逻辑
     * @param event event
     */
    @Subscribe
    public void qLoadEndListener(QLoadEndEvent event) {
        unLock();
        questionList.setEmptyText("没有可用于展示的数据...");
        initMyList();
        updateText();
    }

    @Subscribe
    public void qLoadStartListener(QLoadStartEvent event) {
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
        questionList.setNonData();
    }

    @Subscribe
    public void codeSubmitEventListener(CodeSubmitEvent event) {
        if (! super.doCheck(LC_COMPETITION_TEXT)) {
            return;
        }
        indexLock();
        questionList.setEmptyText("数据加载中, 请稍等");
        questionList.setNonData();
        initMyList();
        unLock();
        updateText();
    }

    @Subscribe
    public void clearCacheEventListeners(ClearCacheEvent event) {
        loginLock();
        questionList.setEmptyText("请先登录...");
        questionList.setNonData();
    }

}
