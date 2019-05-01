package net.cryptic_game.server.socket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class SocketServerUtils {

    public static void sendJson(Channel channel, JSONObject data) {
        channel.writeAndFlush(data.toString());
    }

    /**
     * Sends string via websocket
     *
     * @param channel channel of receiver
     * @param data    data to send
     */
    public static void sendJsonToClient(Channel channel, JSONObject data) {
        channel.writeAndFlush(new TextWebSocketFrame(data.toString()));
    }

    public static void sendJsonToHTTPClient(Channel channel, JSONObject obj) {
        final String responseMessage = obj.toString();

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                copiedBuffer(responseMessage.getBytes()));

        response.headers().set(HttpHeaderNames.SERVER, "cryptic-game-server");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseMessage.length());

        channel.writeAndFlush(response);
    }

}
