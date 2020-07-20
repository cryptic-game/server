package net.cryptic_game.server.user;

import net.cryptic_game.server.database.Database;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class Setting implements Serializable {

    @EmbeddedId
    private SettingKey key;

    @Column(name = "settingValue")
    private String value;

    private Setting(UUID user, String key, String value) {
        this.key = new SettingKey(user, key);
        this.value = value;
    }

    public Setting() {
    }

    public static Setting createSetting(User user, String key, String value) {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        Setting setting = new Setting(user.getUUID(), key, value);
        session.save(setting);

        session.getTransaction().commit();
        session.close();

        return setting;
    }

    public static Setting getSetting(User user, String key) {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        Setting setting = session.get(Setting.class, new SettingKey(user.getUUID(), key));

        session.getTransaction().commit();
        session.close();

        return setting;
    }

    public static List<Setting> getSettingsOfUser(User user) {
        try (Session session = Database.getInstance().openSession()) {
            return session.createQuery("select object(s) from Setting as s where s.key.user = :userId", Setting.class)
                    .setParameter("userId", user.getUUID())
                    .getResultList();
        }
    }

    public UUID getUser() {
        return this.key.user;
    }

    public String getKey() {
        return this.key.key;
    }

    public String getValue() {
        return value;
    }

    public void updateValue(String newValue) {
        this.value = newValue;
        update();
    }

    public void delete() {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.delete(this);

        session.getTransaction().commit();
        session.close();
    }

    private void update() {
        Session session = Database.getInstance().openSession();
        session.beginTransaction();

        session.update(this);

        session.getTransaction().commit();
        session.close();
    }

    @Embeddable
    public static class SettingKey implements Serializable {

        @Type(type = "uuid-char")
        private UUID user;

        @Column(length = 50, name = "settingKey")
        private String key;

        public SettingKey() {
        }

        SettingKey(UUID user, String key) {
            this.user = user;
            this.key = key;
        }
    }
}
