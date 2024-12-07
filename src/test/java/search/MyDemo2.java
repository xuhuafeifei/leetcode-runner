package search;

import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
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
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MyDemo2 {
    public static void main(String[] args) throws Exception {
        var analyzer = new LCAnalyzer();
        Directory directory = new RAMDirectory();

        IndexWriter iwriter = new IndexWriter(directory, analyzer, true , IndexWriter.MaxFieldLength.LIMITED);

        BufferedReader reader = new BufferedReader(new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt", StandardCharsets.UTF_8));
        String s;
        int i = 0;
        while ((s = reader.readLine()) != null) {
            Document doc = new Document();
            doc.add(new Field("id", String.valueOf(i), Field.Store.YES, Field.Index.NO));
            doc.add(new Field("titleSlug", s, Field.Store.YES, Field.Index.ANALYZED));

            iwriter.addDocument(doc);
            i += 1;
        }
        iwriter.close();

        // search
        //实例化搜索器
        IndexSearcher isearcher = new IndexSearcher(directory);

        Scanner sc = new Scanner(System.in);

        while (true) {
            String next = sc.next();
//            Query query = IKQueryParser.parse("titleSlug", next);
            QueryParser parser = new QueryParser(Version.LUCENE_29, "titleSlug", analyzer);
            Query query = parser.parse(next);

            TopDocs topDocs = isearcher.search(query, 100);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document targetDoc = isearcher.doc(scoreDoc.doc);
                System.out.println(targetDoc.toString());
            }
        }
    }
}
