package net.cryptic_game.server.sql;

import org.hibernate.cfg.Environment;

import java.nio.charset.Charset;
import java.util.Properties;

class SqlProperties extends Properties {

    SqlProperties(final Charset charset) {
        super();
        this.put(Environment.AUTOCOMMIT, "false");
        this.put(Environment.HBM2DDL_AUTO, "update");
        this.put(Environment.HBM2DDL_CHARSET_NAME, charset.toString());
        this.put(Environment.CONNECTION_PREFIX + ".CharSet", charset.toString());
        this.put(Environment.CONNECTION_PREFIX + ".characterEncoding", charset.toString());
        this.put(Environment.CONNECTION_PREFIX + ".useUnicode", "true");
        this.put("hibernate.hikari.connectionTimeout", "10000"); // 10 seconds
        this.put("hibernate.hikari.initializationFailTimeout", "30000"); // 30 seconds
    }

    void setServer(final SqlServerType server, final String location, final String database) {
        this.put(Environment.URL, "jdbc:" + server.getUrlPrefix() + ":" + location + "/" + database + "");
        this.put(Environment.DRIVER, server.getDriver());
        this.put(Environment.DIALECT, server.getDialect().getName());
    }

    void setUser(final String username, final String password) {
        this.put(Environment.USER, username);
        this.put(Environment.PASS, password);
    }
}
