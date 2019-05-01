package net.cryptic_game.server.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(final SocketChannel ch) {
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
        ch.pipeline().addLast("request", new HTTPServerHandler());
    }

}
