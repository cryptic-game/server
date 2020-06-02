package net.cryptic_game.server.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class SocketSever {

    private static final boolean EPOLL = Epoll.isAvailable();
    private static final Logger logger = LoggerFactory.getLogger(SocketSever.class);

    private int port;
    private String name;

    public SocketSever(String name, String host, int port, ChannelInitializer<SocketChannel> initializer, boolean mainThread) {
        this.name = name;
        this.port = port;

        EventLoopGroup group = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        try {
            ChannelFuture f = new ServerBootstrap().group(group)
                    .channel(EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(initializer).localAddress(new InetSocketAddress(host, port)).bind();

            logger.info(this.getName() + " booted on port " + port);

            if (mainThread) {
                f.channel().closeFuture().sync();
            }
        } catch (Exception e) {
            logger.error("Cannot bind port to " + port);
        }
    }

    public SocketSever(String name, String host, int port, ChannelInitializer<SocketChannel> initializer) {
        this(name, host, port, initializer, false);
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

}
