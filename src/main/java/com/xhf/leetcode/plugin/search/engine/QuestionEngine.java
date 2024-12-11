package com.xhf.leetcode.plugin.search.engine;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.search.dict.DictTree;
import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Question问题查询引擎, 索引问题题目. 对外提供题目搜索能力
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class QuestionEngine implements SearchEngine<Question> {
    /**
     * 文本分析器
     */
    private final LCAnalyzer analyzer;
    /**
     * 索引目录
     */
    private final Directory directory;
    /**
     * idea 项目
     */
    private final Project project;

    public QuestionEngine(Project project) {
        this.analyzer = new LCAnalyzer();
        this.directory = new RAMDirectory();
        this.project = project;
        // 提前初始化字典树
        DictTree.getInstance();
    }

    @Override
    public void buildIndex(List<Question> sources) throws Exception {
        IndexWriter iwriter = new IndexWriter(directory, analyzer, true , IndexWriter.MaxFieldLength.LIMITED);

        for (int i = 0; i < sources.size(); i++) {
            Document doc = new Document();
            doc.add(new Field("id", String.valueOf(i), Field.Store.YES, Field.Index.NO));
            doc.add(new Field("titleSlug", sources.get(i).toString(), Field.Store.YES, Field.Index.ANALYZED));

            iwriter.addDocument(doc);
        }

        iwriter.close();
    }

    @Override
    public List<Question> search(String queryParam) throws IOException, ParseException {
        //实例化搜索器
        IndexSearcher isearcher = new IndexSearcher(directory);

        QueryParser parser = new QueryParser(Version.LUCENE_29, "titleSlug", analyzer);
        Query query = parser.parse(queryParam);

        TopDocs topDocs = isearcher.search(query, 100);

        List<Question> totalQuestion = QuestionService.getInstance().getTotalQuestion(project);

        int length = topDocs.scoreDocs.length;
        List<Question> ans = new ArrayList<>(length);
        /*
            通过查询得到的对象, 通过索引构建时存储的idx信息
            从原始题目中找到对应的题目对象
         */
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document targetDoc = isearcher.doc(scoreDoc.doc);
            // 通过id在原始数据中找到匹配得到的题目对象
            int idx = Integer.parseInt(targetDoc.get("id"));
            ans.add(totalQuestion.get(idx));
        }

        return ans;
    }

    @Override
    public void close() throws IOException {
        // directory.close();
        // 暂时没想好关闭操作. 一开始想着如果长期不查询就可以关闭, 但是这样会丢失索引.
        // 下一次查询操作时, 需要重新加载索引. 但重新加载索引的行为不太好设计, 因此暂不考虑
        throw new UnsupportedOperationException("not support close operation in QuestionEngie");
    }
}
