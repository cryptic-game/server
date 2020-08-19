package net.cryptic_game.server.sql;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MariaDB102Dialect;
import org.hibernate.dialect.MariaDB103Dialect;
import org.hibernate.dialect.MariaDB10Dialect;
import org.hibernate.dialect.MariaDB53Dialect;

public enum SqlServerType {

    MARIADB_05_03("mariadb", "org.mariadb.jdbc.Driver", MariaDB53Dialect.class),
    MARIADB_10_00("mariadb", "org.mariadb.jdbc.Driver", MariaDB10Dialect.class),
    MARIADB_10_02("mariadb", "org.mariadb.jdbc.Driver", MariaDB102Dialect.class),
    MARIADB_10_03("mariadb", "org.mariadb.jdbc.Driver", MariaDB103Dialect.class);

    private final String urlPrefix;
    private final String driver;
    private final Class<? extends Dialect> dialect;

    SqlServerType(final String urlPrefix, final String driver, final Class<? extends Dialect> dialect) {
        this.urlPrefix = urlPrefix;
        this.driver = driver;
        this.dialect = dialect;
    }

    public String getUrlPrefix() {
        return this.urlPrefix;
    }

    public String getDriver() {
        return this.driver;
    }

    public Class<? extends Dialect> getDialect() {
        return this.dialect;
    }
}
