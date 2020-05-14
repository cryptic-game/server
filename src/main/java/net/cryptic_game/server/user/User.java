package net.cryptic_game.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.cryptic_game.server.database.Database;
import org.apache.commons.validator.routines.EmailValidator;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;
    @Type(type = "text")
    private String name;
    @Type(type = "text")
    private String password;
    private Date created;
    private Date last;

    private User(UUID uuid, String name, String password, Date created, Date last) {
        this.uuid = uuid;
        this.name = name;
        this.password = password;
        this.created = created;
        this.last = last;
    }

    public User() {
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLast() {
        return last;
    }

    public boolean checkPassword(String password) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), this.password);

        return result.verified;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (isValidPassword(newPassword) && checkPassword(oldPassword)) {
            this.password = hashPassword(newPassword);

            Session session = Database.getInstance().openSession();
            session.beginTransaction();

            session.update(this);

            session.getTransaction().commit();
            session.close();

            return true;
        }
        return false;
    }

    public void delete() {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.delete(this);

        session.getTransaction().commit();
        session.close();
    }

    public String toString() {
        return this.getName();
    }

    public void updateLast() {
        this.last = new Date(Calendar.getInstance().getTime().getTime());

        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.update(this);

        session.getTransaction().commit();
        session.close();
    }

    public static User get(UUID uuid) {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        User user = session.get(User.class, uuid);

        session.getTransaction().commit();
        session.close();

        return user;
    }

    public static User get(String name) {
        Session session = Database.getInstance().openSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> from = criteria.from(User.class);

        criteria.select(from);
        criteria.where(builder.equal(from.get("name"), name));
        TypedQuery<User> typed = session.createQuery(criteria);

        User user;

        try {
            user = typed.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            session.close();
        }

        return user;
    }

    public static User create(String name, String password) {
        if (isValidPassword(password) && get(name) == null) {
            UUID uuid = UUID.randomUUID();

            Date now = new Date(Calendar.getInstance().getTime().getTime());
            String hash = hashPassword(password);
            User user = new User(uuid, name, hash, now, now);

            Session session = Database.getInstance().openSession();
            session.beginTransaction();

            session.save(user);

            session.getTransaction().commit();
            session.close();

            return user;
        }

        return null;
    }

    public static boolean isValidPassword(String password) {
        return Pattern.compile("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}").matcher(password).find();
    }

    private static String hashPassword(String toHash) {
        return BCrypt.withDefaults().hashToString(12, toHash.toCharArray());
    }
}
