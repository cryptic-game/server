package net.cryptic_game.server.socket;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

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

public class SocketSever {

	private static final boolean EPOLL = Epoll.isAvailable();
	private static final Logger logger = Logger.getLogger(SocketSever.class);

	private int port;
	private String name;

	@SuppressWarnings("deprecation")
	public SocketSever(String name, String host, int port, ChannelInitializer<SocketChannel> initializer, boolean mainThread) {
		this.name = name;
		this.port = port;

		EventLoopGroup group = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();

		ChannelFuture f = null;
		
		try {
			f = new ServerBootstrap().group(group)
					.channel(EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
					.childHandler(initializer).localAddress(new InetSocketAddress(host, port)).bind().sync();
			
			logger.log(Priority.INFO, this.getName() + " bootet on port " + port);
			
			if(mainThread) {
				f.channel().closeFuture().sync();
			}
		} catch (Exception e) {
			logger.error("cannot bind port " + port);
		}
	}
	
	public SocketSever(String name, String host, int port, ChannelInitializer<SocketChannel> initializer) {
		this(name, host, port, initializer, false);
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

}