/**
 * 
 */
package com.xhf.leetcode.plugin.search.utils;

import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.utils.Safe;
import com.xhf.leetcode.plugin.utils.UnSafe;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl.TokenFactory.AbstractRule.operatorPattern;

/**
 * 字符集识别辅助工具类
 * @author 林良益
 *
 */
public class CharacterHelper {

	public static final char NULL = 0xffff;

	public static boolean isSpaceLetter(char input){
		return input == 8 || input == 9 
				|| input == 10 || input == 13 
				|| input == 32 || input == 160;
	}
	
	public static boolean isEnglishLetter(char input){
		return (input >= 'a' && input <= 'z') 
				|| (input >= 'A' && input <= 'Z');
	}
	
	public static boolean isArabicNumber(char input){
		return input >= '0' && input <= '9';
	}
	
	public static boolean isCJKCharacter(char input){
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				//全角数字字符和日韩字符
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				//韩文字符集
				|| ub == Character.UnicodeBlock.HANGUL_SYLLABLES 
				|| ub == Character.UnicodeBlock.HANGUL_JAMO
				|| ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
				//日文字符集
				|| ub == Character.UnicodeBlock.HIRAGANA //平假名
				|| ub == Character.UnicodeBlock.KATAKANA //片假名
				|| ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
				) {  
			return true;
		}else{
			return false;
		}
		//其他的CJK标点符号，可以不做处理
		//|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
		//|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
	}

	/*---------------------------------------------------下方代码作者: @auth feigebuge---------------------------------------------------------------*/
	
	/**
	 * 进行字符规格化（全角转半角，大写转小写处理）
	 * @param input
	 * @return char
	 */
	public static char regularize(char input){
        if (input == 12288) {
            input = (char) 32;
        }else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);
        }else if (input >= 'A' && input <= 'Z') {
        	input += 32;
		}
        return input;
	}

	/**
	 * 判断是否是变量名字
	 * @param s s
	 * @return boolean
	 */
	public static boolean isVName(String s) {
		if (! isVNameHead(s)) {
			return false;
		}
		char[] arr = s.toCharArray();
		char c = arr[0];
		if (c != '_' && !CharacterHelper.isEnglishLetter(c) && c != '$') {
			return false;
		}
		for (int i = 1; i < arr.length; ++i) {
			c = arr[i];
			if (! isVNameBody(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从第一个字符开始, 匹配合法变量名的长度
	 * eg:
	 * vname() : 5
	 * a.test() : 1
	 * (9 + 0) : 0
	 *
	 * @param s
	 * @return 长度
	 */
	public static int startVNameLen(String s) {
		if (! isVNameHead(s)) {
			return 0;
		}
		int len = 0;
		char[] arr = s.toCharArray();
		for (char c : arr) {
			if (isVNameBody(c)) {
				len += 1;
			} else  {
				break;
			}
		}
		return len;
	}

	public static boolean isVNameBody(char c) {
        return c == '_' || isEnglishLetter(c) || isArabicNumber(c) || c == '$';
    }

	/**
	 * 检查s是否以合法变量开头
	 * eg:
	 * arr : true
	 * $arr: true
	 * ( 1 + 1) : false
	 *
	 * @param s
	 * @return
	 */
	public static boolean isVNameHead(String s) {
		if (StringUtils.isBlank(s)) {
			return false;
		}
		char c = s.charAt(0);
        return c == '_' || CharacterHelper.isEnglishLetter(c) || c == '$';
    }

	/**
	 * 返回匹配括号结束的位置. 结束位置为对应括号的下一个字符
	 * eg:
	 * ()+1, 返回的则是+号的位置
	 * @param arr arr
	 * @param start start
	 * @return int
	 * @throws ComputeError: 括号匹配错误
	 */
	public static int matchBracket(char[] arr, int start) {
		Stack<Character> stack = new Stack<>();
		char c = arr[start];
		stack.push(c);
		int j = start + 1;
		for (; j < arr.length && ! stack.isEmpty(); ++j) {
			if (arr[j] == '(' || arr[j] == '[' || arr[j] == '{') {
				stack.push(arr[j]);
			} else if (arr[j] == ')' || arr[j] == ']' || arr[j] == '}') {
				Character pop = stack.pop();
				if (arr[j] == ')' && pop != '(') {
					throw new ComputeError("matchBracket error ! " + new String(arr, start, j - start));
				}
				if (arr[j] == ']' && pop != '[') {
					throw new ComputeError("matchBracket error ! " + new String(arr, start, j - start));
				}
				if (arr[j] == '}' && pop != '{') {
					throw new ComputeError("matchBracket error ! " + new String(arr, start, j - start));
				}
			}
		}
		if (! stack.isEmpty()) {
			throw new ComputeError("matchBracket error ! " + new String(arr, start, j - start));
		}
		return j;
	}


	public static int matchBracket(String s, int start) {
		return matchBracket(s.toCharArray(), start);
	}

	/**
	 * 匹配中括号, 要求start位置必须是[. 如果匹配成功, 返回匹配]的下一个位置. 如果匹配失败, 返回-1
	 * 该方法区别于{@link #matchArrayBracket(String, int)}, 如果匹配失败, 不会报错
	 * @param sub string
	 * @param start 开始位置
	 * @return end结尾位置
	 */
	@Safe
	public static int matchArrayBracketSafe(String sub, int start) {
		int res = -1;
		if (sub.charAt(start) != '[') {
			return res;
		}
		Stack<Character> stack = new Stack<>();
		for (int i = start; i < sub.length(); ++i) {
			char c = sub.charAt(i);
			if (c == '[') {
				stack.add(c);
			} else if (c == ']') {
				// 此处理应报错, 但方法是safe, 不进行异常的抛出
				if (stack.isEmpty()) {
					return -1;
				}
				stack.pop();
				// 检测下一位的合法性. 如果下一位不是[  , 说明数组括号已经匹配完毕
				if (i + 1 < sub.length() && stack.isEmpty() && sub.charAt(i + 1) != '[') {
					return i + 1;
				}
				res = i + 1;
			}
		}
		return res;
	}

	/**
	 * 匹配数组的中括号, 并返回结束位置. 结束位置是数组括号结束位置的下一位
	 * eg:
	 * arr[0][1]+1, 返回的是+号的位置
	 *
	 * @param sub string
	 * @param start 开始位置
	 * @return end
	 */
	@UnSafe("该方法如果无法匹配到合法括号, 会抛出异常" +
			"此外, 该方法还存在严重缺陷, 对于[]内还包含[], 可能会匹配得到错误的下标, 如a[b[1][2]]")
	@Deprecated
	public static int matchArrayBracket(String sub, int start) {
		Pattern pattern = Pattern.compile("\\[.*?\\](\\[.*?\\])*");
		Matcher m = pattern.matcher(sub);
		int r = -1;
		boolean b = m.find();
		if (b) {
			while (b) {
				if (m.start() >= start) {
					r = m.end();
					break;
				}
				b = m.find();
			}
		} else {
			return -1;
		}
		if (r == -1) {
			throw new ComputeError("matchArrayBracket error ! 不存在合法的数组括号 " + sub.substring(start));
		}
		return r;
	}

	/**
	 * 从start位置开始匹配, 匹配出链式调用的结束位置
	 * 此外, start位置必须是链式调用的开始标志 : '.'
	 * eg:
	 * matchChain(test.a.b().arr[0] + 1, 4) : 返回值是 17, 该位置表示链式调用结束
	 *
	 * @param sub string
	 * @param start start
	 * @return int
	 */
	public static int matchChain(String sub, int start) {
		if (sub.charAt(start) != '.') {
			throw new ComputeError("matchChain方法使用错误, sub[start] = " + sub.charAt(start) + "不是'.'! sub = " + sub + " start = " + start);
		}
		int end = start + 1;
		// 链式匹配是否结束
		boolean chainEnd = false;
        /*
         匹配链式: 匹配每一段的调用, 直到遇到空格, 换行符, tab
         如果遇到(, 则匹配直至对应的). 并设置chainEnd = true, 结束当前链式匹配. 如果下一个字符为'.'
         设置chainEnd为false, 开始新的链式匹配
         */
		for (; end < sub.length(); end++) {
			char c = sub.charAt(end);
			// 处于 链式匹配当中
			if (! chainEnd) {
				if (c == ' ' || c == '\n' || c == '\t') {
					chainEnd = true;
					break;
				} else if (c == '(') {
					// stack匹配到另一个)
					end = CharacterHelper.matchBracket(sub, end) - 1;
					chainEnd = true;
				} else if (c == '[') {
					// 匹配数组括号
					end = CharacterHelper.matchArrayBracketSafe(sub, end) - 1;
					chainEnd = true;
				} else if (end + 1 < sub.length() && sub.charAt(end + 1) == '.') {
					chainEnd = true;
				}
			} else {
				if (c != '.') {
					break;
				} else {
					chainEnd = false;
				}
			}
		}
		return end;
	}

	public static void main(String[] args) {
		// System.out.println(getChainCnt("test.a.b().arr[0] + 1", 4));
		System.out.println(matchBracket("{},{}", 0));
	}

	/**
	 * 从start位置开始匹配, 匹配出从start位置开始的链式调用的个数
	 * 此外, start位置必须是链式调用的开始标志 : '.'
	 * eg:
	 * getChainCnt(test.a.b().arr[0] + 1, 4) : 返回值是 3
	 *
	 * @param sub string
	 * @param start start
	 * @return count
	 */
	public static int getChainCnt(String sub, int start) {
		if (sub.charAt(start) != '.') {
			throw new ComputeError("getChainCnt方法使用错误, sub[start] = " + sub.charAt(start) + "不是'.'! sub = " + sub + " start = " + start);
		}
		int end = start + 1;
		int cnt = 1;
		// 链式匹配是否结束
		boolean chainEnd = false;
        /*
         匹配链式: 匹配每一段的调用, 直到遇到空格, 换行符, tab
         如果遇到(, 则匹配直至对应的). 并设置chainEnd = true, 结束当前链式匹配. 如果下一个字符为'.'
         设置chainEnd为false, 开始新的链式匹配
         */
		for (; end < sub.length(); end++) {
			char c = sub.charAt(end);
			// 处于 链式匹配当中
			if (! chainEnd) {
				if (c == ' ' || c == '\n' || c == '\t') {
					chainEnd = true;
					break;
				} else if (c == '(') {
					// stack匹配到另一个)
					end = CharacterHelper.matchBracket(sub, end) - 1;
					chainEnd = true;
				} else if (c == '[') {
					// 匹配数组括号
					end = CharacterHelper.matchArrayBracketSafe(sub, end) - 1;
					chainEnd = true;
				} else if (end + 1 < sub.length() && sub.charAt(end + 1) == '.') {
					chainEnd = true;
				}
			} else {
				if (c != '.') {
					break;
				} else {
					cnt += 1;
					chainEnd = false;
				}
			}
		}
		return cnt;
	}

	/**
	 * 判断是否是eval 表达式
	 * 详细规则可以查看{@link com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl.TokenFactory.EvalRule#match(String)}
	 * @param s s
	 * @return boolean
	 */
	public static boolean isEvalExpression(String s) {
		char[] arr = s.toCharArray();
		int len = arr.length;
		for (int i = 0; i < len; ++i) {
			char c = arr[i];
			if (c == '(' || c== '[' || c == '{') {
				// stack匹配. 无需考虑合法性, 因为存在语法检查
				i = CharacterHelper.matchBracket(arr, i) - 1;
			} else if (CharacterHelper.isArabicNumber(c)) {
				int j = i;
				while (j < len && CharacterHelper.isArabicNumber(arr[j])) {
					j += 1;
				}
				i = j - 1;
			} else if (c == '_' || CharacterHelper.isEnglishLetter(c) || c == '$') {
				int j = i;
				while (j < len &&
						(arr[j] == '_'
								|| CharacterHelper.isArabicNumber(arr[j])
								|| CharacterHelper.isEnglishLetter(arr[j])
								|| c == '$'
						)
				) {
					j += 1;
				}
				i = j - 1;
			} else if (operatorPattern.matcher(String.valueOf(c)).find()) {
				return true;
			}
		}
		return false;
	}
}