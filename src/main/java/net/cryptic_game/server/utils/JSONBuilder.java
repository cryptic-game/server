package net.cryptic_game.server.utils;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JSONBuilder {

    private final Map<String, Object> jsonMap;

    private JSONBuilder() {
        jsonMap = new HashMap<>();
    }

    public static JSONBuilder anJSON() {
        return new JSONBuilder();
    }

    public static JSONObject simple(String key, Object value) {
        return anJSON().add(key, value).build();
    }

    public static JSONObject error(String message) {
        return simple("error", message);
    }

    public JSONBuilder add(String key, Object value) {
        jsonMap.put(key, value);
        return this;
    }

    public JSONObject build() {
        return new JSONObject(jsonMap);
    }
}
