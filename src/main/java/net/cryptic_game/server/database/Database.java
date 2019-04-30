package net.cryptic_game.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    private Connection connection;

    public Database(Connection connection) {
        this.connection = connection;
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
