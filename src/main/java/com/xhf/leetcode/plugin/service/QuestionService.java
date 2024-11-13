package com.xhf.leetcode.plugin.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.window.LCPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * deal with http module and question ui module
 */
public class QuestionService {
    private static QuestionService qs = new QuestionService();

    public static QuestionService getInstance() {
        return qs;
    }

    /**
     * load questions data
     */
    public void loadAllQuestionData(Project project, LCPanel.MyList myList) {
        // do not use another thread to get dataContext by DataManager
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading...", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // query
                List<Question> totalQuestion = LeetcodeClient.getInstance(project).getTotalQuestion();
                myList.setListData(totalQuestion);
                myList.updateUI();
            }
        });
    }

    /**
     * fill question with code snippets, translated title and content
     * @param question
     */
    public void fillQuestion(Question question, Project project) {
        /* check if the question needs to fill content */
        if (StringUtils.isNotBlank(question.getTranslatedTitle())
                && StringUtils.isNotBlank(question.getTranslatedContent())
                && StringUtils.isNotBlank(question.getCodeSnippets())
        ) {
            return;
        }

        // get a lang type
        String langType = AppSettings.getInstance().getLangType();

        GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams();
        params.setTitleSlug(question.getTitleSlug());

        HttpResponse httpResponse = LeetcodeClient.getInstance(project).queryQuestionInfo(params);

        String resp = httpResponse.getBody();

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject questionJsonObj = jsonObject.getAsJsonObject("data").getAsJsonObject("question");

        String translatedTitle = questionJsonObj.get("translatedTitle").getAsString();
        String translatedContent = questionJsonObj.get("translatedContent").getAsString().replaceAll("\n\n", "");
        String codeSnippets = null;

        for (JsonElement item : questionJsonObj.getAsJsonArray("codeSnippets")) {
            JsonObject obj = item.getAsJsonObject();
            if (GsonUtils.fromJson(obj.get("lang"), String.class).equals(langType)) {
                codeSnippets = obj.get("code").getAsString();
                break;
            }
        }

        // fill target obj
        question.setTranslatedTitle(translatedTitle);
        question.setTranslatedContent(translatedContent);
        question.setCodeSnippets(codeSnippets);
    }

    /**
     * pick on question for random
     * @param project
     * @return
     */
    public Question pickOne(Project project) {
        LeetcodeClient instance = LeetcodeClient.getInstance(project);

        return instance.getRandomQuestion(project);
    }

    /**
     * choose daily question
     * @param project
     */
    public void todayQuestion(Project project) {
        LeetcodeClient instance = LeetcodeClient.getInstance(project);
        Question todayQuestion = instance.getTodayQuestion(project);

        CodeService.openCodeEditor(todayQuestion, project);
    }
}
