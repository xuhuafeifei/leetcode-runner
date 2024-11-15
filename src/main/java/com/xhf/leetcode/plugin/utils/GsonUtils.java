package com.xhf.leetcode.plugin.utils;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GsonUtils {
    private static final Gson gson = new Gson();


    public static String toJsonStr(JsonObject jsonObject) {
        return gson.toJson(jsonObject);
    }

    public static String toJsonStr(Object o) {
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJson(JsonElement jsonEle, Class<T> clazz) {
        return gson.fromJson(jsonEle, clazz);
    }

    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        return fromJsonArray(jsonArray, clazz);
    }
    public static <T> List<T> fromJsonArray(JsonArray jsonArray, Class<T> clazz) {
        Iterator<JsonElement> it = jsonArray.iterator();
        List<T> ans = new ArrayList<>();
        while (it.hasNext()) {
            T res = GsonUtils.fromJson(it.next(), clazz);
            ans.add(res);
        }
        return ans;
    }
}
