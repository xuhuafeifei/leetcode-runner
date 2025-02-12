package deepcoding;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class interview150Test {

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
    public void test1() {
        // 读取a.json
        String content = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\a.json");
        assert content != null;
        JsonElement jsonElement = JsonParser.parseString(content);
        JsonArray asJsonArray = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("studyPlanV2ProgressDetail")
                .getAsJsonObject().get("studyPlanDetail").getAsJsonObject()
                .get("planSubGroups").getAsJsonArray();

        Set<String> set = new HashSet<>(120);

        for (JsonElement element : asJsonArray) {
            for (JsonElement q : element.getAsJsonObject().get("questions").getAsJsonArray()) {
                String slug = q.getAsJsonObject().get("titleSlug").getAsString();
                set.add(slug);
            }
        }

        List<Question> totalQuestion = queryTotalQuestion();
        List<Question> collect = totalQuestion.stream().filter(e -> set.contains(e.getTitleSlug())).collect(Collectors.toList());
        String res = "";
        for (Question question : collect) {
            res += (Integer.parseInt(question.getFrontendQuestionId()) - 1) + ",";
        }
        System.out.println(res);
    }

    @Test
    public void test2() {
        // 读取a.json
        String content = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\c.json");
        assert content != null;
        JsonElement jsonElement = JsonParser.parseString(content);
        JsonArray asJsonArray = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("studyPlanV2Detail")
                .getAsJsonObject().get("planSubGroups").getAsJsonArray();


        for (JsonElement element : asJsonArray) {
            JsonObject o = element.getAsJsonObject();
            String name = o.get("name").getAsString();
            JsonArray arr = o.get("questions").getAsJsonArray();
            String k = "";
            for (JsonElement a : arr) {
                String fid = a.getAsJsonObject().get("questionFrontendId").getAsString();
                k += (Integer.parseInt(fid) - 1) + ",";
            }
            if (k.endsWith(",")) {
                k = k.substring(0, k.length() - 1);
            }
            System.out.println("map.addPair(" + "\"" + name + "\"" + ", " + "\"" + k + "\"" + ");");
        }
    }

    @Test
    public void test3() {
        // 读取a.json
        String content = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\test\\java\\deepcoding\\c.json");
        assert content != null;
        JsonElement jsonElement = JsonParser.parseString(content);
        JsonArray asJsonArray = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("studyPlanV2Detail")
                .getAsJsonObject().get("planSubGroups").getAsJsonArray();

        List<Integer> ids = new ArrayList<>();
        for (JsonElement element : asJsonArray) {
            JsonObject o = element.getAsJsonObject();
            String name = o.get("name").getAsString();
            JsonArray arr = o.get("questions").getAsJsonArray();
            for (JsonElement a : arr) {
                String fid = a.getAsJsonObject().get("questionFrontendId").getAsString();
                ids.add(Integer.parseInt(fid) - 1);
            }
        }
        String join = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(join);
    }
}
