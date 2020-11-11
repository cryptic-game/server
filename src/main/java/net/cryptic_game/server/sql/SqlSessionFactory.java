package net.cryptic_game.server.sql;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class SqlSessionFactory implements AutoCloseable {

    private final Configuration configuration;
    private SessionFactory sessionFactory;

    SqlSessionFactory(final SqlProperties properties) {
        this.configuration = new Configuration();
        this.configuration.addProperties(properties);
    }

    void build() throws SqlException {
        try {
            this.sessionFactory = this.configuration.buildSessionFactory(new StandardServiceRegistryBuilder()
                    .applySettings(this.configuration.getProperties())
                    .enableAutoClose()
                    .build());
        } catch (HibernateException e) {
            throw e.getCause() == null ? new SqlException(e.getMessage(), e) : new SqlException(e.getCause().getMessage().split(":", 2)[1]);
        }
    }

    void addEntity(final Class<?> entity) throws SqlException {
        if (this.sessionFactory == null) this.configuration.addAnnotatedClass(entity);
        else throw new SqlException("It's too late to register any more entities.");
    }

    Session openSession() {
        return this.sessionFactory.openSession();
    }

    @Override
    public void close() {
        this.sessionFactory.close();
    }
}
