package com.xhf.leetcode.plugin.service;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.*;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.FileCreateError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.CalendarSubmitRecord;
import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.TaskCenter;
import com.xhf.leetcode.plugin.utils.TodayIconStatusEnum;
import com.xhf.leetcode.plugin.window.deepcoding.LCCompetitionPanel;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * deal with http module and question ui module
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class QuestionService {

    private final Project project;

    private boolean todaySolved = false;

    private boolean needModify = false;

    private CalendarSubmitRecord calendarSubmitRecord;

    public QuestionService(Project project) {
        this.project = project;
        // 缓存今日每日一题, 同时设置过期时间为当天晚上
        LeetcodeClient instance = LeetcodeClient.getInstance(project);
        var todayRecord = instance.getTodayRecord(project);
        var todayQuestion = todayRecord.getQuestion();

        // 计算当前时间到今天晚上24点差了多少分钟
        LocalDateTime now = LocalDateTime.now();
        LogUtils.info("当前时间: " + now);

        // 获取今天24点的时间
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        LogUtils.info("今天24点时间: " + endOfDay);

        // 计算时间差
        Duration duration = Duration.between(now, endOfDay);
        long millisecondsUntilMidnight = duration.toMillis();
        LogUtils.info("diff time = " + millisecondsUntilMidnight);

        // 数据写入内存, 无需持久化
        StoreService.getInstance(project).addCache(StoreService.LEETCODE_TODAY_QUESTION_KEY, todayQuestion.getTitleSlug(), false, millisecondsUntilMidnight, TimeUnit.MILLISECONDS);

        todaySolved = "FINISH".equalsIgnoreCase(todayRecord.getUserStatus());
        if (todaySolved) {
            this.calendarSubmitRecord = instance.getCalendarSubmitRecord();
        }
        // 如果todaySolved为True, 需要修改图标
        needModify = todaySolved;

        // 注册
        LCEventBus.getInstance().register(this);
    }

    private static QuestionService qs;

    public static QuestionService getInstance(Project project) {
        if (project == null) {
            return null;
        }
        if (qs == null) {
            synchronized (QuestionService.class) {
                qs = new QuestionService(project);
            }
        }
        return qs;
    }

    public static void init(Project project) {
        if (qs == null) {
            synchronized (QuestionService.class) {
                qs = new QuestionService(project);
            }
        }
    }

    private List<CompetitionQuestion> competitionList;

    /**
     * 从文件中加载CompetitionList数据
     */
    public List<CompetitionQuestion> loadCompetitionQuestionData() {
        if (competitionList == null) {
            synchronized (this) {
                loadFromFile();
            }
        }
        return competitionList;
    }

    private void loadFromFile() {
        String jsonPath = "\\data\\dataProcessed.json";
        URL url = LCCompetitionPanel.class.getResource(FileUtils.unUnifyPath(jsonPath));
        String json = FileUtils.readContentFromFile(url);
        competitionList = GsonUtils.fromJsonToList(json, CompetitionQuestion.class);
    }

    /**
     * load questions data
     */
    public void loadAllQuestionData(Project project) {
        LCEventBus.getInstance().post(new QLoadStartEvent(project));
        ProgressManager.getInstance().run(new Task.Backgroundable(project, BundleUtils.i18n("Loading"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // query
                LeetcodeClient.getInstance(project).getTotalQuestion();
                LCEventBus.getInstance().post(new QLoadEndEvent(project));
            }
        });
    }

    /**
     * {@code @Deprecated} 当前版本废弃该方法, 该方法通过consumer引入别的模块的逻辑, 耦合性太强, qs内部逻辑和外部逻辑耦合
     * 为了解决异步加载question导致其余模块不知道question什么时候加载完毕的问题, 引入QLoadStartEvent和QLoadEndEvent事件, 通过EventBus进行通知
     * <p>
     * load questions data and to something
     * consumer是为了让数据加载操作和Consumer.accept内部代码逻辑串行执行提出的解决方案
     * 因为getTotalQuestion很耗费时间, 因此数据加载是异步操作. 但存在部分逻辑需要数据加载完毕后才能执行
     * 通过consumer回调函数的形式, 提供执行需要和数据加载逻辑同步进行的业务
     */
    @Deprecated
    public void loadAllQuestionData(Project project, MyList<Question> myList, Consumer<List<Question>> consumer) {
        // do not use another thread to get dataContext by DataManager
        ProgressManager.getInstance().run(new Task.Backgroundable(project, BundleUtils.i18n("Loading"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // query
                List<Question> totalQuestion = LeetcodeClient.getInstance(project).getTotalQuestion();
                myList.setListData(totalQuestion);
                myList.updateUI();
                // 执行需要等待题目数据全部加载完后, 才能执行的逻辑
                consumer.accept(totalQuestion);
            }
        });
    }

    /**
     * 该方法需要加锁, 因为可能存在多个线程同时调用该方法, 导致数据加载多次
     * 且该方法第一次调用时不会走任何的缓存逻辑, 可能会非常耗时
     * @param project project
     * @return 题目列表
     */
    public synchronized List<Question> getTotalQuestion(Project project) {
        return LeetcodeClient.getInstance(project).getTotalQuestion();
    }

    /**
     * query question by titleSlug
     *
     * @param titleSlug titleSlug
     * @param project project
     * @return 返回Question的额外信息, 具体可参考{@link #parseRespForFillingQuestion(Question, String)}
     */
    public Question queryQuestionInfo(@NotNull String titleSlug, Project project) {
        GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams();
        params.setTitleSlug(titleSlug);

        String resp = this.queryQuestionInfo(params, project);

        Question question = new Question();
        parseRespForFillingQuestion(question, resp);

        return question;
    }

    /**
     * 查询指定titleSlug的question, 并返回相关信息的json
     * 该方法服务于{@link #queryQuestionInfo(String, Project)}
     * 之所以该方法只返回json, 是因为最开始设计Question的时候, 没有设计好codeSnippets字段
     * 因而导致查询Question的详细信息无法直接使用Gson解析, 需要手动处理'额外信息'
     * <p>
     * 请注意, Gson无法直接解析的是Question的详细信息, 而不是题目列表信息(题目列表信息并没有完全显示question的全部信息).
     * 因此{@link #getTotalQuestion(Project)}方法可以使用Gson自动解析, 而不用手动提取
     *
     * @param params query params
     * @param project project
     * @return 查询json信息
     */
    private String queryQuestionInfo(GraphqlReqBody.SearchParams params, Project project) {
        if (StringUtils.isBlank(params.getTitleSlug())) {
            throw new RuntimeException("title slug is null ! " + GsonUtils.toJsonStr(params));
        }
        return LeetcodeClient.getInstance(project).queryQuestionInfoJson(params);
    }

    /**
     * fill question with code snippets, translated title etc...
     * @param question question
     */
    public void fillQuestion(Question question, Project project) {
        if (StringUtils.isBlank(question.getTitleSlug())) {
            throw new RuntimeException("title slug is null ! " + GsonUtils.toJsonStr(question));
        }

        GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams();
        params.setTitleSlug(question.getTitleSlug());

        String resp = LeetcodeClient.getInstance(project).queryQuestionInfoJson(params);

        // parse resp and fill question
        parseRespForFillingQuestion(question, resp);
    }

    /**
     * parse json resp and extract certain field to fill the question
     * 从json中解析提取的字段数据如下:
     * <p>
     * translatedTitle
     * translatedContent
     * questionId
     * exampleTestcases
     * codeSnippets
     *
     * @param question 需要填充的Question对象
     * @param resp json
     */
    private void parseRespForFillingQuestion(Question question, String resp) {
        // get a lang type
        String langType = AppSettings.getInstance().getLangType();
        LangType lt = LangType.getType(langType);
        assert lt != null;

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject questionJsonObj = jsonObject.getAsJsonObject("data").getAsJsonObject("question");

        String translatedTitle = questionJsonObj.get("translatedTitle").getAsString();
        // translatedContent = "null",解决报错
        String translatedContent = "";
        if(!questionJsonObj.get("translatedContent").isJsonNull()) {
            translatedContent = questionJsonObj.get("translatedContent").getAsString().replaceAll("\n\n", "");
        }
        String questionId = questionJsonObj.get("questionId").getAsString();
        String exampleTestcases = questionJsonObj.get("exampleTestcases").getAsString();
        String codeSnippets = null;

        if(!questionJsonObj.get("codeSnippets").isJsonNull()) {
            for (JsonElement item : questionJsonObj.getAsJsonArray("codeSnippets")) {
                JsonObject obj = item.getAsJsonObject();
                String lang = GsonUtils.fromJson(obj.get("lang"), String.class);
                if (lt.has(lang)) {
//            }
//            if (.equalsIgnoreCase(langType)) {
                    codeSnippets = Question.handleCodeSnippets(obj.get("code").getAsString(), langType);
                    break;
                }
            }
        }

        // fill target obj
        question.setQuestionId(questionId);
        question.setTranslatedTitle(translatedTitle);
        question.setTranslatedContent(translatedContent);
        question.setCodeSnippets(codeSnippets);
        question.setExampleTestcases(exampleTestcases);
    }

    /**
     * pick on question for random
     * @param project project
     * @return 返回随即题目
     */
    public Question pickOne(Project project) {
        LeetcodeClient instance = LeetcodeClient.getInstance(project);

        return instance.getRandomQuestion(project);
    }

    /**
     * choose daily question
     * @param project project
     */
    public void todayQuestion(Project project) {
        LeetcodeClient instance = LeetcodeClient.getInstance(project);
        Question todayQuestion = instance.getTodayQuestion(project);

        try {
            CodeService.getInstance(project).openCodeEditor(todayQuestion);
        } catch (FileCreateError e) {
            LogUtils.error(e);
            ConsoleUtils.getInstance(project).showError(BundleUtils.i18n("code.service.file.create.error"), true, true);
        }
    }

    /**
     * 根据frontedQuestionId修改题目状态
     *
     * @param project idea project对象
     * @param fqid question前端显示的id: frontedQuestionId
     * @param correctAnswer 代码运行结果是否通过
     * @return update成功, 返回true. 否则false
     */
    public boolean updateQuestionStatusByFqid(Project project, String fqid, boolean correctAnswer) {
        // update cache
        return LeetcodeClient.getInstance(project).updateQuestionStatusByFqid(fqid, correctAnswer);
    }

    public void reloadTotalQuestion(Project project) {
        // 通知开始加载数据
        LCEventBus.getInstance().post(new QLoadStartEvent(project));
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "ReLoading...", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // query
                LeetcodeClient instance = LeetcodeClient.getInstance(project);
                List<Question> totalQuestion = instance.queryTotalQuestion();
                instance.cacheQuestionList(totalQuestion);
                // 数据加载完毕, 通知别的模块处理后续逻辑
                LCEventBus.getInstance().post(new QLoadEndEvent(project));
            }
        });
    }

    public Question getQuestionByTitleSlug(String titleSlug, Project project) {
        List<Question> totalQuestion = getTotalQuestion(project);
        for (Question question : totalQuestion) {
            // 比较两个字符串是否相等的函数
            if (StringUtils.equals(question.getTitleSlug(), titleSlug)) {
                return question;
            }
        }
        return null;
    }

    /**
     * 判断当前每日一题解决状态
     * 该方法会被频繁调用, 因此采用本地变量缓存的形式进行判断, 而不是调用leetcode接口查询数据
     *
     * @return 返回1, 表示已经解决. 0表示无需修改图标状态. -1表示需要修改为未解决图标
     */
    public TodayIconStatusEnum todayQuestionSolved() {
        if (! needModify) {
            return TodayIconStatusEnum.NO_NEED_MODIFY;
        }
        return todaySolved ? TodayIconStatusEnum.SOLVED : TodayIconStatusEnum.NOT_SOLVED;
    }

    public void modified() {
        needModify  = false;
    }

    /**
     * 跟新每日一题解决状态
     */
    public void updateTodayStatus() {
        LeetcodeClient instance = LeetcodeClient.getInstance(project);
        var todayRecord = instance.getTodayRecord(project);
        todaySolved = "FINISH".equalsIgnoreCase(todayRecord.getUserStatus());
        if (todaySolved) {
            this.calendarSubmitRecord = instance.getCalendarSubmitRecord();
        }
        needModify = true;
    }

    @Subscribe
    public void loginEventListener(LoginEvent event) {
        updateTodayStatus();
    }

    @Subscribe
    public void clearCacheEvent(ClearCacheEvent event) {
        todaySolved = false;
        needModify  = true;
    }

    /**
     * 监听每日一题完成状态, 接收到该信息, 表明每日一题已被用户解决
     * @param event event
     */
    @Subscribe
    public void TodayQuestionOkEventListener(TodayQuestionOkEvent event) {
        todaySolved = true;
        needModify  = true;
        try {
            this.calendarSubmitRecord = LeetcodeClient.getInstance(project).getCalendarSubmitRecord();
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            // roll back
            this.calendarSubmitRecord = null;
        }
    }

    AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 该方法会被TodayQuestionAction频繁调用, 因此尽可能不要话费太多时间
     * @return String 今日一题的完成次数
     */
    public String getTodayQuestionCount() {
        if (calendarSubmitRecord == null) {
            // 如果为null, 则尝试异步获取一次
            TaskCenter.getInstance().createTask(() -> {
                // 如果超过五次都没有获取成功, 则放弃
                if (retryCount.get() < 5) {
                    TodayQuestionOkEventListener(null);
                } else {
                    // 终止获取, 同时修改图标状态的标识位
                    modified();
                }
                retryCount.incrementAndGet();
            }).invokeLater();

            return "NULL";
        }
        return String.valueOf(calendarSubmitRecord.getDailyQuestionStreakCount());
    }
}
