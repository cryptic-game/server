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
import net.cryptic_game.server.microservice.MicroService;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static net.cryptic_game.server.error.ServerError.UNEXPECTED_ERROR;
import static net.cryptic_game.server.socket.SocketServerUtils.sendHTTP;
import static net.cryptic_game.server.utils.JSONBuilder.anJSON;
import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel channel = ctx.channel();

        if (!(msg instanceof FullHttpRequest)) {
            sendHTTP(channel, UNEXPECTED_ERROR);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        switch (request.uri().replace("/","")) {
            case "":
                sendHTTP(channel, anJSON()
                        .add("online", "/online")
                        .add("microservice-status", "/status")
                        .build());
            case "online":
                sendHTTP(channel, simple("online", Client.getOnlineCount()));
                break;
            case "status":
                final List<JSONObject> jsonObjects = new ArrayList<>();
                MicroService.getOnlineMicroServices().forEach(ms -> {
                    jsonObjects.add(anJSON()
                            .add("name", ms.getName())
                            .build());
                });
                sendHTTP(channel, simple("status", jsonObjects));
                break;
            default:
                sendHTTP(channel, ServerError.NOT_FOUND);
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
