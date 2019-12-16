package net.cryptic_game.server.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.Map;

public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {

    private final Map<String, HttpEndpoint> endpoints;

    public HTTPServerInitializer(final Map<String, HttpEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public void initChannel(final SocketChannel ch) {
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
        ch.pipeline().addLast("request", new HTTPServerHandler(this.endpoints));
    }
}
