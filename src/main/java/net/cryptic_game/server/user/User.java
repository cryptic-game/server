package net.cryptic_game.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import net.cryptic_game.server.database.Database;

public class User {

	private static Database db = new Database("user");

	static {
		db.update("CREATE TABLE IF NOT EXISTS `user` (uuid TEXT PRIMARY KEY, name TEXT, password TEXT);");
	}

	private UUID uuid;
	private String name;
	private String password;

	private User(UUID uuid, String name, String password) {
		this.uuid = uuid;
		this.name = name;
		this.password = password;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public boolean checkPassword(String password) {
		Result result = BCrypt.verifyer().verify(password.toCharArray(), this.password);

		return result.verified;
	}

	public void delete() {
		db.update("DELETE FROM `user` WHERE `uuid`='" + this.getUUID() + "';");
	}
	
	public String toString() {
		return this.getName();
	}

	public static User get(UUID uuid) {
		ResultSet rs = db.getResult("SELECT * FROM `user` WHERE `uuid`='" + uuid.toString() + "';");

		try {
			if (rs.next()) {
				return new User(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("password"));
			}
		} catch (SQLException e) {
		}

		return null;
	}

	public static User get(String name) {
		ResultSet rs = db
				.getResult("SELECT * FROM `user` WHERE `name`='" + name + "';");

		try {
			if (rs.next()) {
				return new User(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("password"));
			}
		} catch (SQLException e) {
		}

		return null;
	}

	public static User create(String name, String password) {
		ResultSet rs = db.getResult("SELECT * FROM `user` WHERE `name`='" + name + "'");

		try {
			if (!rs.next()) {
				UUID uuid = UUID.randomUUID();

				User user = new User(uuid, name, password);

				db.update("INSERT INTO `user` (`uuid`, `name`, `password`) VALUES ('" + uuid.toString() + "', '" + name
						+ "', '" + User.hashPassword(password) + "');");

				return user;
			}
		} catch (SQLException e) {
		}
		return null;
	}

	private static String hashPassword(String toHash) {
		return BCrypt.withDefaults().hashToString(12, toHash.toCharArray());
	}

}
