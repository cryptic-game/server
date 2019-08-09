package net.cryptic_game.server.utils;

import org.json.simple.JSONObject;

public class JSON {

    private JSONObject obj;

    public JSON(JSONObject obj) {
        this.obj = obj;
    }

    public <T> T get(String key) {
        if(obj.containsKey(key)) {
            try {
                return (T) obj.get(key);
            } catch(ClassCastException ignored) {
            }
        }

        return null;
    }

}
