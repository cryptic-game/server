package net.cryptic_game.server.utils;

import org.json.simple.JSONObject;

import java.util.UUID;

public class JSON {

    private JSONObject obj;

    public JSON(JSONObject obj) {
        this.obj = obj;
    }

    public String get(String key) {
        return get(key, String.class);
    }

    public UUID getUUID(String key) {
        String value = get(key, String.class);

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    public <T> T get(String key, Class<? extends T> type) {
        if (obj.containsKey(key) && type.isInstance(obj.get(key))) {
            try {
                return type.cast(obj.get(key));
            } catch (ClassCastException ignored) {
            }
        }

        return null;
    }

}
