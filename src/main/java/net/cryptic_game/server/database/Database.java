package net.cryptic_game.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    private Connection connection;

    Database(Connection connection) {
        this.connection = connection;
    }

    public ResultSet getResult(String query, Object... args) {
        try {
            PreparedStatement statement;
            statement = this.connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void update(String query, Object... args) {
        try {
            PreparedStatement statement;
            statement = this.connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
