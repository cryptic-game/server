package net.cryptic_game.server.user;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.database.Database;
import org.hibernate.Criteria;
import org.hibernate.annotations.Type;
import org.hibernate.criterion.Restrictions;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "session")
public class Session implements Serializable {

    private static int EXPIRE = Config.getInteger(DefaultConfig.SESSION_EXPIRE) * 1000;

    @Id
    @Type(type="uuid-char")
    private UUID uuid;
    @Id
    @Type(type="uuid-char")
    private UUID token;
    @Id
    @Type(type="uuid-char")
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
        org.hibernate.Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.save(this);
        session.getTransaction().commit();
        session.close();
    }

    public void delete() {
        org.hibernate.Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.delete(this);

        session.getTransaction().commit();
        session.close();
    }

    public String toString() {
        return this.getToken().toString();
    }

    public static Session create(User user) {
        Session newSession = new Session(UUID.randomUUID(), UUID.randomUUID(), user,
                new Date(Calendar.getInstance().getTime().getTime()), true);

        org.hibernate.Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.save(newSession);

        session.getTransaction().commit();
        session.close();

        return newSession;
    }

    public static Session get(UUID token) {
        org.hibernate.Session session = Database.getInstance().openSession();

        Criteria crit = session.createCriteria(Session.class);
        crit.add(Restrictions.eq("token", token));
        List<Session> results = crit.list();

        if(results.size() == 0) {
            session.close();
            return null;
        }

        session.close();

        return results.get(0);
    }
}
