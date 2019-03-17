package net.cryptic_game.server.microservice;

import java.nio.charset.StandardCharsets;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class MicroServiceServerInitializer extends ChannelInitializer<SocketChannel> {
	
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		
		pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
		pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
		pipeline.addLast(new MicroServiceHandler());
	}

}
