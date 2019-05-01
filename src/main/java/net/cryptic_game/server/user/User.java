package net.cryptic_game.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.database.Database;
import net.cryptic_game.server.database.SQLiteDatabase;
import net.cryptic_game.server.database.MySQLDatabase;
import org.apache.commons.validator.routines.EmailValidator;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

    protected static Database db = null;

    static {

        try {
            if (Config.getBoolean(DefaultConfig.PRODUCTIVE)) {
                db = new MySQLDatabase();
            } else {
                db = new SQLiteDatabase("user.db");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.update(
                "CREATE TABLE IF NOT EXISTS `user` (uuid VARCHAR(36) PRIMARY KEY, name TEXT, mail TEXT, password TEXT, created DATETIME, last DATETIME);");
        db.update(
                "CREATE TABLE IF NOT EXISTS `session` (uuid VARCHAR(36), token VARCHAR(36), user VARCHAR(36), valid BOOLEAN, created DATETIME, PRIMARY KEY(uuid, token, user),  FOREIGN KEY (user) REFERENCES user(uuid));");
    }

    private UUID uuid;
    private String name;
    private String mail;
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
        Result result = BCrypt.verifyer().verify(password.toCharArray(), this.password);

        return result.verified;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (this.checkPassword(oldPassword) && isValidPassword(newPassword)) {
            String hash = hashPassword(newPassword);

            db.update("UPDATE `user` SET `password`='" + hash + "' WHERE `uuid`='" + this.getUUID().toString() + "'");

            this.password = hash;

            return true;
        }
        return false;
    }

    public void delete() {
        db.update("DELETE FROM `user` WHERE `uuid`='" + this.getUUID() + "';");
    }

    public String toString() {
        return this.getName();
    }

    public void updateLast() {
        Date now = new Date(Calendar.getInstance().getTime().getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        db.update("UPDATE `user` SET `last`='" + sdf.format(now) + "' WHERE `uuid`='" + this.getUUID() + "';");
    }

    public static User get(UUID uuid) {
        ResultSet rs = db.getResult("SELECT * FROM `user` WHERE `uuid`='" + uuid.toString() + "';");

        try {
            if (rs.next()) {
                return new User(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("mail"),
                        rs.getString("password"), rs.getDate("created"), rs.getDate("last"));
            }
        } catch (SQLException e) {
        }

        return null;
    }

    public static User get(String name) {
        ResultSet rs = db.getResult("SELECT * FROM `user` WHERE `name`='" + name + "';");

        try {
            if (rs.next()) {
                return new User(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("mail"),
                        rs.getString("password"), rs.getDate("created"), rs.getDate("last"));
            }
        } catch (SQLException e) {
        }

        return null;
    }

    public static User create(String name, String mail, String password) {
        ResultSet rs = db.getResult("SELECT * FROM `user` WHERE `name`='" + name + "'");

        try {
            if (isValidPassword(password) && isValidMailAddress(mail) && !rs.next()) {
                UUID uuid = UUID.randomUUID();

                Date now = new Date(Calendar.getInstance().getTime().getTime());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                User user = new User(uuid, name, mail, password, now, now);

                db.update("INSERT INTO `user` (`uuid`, `name`, `mail`, `password`, `created`, `last`) VALUES ('"
                        + uuid.toString() + "', '" + name + "', '" + mail + "', '" + User.hashPassword(password) + "', '"
                        + sdf.format(now) + "', '" + sdf.format(now) + "');");

                return user;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    public static boolean isValidMailAddress(String mail) {
        return EmailValidator.getInstance().isValid(mail);
    }

    public static boolean isValidPassword(String password) {
        return Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}").matcher(password).find();
    }

    private static String hashPassword(String toHash) {
        return BCrypt.withDefaults().hashToString(12, toHash.toCharArray());
    }

}
