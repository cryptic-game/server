package net.cryptic_game.server.user;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.sql.SqlService;
import org.hibernate.Transaction;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "session")
public class Session implements Serializable {

    private static final int EXPIRE = Config.getInteger(DefaultConfig.SESSION_EXPIRE) * 1000;

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;
    @Id
    @Type(type = "uuid-char")
    private UUID token;
    @Id
    @Type(type = "uuid-char")
    private UUID user;
    private boolean valid;
    private Date created;

    private Session(UUID uuid, UUID token, User user, Date created, boolean valid) {
        this.uuid = uuid;
        this.token = token;
        this.user = user.getUUID();
        this.valid = valid;
        this.created = created;
    }

    public Session() {
    }

    public static Session create(final User user) {
        final Session newSession = new Session(UUID.randomUUID(), UUID.randomUUID(), user,
                new Date(Calendar.getInstance().getTime().getTime()), true);

        try (org.hibernate.Session session = SqlService.getInstance().openSession()) {
            final Transaction transaction = session.beginTransaction();
            session.save(newSession);
            transaction.commit();
            return newSession;
        }
    }

    public static Session get(final UUID token) {
        try (org.hibernate.Session session = SqlService.getInstance().openSession()) {
            return session.createQuery("select object (s) from Session s where s.token = :token", Session.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static List<Session> getSessionsOfUser(final User user) {
        try (org.hibernate.Session session = SqlService.getInstance().openSession()) {
            return session.createQuery("select object(s) from Session s where s.user = :userId", Session.class)
                    .setParameter("userId", user.getUUID())
                    .getResultList();
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public UUID getToken() {
        return token;
    }

    public User getUser() {
        return User.get(user);
    }

    public Date getCreated() {
        return created;
    }

    public boolean isValid() {
        return this.valid && this.created.before(new Date(Calendar.getInstance().getTime().getTime() + EXPIRE));
    }

    public void breakSession() {
        this.valid = false;
        try (org.hibernate.Session session = SqlService.getInstance().openSession()) {
            final Transaction transaction = session.beginTransaction();
            session.update(this);
            transaction.commit();
        }
    }

    public void delete() {
        try (org.hibernate.Session session = SqlService.getInstance().openSession()) {
            final Transaction transaction = session.beginTransaction();
            session.delete(this);
            transaction.commit();
        }
    }

    public String toString() {
        return this.getToken().toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;
        Session session = (Session) o;
        return isValid() == session.isValid() &&
                this.uuid.equals(session.uuid) &&
                this.getToken().equals(session.getToken()) &&
                this.getUser().equals(session.getUser()) &&
                this.getCreated().equals(session.getCreated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.getToken(), this.getUser(), this.isValid(), this.getCreated());
    }
}
