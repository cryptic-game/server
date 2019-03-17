package net.cryptic_game.server;

import org.apache.log4j.BasicConfigurator;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.microservice.MicroServiceServerInitializer;
import net.cryptic_game.server.socket.SocketSever;
import net.cryptic_game.server.websocket.WebSocketServerInitializer;

/**
 * cryptic-game-server for managing microservices
 * 
 * @author use-to
 * @author cryptic-game.net
 * @version 0.0.1-SNAPSHOT
 * 
 */

public class App {

	public static void main(String[] args) {
		BasicConfigurator.configure();

		new SocketSever("microservice", Config.get(DefaultConfig.MSSOCKET_HOST),
				Config.getInteger(DefaultConfig.MSSOCKET_PORT), new MicroServiceServerInitializer());
		new SocketSever("websocket", Config.get(DefaultConfig.WEBSOCKET_HOST),
				Config.getInteger(DefaultConfig.WEBSOCKET_PORT), new WebSocketServerInitializer(), true);
	}

}
