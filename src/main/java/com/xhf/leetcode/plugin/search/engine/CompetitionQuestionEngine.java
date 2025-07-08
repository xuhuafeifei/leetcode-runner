package com.xhf.leetcode.plugin.search.engine;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.window.deepcoding.LCCompetitionPanel;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.lucene.queryParser.ParseException;

/**
 * CompetitionQuestion问题查询引擎, 索引问题题目. 对外提供题目搜索能力
 * <p>
 * 底层使用QuestionEngine提供的搜索能力, 避免索引的重复构建
 * 但本身具有极强的依赖性, 使用时Question的索引必须完成构建
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CompetitionQuestionEngine implements SearchEngine<CompetitionQuestion> {

    // 单例
    private static volatile CompetitionQuestionEngine instance;
    private final QuestionEngine questionEngine;

    private CompetitionQuestionEngine(Project project) {
        this.questionEngine = QuestionEngine.getInstance(project);
    }

    public static CompetitionQuestionEngine getInstance(Project project) {
        if (instance == null) {
            synchronized (CompetitionQuestionEngine.class) {
                if (instance == null) {
                    instance = new CompetitionQuestionEngine(project);
                }
            }
        }
        return instance;
    }

    @Override
    public void buildIndex(List<CompetitionQuestion> sources) throws Exception {
        throw new UnsupportedOperationException("not support buildIndex operation in CompetitionQuestionEngine");
    }

    @Override
    public List<CompetitionQuestion> search(String queryParam) throws IOException, ParseException {
        List<CompetitionQuestion> clist = LCCompetitionPanel.getCompetitionList();
        // 存储下标集合
        Set<Integer> ids = questionEngine.searchInner(queryParam);
        // 筛选clist
        return clist.stream().filter(c -> ids.contains(c.getID() - 1)).collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        // directory.close();
        // 暂时没想好关闭操作. 一开始想着如果长期不查询就可以关闭, 但是这样会丢失索引.
        // 下一次查询操作时, 需要重新加载索引. 但重新加载索引的行为不太好设计, 因此暂不考虑
        throw new UnsupportedOperationException("not support close operation in CompetitionQuestionEngine");
    }
}
