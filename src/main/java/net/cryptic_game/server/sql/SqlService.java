package net.cryptic_game.server.sql;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SqlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlService.class);
    private static final SqlService INSTANCE = new SqlService();

    private final SqlProperties properties;
    private SqlSessionFactory sessionFactory;

    private SqlService() {
        this.properties = new SqlProperties(StandardCharsets.UTF_8);

        try {
            this.properties.setServer(
                    SqlServerType.valueOf(Config.get(DefaultConfig.SQL_SERVER_TYPE).toUpperCase()),
                    Config.get(DefaultConfig.SQL_SERVER_LOCATION),
                    Config.get(DefaultConfig.SQL_SERVER_DATABASE)
            );
            this.properties.setUser(
                    Config.get(DefaultConfig.SQL_SERVER_USERNAME),
                    Config.get(DefaultConfig.SQL_SERVER_PASSWORD)
            );

            this.sessionFactory = new SqlSessionFactory(this.properties);
        } catch (IllegalArgumentException ignored) {
            LOGGER.error("Can't found sql serverType type {}.", Config.get(DefaultConfig.SQL_SERVER_TYPE));
        }
    }

    public static SqlService getInstance() {
        return INSTANCE;
    }

    public void start() {
        try {
            this.sessionFactory.build();
        } catch (SqlException e) {
            LOGGER.error("Unable to connect to the database server.", e);
        }
    }

    public Session openSession() {
        return this.sessionFactory.openSession();
    }

    public void addEntity(final Class<?> entity) {
        try {
            this.sessionFactory.addEntity(entity);
        } catch (SqlException e) {
            LOGGER.error("Unable to register entity " + entity.getName() + ".", e);
        }
    }
}
