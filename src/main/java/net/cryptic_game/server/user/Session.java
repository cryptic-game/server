package net.cryptic_game.server.user;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

public class Session {

	private static int EXPIRE = Config.getInteger(DefaultConfig.SESSION_EXPIRE) * 1000;

	private UUID uuid;
	private UUID token;
	private User user;
	private boolean vaild;
	private Date created;

	private Session(UUID uuid, UUID token, User user, Date created, boolean valid) {
		this.uuid = uuid;
		this.token = token;
		this.user = user;
		this.vaild = valid;
		this.created = created;
	}

	public UUID getUUID() {
		return uuid;
	}

	public UUID getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

	public Date getCreated() {
		return created;
	}

	public boolean isValid() {		
		return this.vaild && this.created.before(new Date(Calendar.getInstance().getTime().getTime()+ EXPIRE));
	}

	public void breakSession() {
		this.vaild = false;
		User.db.update("UPDATE `session` SET `valid`=" + this.isValid() + " WHERE `uuid`='" + this.getUUID() + "'");
	}

	public void delete() {
		User.db.update("DELETE FROM `session` WHERE `uuid`='" + this.getUUID() + "';");
	}

	public String toString() {
		return this.getToken().toString();
	}

	public static Session create(User user) {
		Session session = new Session(UUID.randomUUID(), UUID.randomUUID(), user,
				new Date(Calendar.getInstance().getTime().getTime()), true);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		User.db.update("INSERT INTO `session` (`uuid`, `token`, `user`, `created`, `valid`) VALUES ('"
				+ session.getUUID().toString() + "', " + "'" + session.getToken().toString() + "', '"
				+ session.getUser().getUUID().toString() + "', '" + sdf.format(session.getCreated()) + "', " + session.isValid()
				+ ")");

		return session;
	}

	public static Session get(UUID token) {
		ResultSet rs = User.db.getResult("SELECT * FROM `session` WHERE `token`='" + token.toString() + "';");

		try {
			if (rs.next()) {
				return new Session(UUID.fromString(rs.getString("uuid")), UUID.fromString(rs.getString("token")),
						User.get(UUID.fromString(rs.getString("user"))), rs.getDate("created"), rs.getBoolean("valid"));
			}
		} catch (SQLException e) {
		}

		return null;
	}

}
