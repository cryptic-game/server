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
			this.connection = this.createConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public boolean isConnected() {
    	try {
			return this.connection != null && !this.connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
    }
    
    public void reconnect() {
    	logger.error("lost connection to database... trying to reconnect");
    	while(true) {
	    	try {
				this.connection = this.createConnection();
				logger.info("reconnected to database");
				return;
			} catch (SQLException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} // 0.5 seconds
			}
    	}
    }
    
    public ResultSet getResult(String query, Object... args) {
    	if(!this.isConnected()) {
    		this.reconnect();
    	}
    	
        try {
            PreparedStatement statement;
            statement = this.connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            return statement.executeQuery();
        } catch (Exception e) {
        	this.reconnect();
        	return this.getResult(query, args);
        }
    }

    public void update(String query, Object... args) {
    	if(!this.isConnected()) {
    		this.reconnect();
    	}
    	
        try {
            PreparedStatement statement;
            statement = this.connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);

            statement.executeUpdate();
        } catch (Exception e) {
        	this.reconnect();
        	this.update(query, args);
        }
    }
    
    public abstract Connection createConnection() throws SQLException;

}
