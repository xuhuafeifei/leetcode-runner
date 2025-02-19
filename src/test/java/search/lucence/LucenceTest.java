package search.lucence;

import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LucenceTest {
    @Test
    public void test() throws Exception {
        Document doc = new Document();
        doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NO));
        doc.add(new Field("titleSlug", "hello-world", Field.Store.YES, Field.Index.ANALYZED));

        var analyzer = new LCAnalyzer();
        var directory = new RAMDirectory();
        IndexWriter iwriter = new IndexWriter(directory, analyzer, true , IndexWriter.MaxFieldLength.LIMITED);
        iwriter.addDocument(doc);
    }
}
