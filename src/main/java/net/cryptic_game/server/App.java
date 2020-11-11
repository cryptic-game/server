package net.cryptic_game.server;

import io.sentry.Sentry;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.microservice.MicroServiceServerInitializer;
import net.cryptic_game.server.server.http.HttpServer;
import net.cryptic_game.server.socket.SocketSever;
import net.cryptic_game.server.sql.SqlService;
import net.cryptic_game.server.user.Session;
import net.cryptic_game.server.user.Setting;
import net.cryptic_game.server.user.User;
import net.cryptic_game.server.websocket.WebSocketServerInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class App {

    public static void main(String[] args) {
        setLoglevel(Level.getLevel(Config.get(DefaultConfig.LOG_LEVEL)));

        if (!Config.get(DefaultConfig.SENTRY_DSN).equals("")) {
            Sentry.init(Config.get(DefaultConfig.SENTRY_DSN));
        }

        final SqlService sqlService = SqlService.getInstance();
        sqlService.addEntity(User.class);
        sqlService.addEntity(Session.class);
        sqlService.addEntity(Setting.class);
        sqlService.start();

        new SocketSever("microservice", Config.get(DefaultConfig.MSSOCKET_HOST),
                Config.getInteger(DefaultConfig.MSSOCKET_PORT), new MicroServiceServerInitializer());
        new HttpServer(Config.getInteger(DefaultConfig.HTTP_PORT)).start();
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
