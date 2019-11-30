package net.cryptic_game.server.user;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class Setting implements Serializable  {

    @Id
    @Type(type = "uuid-char")
    private UUID user;

    @Id
    @Column(length = 50, name = "settingKey")
    private String key;

    @Column(name = "settingValue")
    private String value;
}
