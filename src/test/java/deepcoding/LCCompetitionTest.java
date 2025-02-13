package deepcoding;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCCompetitionTest {

    public List<Question> queryTotalQuestion() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.PROBLEM_SET_QUERY);

        List<Question> ans = new ArrayList<>();
        boolean flag = true;
        int skip = 0, limit = 100;
        while (flag) {
            GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams.ParamsBuilder()
                    .setCategorySlug("all-code-essentials")
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();
            body.setBySearchParams(params);

            HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                    .setBody(body.toJsonStr())
                    .setContentType("application/json")
                    .addBasicHeader()
                    .build();

            HttpClient httpClient = HttpClient.getInstance();
            HttpResponse httpResponse = httpClient.executePost(httpRequest, null);
            String resp = httpResponse.getBody();

            // parse json to array
            JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
            JsonObject pql = jsonObject.getAsJsonObject("data").getAsJsonObject("problemsetQuestionList");

            // no questions left
            if (! pql.get("hasMore").getAsBoolean()) {
                flag = false;
            }
            // parse json array
            JsonArray jsonArray = pql.getAsJsonArray("questions");
            List<Question> questions = GsonUtils.fromJsonArray(jsonArray, Question.class);
            // merge
            ans.addAll(questions);

            body.clear();
            skip = skip + limit;
        }

        return ans;
    }

    @Test
    public void test1() throws IOException {
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\data.json");
        List<CompetitionQuestion> list = GsonUtils.fromJsonToList(s, CompetitionQuestion.class);
        // 查询所有数据
        List<Question> questions = queryTotalQuestion();
        // 填充
        for (CompetitionQuestion competitionQuestion : list) {
            int id = competitionQuestion.getID();
            Question question = questions.get(id - 1);
            assert question.getTitleSlug().equals(competitionQuestion.getTitleSlug());
            competitionQuestion.setDifficulty(question.getDifficulty());
            competitionQuestion.setFid(question.getFrontendQuestionId());
            competitionQuestion.setAlgorithm(question.getTopicTags().stream().map(TopicTag::getNameTranslated).collect(Collectors.joining(",")));
        }
        FileUtils.createAndWriteFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\dataProcessed.json", GsonUtils.toJsonStr(list));
    }

    @Test
    public void test2() {
        // 读取a.json
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\data.json");
        List<CompetitionQuestion> list = GsonUtils.fromJsonToList(s, CompetitionQuestion.class);

        Set<String> set = new TreeSet<>();
        for (CompetitionQuestion c : list) {
            set.add(c.getContestID_zh());
        }
        for (String se : set) {
            System.out.println("map.addPair(" + "\"" + se + "\"" + ", " + "\"" + se + "\"" + ");");
        }
    }

    @Test
    public void test3() {
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\data.txt");
        s = GsonUtils.fromJson(s, String.class);
        System.out.println(s);
    }

    public static String convertUnicodeToString(String unicode) {
        StringBuilder sb = new StringBuilder();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 去除前后多余的空格
            String hexValue = hex[i].trim();
            // 检查是否是有效的四位十六进制数
            if (hexValue.length() == 4 && hexValue.matches("[0-9a-fA-F]+")) {
                int data = Integer.parseInt(hexValue, 16);
                sb.append((char) data);
            } else {
                // 如果不是有效的四位十六进制数，可以选择跳过或抛出异常
                System.out.println("Invalid hex value: " + hexValue);
            }
        }
        return sb.toString();
    }

}
