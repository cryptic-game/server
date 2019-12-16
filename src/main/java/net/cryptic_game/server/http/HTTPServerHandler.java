package net.cryptic_game.server.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.sentry.Sentry;
import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.client.ClientType;
import net.cryptic_game.server.error.ServerError;

import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static net.cryptic_game.server.error.ServerError.UNEXPECTED_ERROR;
import static net.cryptic_game.server.socket.SocketServerUtils.sendHTTP;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, HttpEndpoint> endpoints;

    public HTTPServerHandler(final Map<String, HttpEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel channel = ctx.channel();

        if (!(msg instanceof FullHttpRequest)) {
            sendHTTP(channel, UNEXPECTED_ERROR);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        String path = request.uri();
        if (path.startsWith("/")) path = path.substring(1);

        final HttpEndpoint httpEndpoint = this.endpoints.get(path);

        if (httpEndpoint == null) {
            sendHTTP(channel, ServerError.NOT_FOUND);
        } else {
            sendHTTP(channel, httpEndpoint.handleRequest());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())));

        Sentry.capture(cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Client.addClient(ctx.channel(), ClientType.HTTP);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Client.removeClient(ctx.channel());
    }

}
