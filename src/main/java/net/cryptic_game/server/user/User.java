package net.cryptic_game.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.cryptic_game.server.database.Database;
import org.apache.commons.validator.routines.EmailValidator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Type;
import org.hibernate.criterion.Restrictions;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private String mail;
    @Type(type = "text")
    private String password;
    private Date created;
    private Date last;

    private User(UUID uuid, String name, String mail, String password, Date created, Date last) {
        this.uuid = uuid;
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.created = created;
        this.last = last;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLast() {
        return last;
    }

    public boolean checkPassword(String password) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), password);

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

        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("name", name));
        List<User> results = criteria.list();

        session.close();

        if (results.size() == 0) {
            return null;
        }

        return results.get(0);
    }

    public static User create(String name, String mail, String password) {
        if (isValidMailAddress(mail) && isValidPassword(password) && get(name) == null) {
            UUID uuid = UUID.randomUUID();

            Date now = new Date(Calendar.getInstance().getTime().getTime());
            String hash = hashPassword(password);
            User user = new User(uuid, name, mail, hash, now, now);

            Session session = Database.getInstance().openSession();
            session.beginTransaction();

            session.save(user);

            session.getTransaction().commit();
            session.close();

            return user;
        }

        return null;
    }

    public static boolean isValidMailAddress(String mail) {
        return EmailValidator.getInstance().isValid(mail);
    }

    public static boolean isValidPassword(String password) {
        return Pattern.compile("(?=.*\\d)((?=.*[a-z])|(?=.*[A-Z]))(?=.*[!\"#$%`&\\\\'()*+,.\\/:;<=>?@\\[\\]^_{|}~-]).{8,}").matcher(password).find();
    }

    private static String hashPassword(String toHash) {
        return BCrypt.withDefaults().hashToString(12, toHash.toCharArray());
    }
}
