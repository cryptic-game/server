package net.cryptic_game.server;

import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.database.Database;
import net.cryptic_game.server.http.HTTPServer;
import net.cryptic_game.server.microservice.MicroServiceServerInitializer;
import net.cryptic_game.server.socket.SocketSever;
import net.cryptic_game.server.websocket.WebSocketServerInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class App {

    public static void main(String[] args) {
        setLoglevel(Level.getLevel(Config.get(DefaultConfig.LOG_LEVEL)));

        new Database();

        new SocketSever("microservice", Config.get(DefaultConfig.MSSOCKET_HOST),
                Config.getInteger(DefaultConfig.MSSOCKET_PORT), new MicroServiceServerInitializer());
        new HTTPServer(Config.getInteger(DefaultConfig.HTTP_PORT)).start();
        new SocketSever("websocket", Config.get(DefaultConfig.WEBSOCKET_HOST),
                Config.getInteger(DefaultConfig.WEBSOCKET_PORT), new WebSocketServerInitializer(), true);
    }

    private static void setLoglevel(final Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final LoggerConfig loggerConfig = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

}
