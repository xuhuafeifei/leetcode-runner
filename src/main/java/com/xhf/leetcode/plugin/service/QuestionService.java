package com.xhf.leetcode.plugin.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.QLoadEndEvent;
import com.xhf.leetcode.plugin.bus.QLoadStartEvent;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * deal with http module and question ui module
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class QuestionService {
    private static final QuestionService qs = new QuestionService();

    public static QuestionService getInstance() {
        return qs;
    }

    /**
     * load questions data
     */
    public void loadAllQuestionData(Project project) {
        LCEventBus.getInstance().post(new QLoadStartEvent(project));
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading...", false) {
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
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading...", false) {
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
        /* check if the question needs to fill content */
        if (StringUtils.isNotBlank(question.getTranslatedTitle())
                && StringUtils.isNotBlank(question.getTranslatedContent())
                && StringUtils.isNotBlank(question.getCodeSnippets())
                && StringUtils.isNotBlank(question.getQuestionId())
                && StringUtils.isNotBlank(question.getExampleTestcases())
        ) {
            return;
        }
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

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject questionJsonObj = jsonObject.getAsJsonObject("data").getAsJsonObject("question");

        String translatedTitle = questionJsonObj.get("translatedTitle").getAsString();
        String translatedContent = questionJsonObj.get("translatedContent").getAsString().replaceAll("\n\n", "");
        String questionId = questionJsonObj.get("questionId").getAsString();
        String exampleTestcases = questionJsonObj.get("exampleTestcases").getAsString();
        String codeSnippets = null;

        for (JsonElement item : questionJsonObj.getAsJsonArray("codeSnippets")) {
            JsonObject obj = item.getAsJsonObject();
            if (GsonUtils.fromJson(obj.get("lang"), String.class).equalsIgnoreCase(langType)) {
                codeSnippets = obj.get("code").getAsString();
                break;
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

        CodeService.openCodeEditor(todayQuestion, project);
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
}
