package net.cryptic_game.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HTTPServer {

    private int port;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public HTTPServer(int port) {
        this.port = port;

        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start() {
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class).childHandler(new HTTPServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(this.port).sync();
        } catch (final InterruptedException e) {
        }
    }

}