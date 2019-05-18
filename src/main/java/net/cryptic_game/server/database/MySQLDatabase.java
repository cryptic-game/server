package net.cryptic_game.server.database;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase extends Database {

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + Config.get(DefaultConfig.MYSQL_HOSTNAME) + ":"
				+ Config.getInteger(DefaultConfig.MYSQL_PORT) + "/" + Config.get(DefaultConfig.MYSQL_DATABASE)
				+ "?autoReconnect=true" + "&user=" + Config.get(DefaultConfig.MYSQL_USERNAME) + "&password="
				+ Config.get(DefaultConfig.MYSQL_PASSWORD));
	}

}
