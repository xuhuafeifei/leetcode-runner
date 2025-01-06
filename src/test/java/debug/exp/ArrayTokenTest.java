package debug.exp;

import com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArrayTokenTest {

    private JavaEvaluatorImpl.ArrayToken token;
    private Context context;

    @Before
    public void setUp() {
        context = new Context();
    }

//    @Test
    public void getDimsStr_NoDimensions_EmptyList() {
        token = new JavaEvaluatorImpl.ArrayToken("int[]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(0, dims.size());
    }

    @Test
    public void getDimsStr_SingleDimension_SingleElementList() {
        token = new JavaEvaluatorImpl.ArrayToken("int[10]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(1, dims.size());
        assertEquals("10", dims.get(0));
    }

    @Test
    public void getDimsStr_MultipleDimensions_MultipleElementsList() {
        token = new JavaEvaluatorImpl.ArrayToken("int[10][20]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(2, dims.size());
        assertEquals("10", dims.get(0));
        assertEquals("20", dims.get(1));
    }

    @Test
    public void getDimsStr_NestedDimensions_NestedElementsList() {
        token = new JavaEvaluatorImpl.ArrayToken("int[10][20][30]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(3, dims.size());
        assertEquals("10", dims.get(0));
        assertEquals("20", dims.get(1));
        assertEquals("30", dims.get(2));
    }

    @Test
    public void getDimsStr_MixedDimensions_MixedElementsList() {
        token = new JavaEvaluatorImpl.ArrayToken("int[10][20][30][40]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(4, dims.size());
        assertEquals("10", dims.get(0));
        assertEquals("20", dims.get(1));
        assertEquals("30", dims.get(2));
        assertEquals("40", dims.get(3));
    }

    @Test
    public void test() {
        token = new JavaEvaluatorImpl.ArrayToken("arr[a.test()][b][c.invoke(a,b,1+2)]", context);
        List<String> dims = token.getDimsStr();
        assertEquals(3, dims.size());
        assertEquals("a.test()", dims.get(0));
        assertEquals("b", dims.get(1));
        assertEquals("c.invoke(a,b,1+2)", dims.get(2));
    }
}
