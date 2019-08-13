package net.cryptic_game.server.database;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Calendar;
import java.util.Properties;

public class Database {

    private static Database instance;

    private SessionFactory sessionFactory;

    public Database() {
        instance = this;

        try {
            Configuration cfg = getConfiguration();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(cfg.getProperties()).build();

            sessionFactory = cfg.buildSessionFactory(serviceRegistry);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }

    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();

        Properties settings = new Properties();

        settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        settings.put(Environment.URL, "jdbc:mysql://" + Config.get(DefaultConfig.MYSQL_HOSTNAME) + ":" + Config.get(DefaultConfig.MYSQL_PORT)
                        + "/" + Config.get(DefaultConfig.MYSQL_DATABASE) + "?serverTimezone=" + Calendar.getInstance().getTimeZone().getID());
        settings.put(Environment.USER, Config.get(DefaultConfig.MYSQL_USERNAME));
        settings.put(Environment.PASS, Config.get(DefaultConfig.MYSQL_PASSWORD));
        settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.HBM2DDL_AUTO, "validate");

        configuration.setProperties(settings);

        configuration.addAnnotatedClass(net.cryptic_game.server.user.User.class);
        configuration.addAnnotatedClass(net.cryptic_game.server.user.Session.class);

        return configuration;
    }

    public static Database getInstance() {
        return instance;
    }
}
