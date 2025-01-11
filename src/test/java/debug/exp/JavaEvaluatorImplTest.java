package debug.exp;


import com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl;
import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;

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
        assertTrue(token instanceof JavaEvaluatorImpl.VariableTokenChain);
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
    public void testAb() {
        assert !JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern.matcher("a.invoke()").find();
        assert JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern.matcher("invoke()").find();

        JavaEvaluatorImpl.TokenFactory.MyMatcher matcher = JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern.matcher("invoke().invoke()");
        assert matcher.find();
        assert matcher.end() == 8;


        assert !JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern.matcher("1 + arr[0]").find();
        assert JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern.matcher("arr[0]").find();
        assert JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern.matcher("arr[0] + 1").find();
        assert JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern.matcher("arr[0].invoke() + 1").find();

        Matcher matcher1 = JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern.matcher("arr[0].invoke() + 1");
        assert matcher1.find();
        assert matcher1.end() == 6;
    }

    @Test
    public void testCharacterHelper() {
        assert 21 == CharacterHelper.matchChain(".abab.arr[0].invoke() + 1", 0);
        assert 31 == CharacterHelper.matchChain(".arr[0][0].abab.arr[0].invoke() + 1", 0);
        assert 35 == CharacterHelper.matchChain("test.arr[0][0].abab.arr[0].invoke() + 1", 4);


        assert 3 == CharacterHelper.getChainCnt(".abab.arr[0].invoke() + 1", 0);
        assert 4 == CharacterHelper.getChainCnt(".arr[0][0].abab.arr[0].invoke() + 1", 0);
        assert 4 == CharacterHelper.getChainCnt("test.arr[0][0].abab.arr[0].invoke() + 1", 4);

        assert new JavaEvaluatorImpl.TokenFactory.VariableRuleChain().match("a.test");
        assert ! new JavaEvaluatorImpl.TokenFactory.VariableRuleChain().match("a.test + 1");
    }

    @Test
    public void test() throws Exception {
        assert tokenFactory.parseToToken("1 + a.invoke(b, c, d)") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("1 + a.invoke(b, c, d)").getToken().equals("1 + a.invoke(b, c, d)");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d)") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d)").getToken().equals("a.invoke(1 + 2, c, d)");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d) + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d) + 1").getToken().equals("a.invoke(1 + 2, c, d) + 1");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).b") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).b").getToken().equals("a.invoke(1 + 2, c, d).b");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).b + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).b + 1").getToken().equals("a.invoke(1 + 2, c, d).b + 1");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0]") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0]").getToken().equals("a.invoke(1 + 2, c, d).arr[0]");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0] + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0] + 1").getToken().equals("a.invoke(1 + 2, c, d).arr[0] + 1");

        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0] + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("a.invoke(1 + 2, c, d).arr[0] + 1").getToken().equals("a.invoke(1 + 2, c, d).arr[0] + 1");

        assert tokenFactory.parseToToken("1 + arr[0].invoke(b, c, d)") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("1 + arr[0].invoke(b, c, d)").getToken().equals("1 + arr[0].invoke(b, c, d)");

        assert tokenFactory.parseToToken("a[0].invoke(b, c, d)") instanceof JavaEvaluatorImpl.ArrayTokenChain;
        assert tokenFactory.parseToToken("a[0].invoke(b, c, d)").getToken().equals("a[0].invoke(b, c, d)");

        assert tokenFactory.parseToToken("arr[0][1][b.test()]") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToToken("arr[0][1][b.test()] + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("arr[0][1][b.test()].b + 1") instanceof JavaEvaluatorImpl.EvalToken;

        assert tokenFactory.parseToToken("arr[0][1][b.test()]").getToken().equals("arr[0][1][b.test()]");

        assert tokenFactory.parseToToken("1.034") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToToken("\"ababa\"") instanceof JavaEvaluatorImpl.ConstantToken;

        assert tokenFactory.parseToToken("\"ababa\" + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("\"ababa\" + 1").getToken().equals("\"ababa\" + 1");

        assert tokenFactory.parseToToken("invoke(1 + 2, c, d)") instanceof JavaEvaluatorImpl.PureCallToken;
        assert tokenFactory.parseToToken("invoke(1 + 2, c, d)").getToken().equals("invoke(1 + 2, c, d)");

        assert tokenFactory.parseToToken("invoke(1 + 2, c, d).arr[0]") instanceof JavaEvaluatorImpl.PureCallTokenChain;
        assert tokenFactory.parseToToken("invoke(1 + 2, c, d).arr[0]").getToken().equals("invoke(1 + 2, c, d).arr[0]");

        assert tokenFactory.parseToToken("invoke(1 + 2, c, d).arr[0] + 1") instanceof JavaEvaluatorImpl.EvalToken;
        assert tokenFactory.parseToToken("invoke(1 + 2, c, d).arr[0] + 1").getToken().equals("invoke(1 + 2, c, d).arr[0] + 1");

        assert tokenFactory.parseToToken("[1][2]") instanceof JavaEvaluatorImpl.ArrayTokenChain; // [1][2]只有可能在处理链式调用时出现
    }

    @Test
    public void testParseStart() {
        assert tokenFactory.parseToTokenFromStart("1 + a.invoke(b, c, d)") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToTokenFromStart("1 + a.invoke(b, c, d)").getToken().equals("1");

        assert tokenFactory.parseToTokenFromStart("abc - 2") instanceof JavaEvaluatorImpl.VariableToken;
        assert tokenFactory.parseToTokenFromStart("abc - 2").getToken().equals("abc");

        assert tokenFactory.parseToTokenFromStart("a- 2") instanceof JavaEvaluatorImpl.VariableToken;
        assert tokenFactory.parseToTokenFromStart("a- 2").getToken().equals("a");

        assert tokenFactory.parseToTokenFromStart("a.invoke - 2") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke - 2").getToken().equals("a.invoke");

        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d) - 2") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d) - 2").getToken().equals("a.invoke(1 + 2, c, d)");

        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d) + 1") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d) + 1").getToken().equals("a.invoke(1 + 2, c, d)");

        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).b * 2") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).b * 2").getToken().equals("a.invoke(1 + 2, c, d).b");

        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).b + 1") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).b + 1").getToken().equals("a.invoke(1 + 2, c, d).b");

        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).arr[0] / 2") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart("a.invoke(1 + 2, c, d).arr[0] / 2").getToken().equals("a.invoke(1 + 2, c, d).arr[0]");

        assert tokenFactory.parseToTokenFromStart("1 + arr[0].invoke(b, c, d)") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToTokenFromStart("1 + arr[0].invoke(b, c, d)").getToken().equals("1");

        assert tokenFactory.parseToTokenFromStart("a[0].invoke(b, c, d) + 2") instanceof JavaEvaluatorImpl.ArrayTokenChain;
        assert tokenFactory.parseToTokenFromStart("a[0].invoke(b, c, d) + 2").getToken().equals("a[0].invoke(b, c, d)");

        assert tokenFactory.parseToTokenFromStart("arr[0][1][b.test()] ") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToTokenFromStart("arr[0][1][b.test()] ").getToken().equals("arr[0][1][b.test()]");

        assert tokenFactory.parseToTokenFromStart("1.034") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToTokenFromStart("\"ababa\"") instanceof JavaEvaluatorImpl.ConstantToken;

        assert tokenFactory.parseToTokenFromStart("\"ababa\" + 1") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToTokenFromStart("\"ababa\" + 1").getToken().equals("\"ababa\"");

        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d)") instanceof JavaEvaluatorImpl.PureCallToken;
        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d)").getToken().equals("invoke(1 + 2, c, d)");

        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d).arr[0]") instanceof JavaEvaluatorImpl.PureCallTokenChain;
        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d).arr[0]").getToken().equals("invoke(1 + 2, c, d).arr[0]");

        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d).arr[0] + 1") instanceof JavaEvaluatorImpl.PureCallTokenChain;
        assert tokenFactory.parseToTokenFromStart("invoke(1 + 2, c, d).arr[0] + 1").getToken().equals("invoke(1 + 2, c, d).arr[0]");
    }

    public void assertDebugError(String expression) {
        try {
            JavaEvaluatorImpl.AbstractToken.doCheck(expression);
            assert false;
        } catch (DebugError e) {
            e.printStackTrace();
            assert true;
        } catch (ComputeError e) {
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

//    /**
//     * 测试InvokeToken dot
//     */
//    @Test
//    public void test3() throws Exception {
//        JavaEvaluatorImpl.InvokeToken t = new JavaEvaluatorImpl.InvokeToken("a.invoke(b, c, d)", null);
//        assert "a".equals(t.getCallVName());
//        assert "a[test.demo]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo].invoke()", null).getCallVName());
//        assert "a[test.demo][b.a() + 2]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo][b.a() + 2].invoke()", null).getCallVName());
//        assert "a[test.demo][b.a() + 2]".equals(new JavaEvaluatorImpl.InvokeToken("a[test.demo][b.a() + 2].invoke(a, b.test(), c[1][2])", null).getCallVName());
//    }

    @Test
    public void test4() throws Exception {
        assert tokenFactory.parseToToken("1 + 2 + 3").getToken().equals("1 + 2 + 3");
        assert tokenFactory.parseToTokenFromStart("1 + 2 + 3").getToken().equals("1");
        assert tokenFactory.parseToTokenFromStart("<< 2 + 3").getToken().equals("<<");

        assert tokenFactory.parseToToken("a.test(b, c,d) + arr[2]").getToken().equals("a.test(b, c,d) + arr[2]");
        assert tokenFactory.parseToTokenFromStart("a.test(b, c,d) + arr[2]").getToken().equals("a.test(b, c,d)");

        assert tokenFactory.parseToToken("1 + b.invoke(a,b[1],1+2) + 3").getToken().equals("1 + b.invoke(a,b[1],1+2) + 3");
        assert tokenFactory.parseToTokenFromStart("1 + b.invoke(a,b[1],1+2) + 3").getToken().equals("1");
    }

    @Test
    public void test5() throws Exception {
        assert "() + ()".equals(tokenFactory.handleInput("  () + ()  "));
        assert "() + ()".equals(tokenFactory.handleInput("  ( () + () )  "));
        assert "() + ()".equals(tokenFactory.handleInput("  (  ( () + () ))  "));
    }

    @Test
    public void test6() throws Exception {
        assert tokenFactory.parseToTokenFromStart("+") instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToTokenFromStart("+").getToken().equals("+");

        assert tokenFactory.parseToTokenFromStart("+123") instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToTokenFromStart("+123").getToken().equals("+");

        assert tokenFactory.parseToTokenFromStart("+ 1  - 2 + a.invoke(a, b, c)") instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToTokenFromStart("+ 1  - 2 + a.invoke(a, b, c)").getToken().equals("+");

        assert tokenFactory.parseToTokenFromStart("+123") instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToTokenFromStart("+123").getToken().equals("+");

        assert tokenFactory.parseToTokenFromStart("a+2-3*a") instanceof JavaEvaluatorImpl.VariableToken;
        assert tokenFactory.parseToTokenFromStart("a+2-3*a").getToken().equals("a");

        assert tokenFactory.parseToTokenFromStart("1 +a*b-(1+2*c-a)") instanceof JavaEvaluatorImpl.ConstantToken;
        assert tokenFactory.parseToTokenFromStart("1 +a*b-(1+2*c-a)").getToken().equals("1");

        assert tokenFactory.parseToTokenFromStart("*b-(1+2*c-a)") instanceof JavaEvaluatorImpl.OperatorToken;
        assert tokenFactory.parseToTokenFromStart("*b-(1+2*c-a)").getToken().equals("*");

        assert tokenFactory.parseToTokenFromStart("arr[0][1] +a*b-(1+2*c-a)") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToTokenFromStart("arr[0][1] +a*b-(1+2*c-a)").getToken().equals("arr[0][1]");

        assert tokenFactory.parseToTokenFromStart("arr[0][a.test(a,b,1+3)] +a*b-(1+2*c-a)") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToTokenFromStart("arr[0][a.test(a,b,1+3)] +a*b-(1+2*c-a)").getToken().equals("arr[0][a.test(a,b,1+3)]");

        assert tokenFactory.parseToTokenFromStart(" b[0]") instanceof JavaEvaluatorImpl.ArrayToken;
        assert tokenFactory.parseToTokenFromStart(" b[0]").getToken().equals("b[0]");

        assert tokenFactory.parseToTokenFromStart(" b.invoke() + arr[0][1] + b[0]") instanceof JavaEvaluatorImpl.VariableTokenChain;
        assert tokenFactory.parseToTokenFromStart(" b.invoke() + arr[0][1] + b[0]").getToken().equals("b.invoke()");


        assert tokenFactory.parseToTokenFromStart("dfs(1,2,3)") instanceof JavaEvaluatorImpl.PureCallToken;
        assert tokenFactory.parseToTokenFromStart("dfs(1,2,3)").getToken().equals("dfs(1,2,3)");
    }

    @Test
    public void test7() throws Exception {
        JavaEvaluatorImpl.TokenFactory.MyMatcher matcher = JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern.matcher("dfs(1, (1 + 2) * 3, 4)");
        assert matcher.find();
        assert matcher.end() == (150 - 129 + 1);

        matcher = JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern.matcher("a.dfs(1, (1 + 2) * 3, 4)");
        assert ! matcher.find();

        matcher = JavaEvaluatorImpl.TokenFactory.AbstractRule.invokePattern.matcher("a.dfs(1, (1 + 2) * 3, 4)");
        assert matcher.find();
        assert matcher.end() == (111 - 88 + 1);
    }
}
