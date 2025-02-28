package com.xhf.leetcode.plugin.search.engine;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.search.dict.DictTree;
import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
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
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

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
    private Directory directory;
    /**
     * idea 项目
     */
    private final Project project;
    /**
     * 题目排序比较器
     */
    private final QuestionCompare questionCompare;
    // 单例
    private static volatile QuestionEngine instance;
    public static QuestionEngine getInstance(Project project) {
        if (instance == null) {
            synchronized (QuestionEngine.class) {
                if (instance == null) {
                    instance = new QuestionEngine(project);
                }
            }
        }
        return instance;
    }

    private QuestionEngine(Project project) {
        this.analyzer = new LCAnalyzer();
        try {
            String path = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("lucence").build();
            this.directory = new NIOFSDirectory(FileUtils.createAndGetDirectory(path));
        } catch (Exception e) {
            LogUtils.warn("QuestionEngine create NIOSDirectory failed! the reason is " + e.getMessage()
                    + "\nthis may cause memory pressure on the user's computer! "
            );
            this.directory = new RAMDirectory();
        }
        this.project = project;
        this.questionCompare = new QuestionCompare();
        // 提前初始化字典树
        DictTree.init();
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
        // query如何设置自己的分词器
        Query query = parser.parse(queryParam);

        TopDocs topDocs = isearcher.search(query, 100);

//        long start = System.currentTimeMillis();
        List<Question> questions = normalSort(topDocs, isearcher);
//        LogUtils.debug("normalSort花费时间: " + (System.currentTimeMillis() - start));

        return questions;
    }

    protected Set<Integer> searchInner(String queryParam) throws IOException, ParseException {
        //实例化搜索器
        IndexSearcher isearcher = new IndexSearcher(directory);

        QueryParser parser = new QueryParser(Version.LUCENE_29, "titleSlug", analyzer);
        // query如何设置自己的分词器
        Query query = parser.parse(queryParam);

        TopDocs topDocs = isearcher.search(query, 100);

        Set<Integer> set = new HashSet<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document targetDoc = isearcher.doc(scoreDoc.doc);
            // 通过id在原始数据中找到匹配得到的题目对象
            int idx = Integer.parseInt(targetDoc.get("id"));
            set.add(idx);
        }
        return set;
    }

    /**
     * 按照题目序号从小打到排序
     * <p>
     * 1~2 millis second. 效率极高, 无需堆排序优化
     * @param topDocs
     * @param isearcher
     * @return
     * @throws IOException
     */
    private List<Question> normalSort(TopDocs topDocs, IndexSearcher isearcher) throws IOException {
        List<Question> totalQuestion = QuestionService.getInstance(project).getTotalQuestion(project);

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
        ans.sort(questionCompare);

        return ans;
    }

    static class QuestionCompare implements Comparator<Question> {
        private final String[] types;

        public QuestionCompare() {
            types = new String[]{"LCP", "LCR", "面试题"};
        }

        @Override
        public int compare(Question o1, Question o2) {
            String s1 = o1.getFrontendQuestionId();
            String s2 = o2.getFrontendQuestionId();

            // 判断s1和s2是否为纯数字
            boolean isNumber1 = s1.matches("\\d+");
            boolean isNumber2 = s2.matches("\\d+");

            // 如果一个是数字，排在前面
            if (isNumber1 && !isNumber2) {
                return -1;
            } else if (!isNumber1 && isNumber2) {
                return 1;
            }

            // 如果都是数字，直接比较数字的大小
            if (isNumber1 && isNumber2) {
                return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
            }

            // 处理 LCP、LCR、面试题 排序
            int type1Index = getTypeIndex(s1, types);
            int type2Index = getTypeIndex(s2, types);

            // 如果类型不同，按类型顺序比较
            if (type1Index != type2Index) {
                return Integer.compare(type1Index, type2Index);
            }

            // 类型相同，比较数字部分
            int num1 = Integer.parseInt(s1.replaceAll("\\D+", ""));
            int num2 = Integer.parseInt(s2.replaceAll("\\D+", ""));
            return Integer.compare(num1, num2);
        }

        // 获取类型的索引，LCP -> 0, LCR -> 1, 面试题 -> 2
        private int getTypeIndex(String s, String[] types) {
            for (int i = 0; i < types.length; i++) {
                if (s.startsWith(types[i])) {
                    return i;
                }
            }
            return -1; // 默认返回-1
        }
    }

    @Override
    public void close() throws IOException {
        // directory.close();
        // 暂时没想好关闭操作. 一开始想着如果长期不查询就可以关闭, 但是这样会丢失索引.
        // 下一次查询操作时, 需要重新加载索引. 但重新加载索引的行为不太好设计, 因此暂不考虑
        throw new UnsupportedOperationException("not support close operation in QuestionEngie");
    }
}
