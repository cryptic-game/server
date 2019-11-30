package net.cryptic_game.server.user;

import net.cryptic_game.server.database.Database;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class Setting implements Serializable  {

    @EmbeddedId
    private SettingKey key;

    @Column(name = "settingValue")
    private String value;
}
