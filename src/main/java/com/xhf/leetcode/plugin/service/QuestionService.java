package com.xhf.leetcode.plugin.service;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.window.LCPanel;
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
        // do not use other thread to dataContext by DataManager
//        LCPanel.MyList myList = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_QUESTION_LIST);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "loginSuccess", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // query
                List<Question> totalQuestion = LeetcodeClient.getInstance(project).getTotalQuestion();
                myList.setListData(totalQuestion);
                myList.updateUI();
            }
        });
//        ApplicationManager.getApplication().invokeLater(() -> {
//            // query
//            List<Question> totalQuestion = LeetcodeClient.getTotalQuestion();
//            LCPanel.MyList myList = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_QUESTION_LIST);
//            myList.setListData(totalQuestion);
//            myList.updateUI();
//        });
    }

    /**
     * page query data
     * @param project
     */
//    public void loadQuestionDataByPageInfo(Project project) {
//        QSetPanel qSetPanel = LCToolWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_QUESTION_SET_PANEL);
//        // get page info
//        PagePanel.PageInfo pageInfo = qSetPanel.getPagePanel().getPageInfo();
//        Integer skip = pageInfo.getPage() * pageInfo.getPageSize();
//        Integer pageSize = pageInfo.getPageSize();
//
//        ApplicationManager.getApplication().invokeLater(() -> {
//            // query
//            List<Question> totalQuestion = LeetcodeClient.getQuestionList(
//                    new SearchParams.ParamsBuilder()
//                        .basicParams()
//                        .setSkip(skip)
//                        .setLimit(pageSize)
//                        .build());
//
//            MyTable myTable = qSetPanel.getMyTable();
//            myTable.updateData(totalQuestion);
//        });
//    }
}
