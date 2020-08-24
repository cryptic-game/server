package net.cryptic_game.server.config;

import net.cryptic_game.server.sql.SqlServerType;

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

    SQL_SERVER_TYPE(SqlServerType.MARIADB_10_03.toString()),
    SQL_SERVER_LOCATION("//localhost:3306"),
    SQL_SERVER_USERNAME("cryptic"),
    SQL_SERVER_PASSWORD("cryptic"),
    SQL_SERVER_DATABASE("cryptic"),

    MYSQL_PORT(3306),
    PRODUCTIVE(true),
    SESSION_EXPIRE(60 * 60 * 24 * 2), // 2 days
    RESPONSE_TIMEOUT(20), // 20 seconds
    LOG_LEVEL("WARN"),
    SENTRY_DSN("");

    private final Object value;

    DefaultConfig(Object value) {
        this.value = value;
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

    public Object getValue() {
        return value;
    }

}
