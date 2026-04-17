package com.vbay.shared.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().create();

    private JsonUtils() {
    }

    public static Gson getGson() {
        return gson;
    }

    public static String toJson(Object obj) {
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(JsonElement jsonElement, Class<T> clazz) {
        try {
            return gson.fromJson(jsonElement, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject toJsonObject(Object obj) {
        return gson.toJsonTree(obj).getAsJsonObject();
    }
}
