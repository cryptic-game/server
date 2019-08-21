package net.cryptic_game.server.config;

import java.util.HashMap;
import java.util.Map;

public enum DefaultConfig {

    WEBSOCKET_HOST("0.0.0.0"),
    WEBSOCKET_PORT(80),
    MSSOCKET_HOST("127.0.0.1"),
    MSSOCKET_PORT(1239),
    HTTP_PORT(8080),
    AUTH_ENABLED(true),
    STORAGE_LOCATION("data/"),
    MYSQL_HOSTNAME("localhost"),
    MYSQL_USERNAME("cryptic"),
    MYSQL_PASSWORD("cryptic"),
    MYSQL_DATABASE("cryptic"),
    MYSQL_PORT(3306),
    PRODUCTIVE(true),
    SESSION_EXPIRE(60 * 60 * 24 * 2), // 2 days
    RESPONSE_TIMEOUT(20), // 20 seconds
    LOG_LEVEL("INFO");

    private Object value;

    DefaultConfig(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * @return map with all key-value pairs
     */
    public static Map<String, String> defaults() {
        Map<String, String> defaults = new HashMap<>();

        for (DefaultConfig e : DefaultConfig.values()) {
            defaults.put(e.toString(), e.getValue().toString());
        }

        return defaults;
    }

}
