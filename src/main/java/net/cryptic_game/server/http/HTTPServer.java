package net.cryptic_game.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cryptic_game.server.http.endpoints.MicroServiceStatusEndpoint;
import net.cryptic_game.server.http.endpoints.PlayerLeaderboardEndpoint;
import net.cryptic_game.server.http.endpoints.PlayersOnlineEndpoint;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class HTTPServer {

    private static final Logger logger = Logger.getLogger(HTTPServer.class);

    private int port;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    private final Map<String, HttpEndpoint> endpoints;

    public HTTPServer(int port) {
        this.port = port;

        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();

        this.endpoints = new HashMap<>();

        this.addEndpoint(new PlayersOnlineEndpoint());
        this.addEndpoint(new MicroServiceStatusEndpoint());
        this.addEndpoint(new PlayerLeaderboardEndpoint());

    }

    private void addEndpoint(final HttpEndpoint endpoint) {
        this.endpoints.put(endpoint.getName(), endpoint);
    }

    public void start() {
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class).childHandler(new HTTPServerInitializer(this.endpoints))
                    .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(this.port).sync();
            logger.info("http booted on port " + port);
        } catch (final InterruptedException ignored) {
        }
    }

}
