package net.cryptic_game.server.socket;

import org.json.simple.JSONObject;

import io.netty.channel.Channel;
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

}
