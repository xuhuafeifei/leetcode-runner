package debug;

import com.xhf.leetcode.plugin.debug.params.InstParser;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.StdInReader;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ParserAndReader {
    @Test
    public void test1() throws Exception {
        InstReader reader = new StdInReader();
        InstParser parser = new InstParserImpl();
        String inst;
        while ((inst = reader.readInst()) != null) {
            Instrument parse = parser.parse(inst);
            System.out.println(parser);
        }
    }

    public static void main(String[] args) {
        InstReader reader = new StdInReader();
        InstParser parser = new InstParserImpl();
        String inst;
        while ((inst = reader.readInst()) != null) {
            Instrument instrument = parser.parse(inst);
            System.out.println(instrument);
        }
    }
}
