package net.cryptic_game.server.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cryptic_game.server.server.http.endpoints.MicroServiceStatusEndpoint;
import net.cryptic_game.server.server.http.endpoints.PlayerLeaderboardEndpoint;
import net.cryptic_game.server.server.http.endpoints.PlayersOnlineEndpoint;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private static final Logger logger = Logger.getLogger(HttpServer.class);

    private final int port;
    private final Map<String, HttpEndpoint> endpoints;

    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public HttpServer(final int port) {
        this.port = port;
        this.endpoints = new HashMap<>();

        this.masterGroup = new NioEventLoopGroup();
        this.slaveGroup = new NioEventLoopGroup();

        this.registerEndpoints();
    }

    private void registerEndpoints() {
        this.addEndpoint(new PlayersOnlineEndpoint());
        this.addEndpoint(new MicroServiceStatusEndpoint());
        this.addEndpoint(new PlayerLeaderboardEndpoint());
    }

    private void addEndpoint(final HttpEndpoint endpoint) {
        this.endpoints.put(endpoint.getName().toLowerCase(), endpoint);
    }

    public void start() {
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer(this.endpoints))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.bind(this.port).sync();
            logger.info("HttpSever is listening on port " + this.port + ".");
        } catch (final InterruptedException ignored) {
        }
    }
}
