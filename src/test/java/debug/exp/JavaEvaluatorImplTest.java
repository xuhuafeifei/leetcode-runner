package debug.exp;


import com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl;
import com.xhf.leetcode.plugin.exception.DebugError;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JavaEvaluatorImplTest {

    private JavaEvaluatorImpl.TokenFactory tokenFactory;

    @Before
    public void setUp() {
        tokenFactory = new JavaEvaluatorImpl.TokenFactory();
    }

    @Test
    public void parseToToken_CalculationOperator_ReturnsEvalToken() throws Exception {
        JavaEvaluatorImpl.Token token = tokenFactory.parseToToken("+");
        assertTrue(token instanceof JavaEvaluatorImpl.OperatorToken);
    }

    @Test
    public void parseToToken_InvokeExpression_ReturnsInvokeToken() throws Exception {
        JavaEvaluatorImpl.Token token = tokenFactory.parseToToken("obj.method()");
        assertTrue(token instanceof JavaEvaluatorImpl.InvokeToken);
    }

    @Test
    public void parseToToken_VariableName_ReturnsVariableToken() throws Exception {
        JavaEvaluatorImpl.Token token = tokenFactory.parseToToken("varName");
        assertTrue(token instanceof JavaEvaluatorImpl.VariableToken);
    }

    @Test
    public void parseToToken_ArrayExpression_ReturnsArrayToken() throws Exception {
        JavaEvaluatorImpl.Token token = tokenFactory.parseToToken("array[0]");
        assertTrue(token instanceof JavaEvaluatorImpl.ArrayToken);
    }

    @Test
    public void parseToToken_Constant_ReturnsConstantToken() throws Exception {
        JavaEvaluatorImpl.Token token = tokenFactory.parseToToken("123");
        assertTrue(token instanceof JavaEvaluatorImpl.ConstantToken);
    }

//    @Test(expected = DebugError.class)
    @Deprecated
    public void parseToToken_NoMatchingRule_ThrowsDebugError() throws Exception {
        // 这属于非法表达式, 会在语法检查时抛出异常
        tokenFactory.parseToToken("a[");
    }

    @Test
    public void test() throws Exception {
        assert tokenFactory.parseToToken("1 + a.invoke(b, c, d)") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d)") instanceof JavaEvaluatorImpl.InvokeToken;
        assert tokenFactory.parseToToken("1 + arr[0].invoke(b, c, d)") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a[0].invoke(b, c, d)") instanceof JavaEvaluatorImpl.InvokeToken;
        assert tokenFactory.parseToToken("arr[0][1][b.test()]") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToToken("1.034") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToToken("\"ababa\"") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToToken("\"ababa\" + 1") instanceof JavaEvaluatorImpl.EvalToken;
    }

    public void assertDebugError(String expression) {
        try {
            JavaEvaluatorImpl.AbstractToken.doCheck(expression);
            assert false;
        } catch (DebugError e) {
            e.printStackTrace();
            assert true;
        }
    }

    public void assertNoDebugError(String expression) {
        try {
            JavaEvaluatorImpl.AbstractToken.doCheck(expression);
            assert true;
        } catch (DebugError e) {
            e.printStackTrace();
            assert false;
        }
    }


    @Test
    public void test2() throws Exception{
        assertNoDebugError("1 + a.invoke(b, c, d)");
        assertDebugError("[(1)");
        assertDebugError("a.invoke(1 + 2, b, c).test()");
        assertNoDebugError("a.invoke(b.test(), c[0][1][2].demo()) + 2");
        assertDebugError("[(1)");
        assertDebugError("a.invoke(1 += 2, b, c).test()");
        assertDebugError("a.invoke(i ++, b, c).test()");
        assertDebugError("arr[1][j--]");
        assertDebugError("arr[1][j-=1]");
    }

    /**
     * 测试InvokeToken dot
     */
    @Test
    public void test3() throws Exception {
        JavaEvaluatorImpl.InvokeToken t = new JavaEvaluatorImpl.InvokeToken("a.invoke(b, c, d)", null);
        assert "a".equals(t.getCallVName());
        assert "a[test.demo]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo].invoke()", null).getCallVName());
        assert "a[test.demo][b.a() + 2]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo][b.a() + 2].invoke()", null).getCallVName());
        assert "a[test.demo][b.a() + 2]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo][b.a() + 2].invoke(a, b.test(), c[1][2])", null).getCallVName());
    }

    @Test
    public void test4() throws Exception {
        assert tokenFactory.parseToToken("1 + 2 + 3", null, 0).getToken().equals("1");
        assert tokenFactory.parseToToken("1 + 2 + 3", null, 2).getToken().equals("+");
        assert tokenFactory.parseToToken("1 << 2 + 3", null, 2).getToken().equals("<<");
        assert tokenFactory.parseToToken("a.test(b, c,d) + arr[2]", null, 15).getToken().equals("+");
        assert tokenFactory.parseToToken("1 + b.invoke(a,b[1],1+2) + 3", null, 4).getToken().equals("b.invoke(a,b[1],1+2)");
    }

    @Test
    public void test5() throws Exception {
        assert "() + ()".equals(tokenFactory.handleInput("  () + ()  "));
        assert "() + ()".equals(tokenFactory.handleInput("  ( () + () )  "));
        assert "() + ()".equals(tokenFactory.handleInput("  (  ( () + () ))  "));
    }

    @Test
    public void test6() throws Exception {
        assert tokenFactory.parseToToken("+", null, 0) instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToToken("+", null, 0).getToken().equals("+");

        assert tokenFactory.parseToToken("+123", null, 0) instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToToken("+123", null, 0).getToken().equals("+");

        assert tokenFactory.parseToToken("+ 1  - 2 + a.invoke(a, b, c)", null, 0) instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToToken("+ 1  - 2 + a.invoke(a, b, c)", null, 0).getToken().equals("+");

        assert tokenFactory.parseToToken("2+123", null, 1) instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToToken("2+123", null, 1).getToken().equals("+");

        assert tokenFactory.parseToToken("a+2-3*a", null, 0) instanceof JavaEvaluatorImpl.VariableToken;
        assert tokenFactory.parseToToken("a+2-3*a", null, 0).getToken().equals("a");

        assert tokenFactory.parseToToken("1 +a*b-(1+2*c-a)", null, 0) instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToToken("1 +a*b-(1+2*c-a)", null, 0).getToken().equals("1");

        assert tokenFactory.parseToToken("1 +a*b-(1+2*c-a)", null, 4) instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToToken("1 +a*b-(1+2*c-a)", null, 4).getToken().equals("*");

        assert tokenFactory.parseToToken("arr[0][1] +a*b-(1+2*c-a)", null, 0) instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToToken("arr[0][1] +a*b-(1+2*c-a)", null, 0).getToken().equals("arr[0][1]");

        assert tokenFactory.parseToToken("1+arr[0][a.test(a,b,1+3)] +a*b-(1+2*c-a)", null, 2) instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToToken("1+arr[0][a.test(a,b,1+3)]+a*b-(1+2*c-a)", null, 2).getToken().equals("arr[0][a.test(a,b,1+3)]");

        assert tokenFactory.parseToToken("1 + arr[0][1] + b[0]", null, 16) instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToToken("1 + arr[0][1] + b[0]", null, 16).getToken().equals("b[0]");

        assert tokenFactory.parseToToken("1 + b.invoke() + arr[0][1] + b[0]", null, 4) instanceof JavaEvaluatorImpl.InvokeToken;
        assert tokenFactory.parseToToken("1 + b.invoke() + arr[0][1] + b[0]", null, 4).getToken().equals("b.invoke()");
    }
}
