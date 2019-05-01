package net.cryptic_game.server.database;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends Database {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public SQLiteDatabase(String name) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + Config.get(DefaultConfig.STORAGE_LOCATION) + name));
    }

}
