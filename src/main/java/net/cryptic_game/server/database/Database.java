package net.cryptic_game.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

public class Database {

	private Connection connection;

	public Database(String name) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String path = Config.get(DefaultConfig.STORAGE_LOCATION) + name;
		
		try {
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet getResult(String query) {
		PreparedStatement statement;
		try {
			statement = this.connection.prepareStatement(query);

			return statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void update(String query) {
		PreparedStatement statement;
		try {
			statement = this.connection.prepareStatement(query);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
