package net.cryptic_game.server;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.http.HTTPServer;
import net.cryptic_game.server.microservice.MicroServiceServerInitializer;
import net.cryptic_game.server.socket.SocketSever;
import net.cryptic_game.server.websocket.WebSocketServerInitializer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class App {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.toLevel(Config.get(DefaultConfig.LOG_LEVEL)));
        BasicConfigurator.configure();

        new SocketSever("microservice", Config.get(DefaultConfig.MSSOCKET_HOST),
                Config.getInteger(DefaultConfig.MSSOCKET_PORT), new MicroServiceServerInitializer());
        new HTTPServer(Config.getInteger(DefaultConfig.HTTP_PORT)).start();
        new SocketSever("websocket", Config.get(DefaultConfig.WEBSOCKET_HOST),
                Config.getInteger(DefaultConfig.WEBSOCKET_PORT), new WebSocketServerInitializer(), true);
    }

}
