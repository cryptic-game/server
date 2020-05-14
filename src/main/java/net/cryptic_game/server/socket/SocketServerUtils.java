package net.cryptic_game.server.socket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.cryptic_game.server.error.ServerError;
import org.json.simple.JSONObject;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class SocketServerUtils {

    public static void sendRaw(Channel channel, JSONObject data) {
        channel.writeAndFlush(data.toString());
    }

    public static void sendRaw(Channel channel, ServerError serverError) {
        sendRaw(channel, serverError.getResponse());
    }

    public static void sendWebsocket(Channel channel, JSONObject data) {
        channel.writeAndFlush(new TextWebSocketFrame(data.toString()));
    }

    public static void sendWebsocket(Channel channel, ServerError serverError) {
        sendWebsocket(channel, serverError.getResponse());
    }

    public static void sendHTTP(Channel channel, JSONObject data) {
        final String responseMessage = data.toString();

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                copiedBuffer(responseMessage.getBytes()));

        response.headers().set(HttpHeaderNames.SERVER, "cryptic-game-server");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseMessage.length());

        channel.writeAndFlush(response);
    }

    public static void sendHTTP(Channel channel, ServerError serverError) {
        sendHTTP(channel, serverError.getResponse());
    }

}
