package net.cryptic_game.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public abstract class Database {

    private Connection connection;

    private static final Logger logger = Logger.getLogger(Database.class);

    Database() {
        try {
            connection = createConnection();
        } catch (SQLException ignored) {
        }
    }

    private boolean isDisconnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void reconnect() {
        logger.error("lost connection to database... trying to reconnect");
        while (true) {
            try {
                connection = createConnection();
                logger.info("reconnected to database");
                return;
            } catch (SQLException ignored) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } // 0.5 seconds
            }
        }
    }

    public ResultSet getResult(String query, Object... args) {
        if (isDisconnected()) {
            reconnect();
        }

        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            return statement.executeQuery();
        } catch (Exception e) {
            reconnect();
            return getResult(query, args);
        }
    }

    public void update(String query, Object... args) {
        if(isDisconnected()) {
            reconnect();
        }

        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            statement.executeUpdate();
        } catch (Exception e) {
            reconnect();
            update(query, args);
        }
    }

    public abstract Connection createConnection() throws SQLException;

}
