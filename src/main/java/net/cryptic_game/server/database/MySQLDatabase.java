package net.cryptic_game.server.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

public class MySQLDatabase extends Database {
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public MySQLDatabase() throws SQLException {
		super(DriverManager.getConnection("jdbc:mysql://" + Config.get(DefaultConfig.MYSQL_HOSTNAME) + ":"
				+ Config.getInteger(DefaultConfig.MYSQL_PORT) + "/" + Config.get(DefaultConfig.MYSQL_DATABASE)
				+ "?user=" + Config.get(DefaultConfig.MYSQL_USERNAME) + "&password="
				+ Config.get(DefaultConfig.MYSQL_PASSWORD)));
	}

}
