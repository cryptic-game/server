package net.cryptic_game.server.socket;

import static io.netty.buffer.Unpooled.copiedBuffer;

import org.json.simple.JSONObject;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class SocketServerUtils {

	public static void sendJson(Channel channel, JSONObject data) {
		channel.writeAndFlush(data.toString());
	}

	/**
	 * send string via websocket
	 * 
	 * @param channel channel of receiver
	 * @param data data to send
	 */
	public static void sendJsonToClient(Channel channel, JSONObject data) {
		channel.writeAndFlush(new TextWebSocketFrame(data.toString()));
	}

	@SuppressWarnings("deprecation")
	public static void sendJsonToHTTPClient(Channel channel, JSONObject obj) {
		final String responseMessage = obj.toString();

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				copiedBuffer(responseMessage.getBytes()));

		response.headers().set(HttpHeaders.Names.SERVER, "cryptic-game-server");
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
		
		channel.writeAndFlush(response);
	}

}
