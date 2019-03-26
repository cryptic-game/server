package net.cryptic_game.server.microservice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.cryptic_game.server.socket.SocketServerUtils;

public class MicroServiceHandler extends SimpleChannelInboundHandler<String> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		try {
			JSONObject obj = (JSONObject) new JSONParser().parse(msg);

			MicroService ms = MicroService.get(ctx.channel());

			if (ms == null || !ms.send(obj)) {
				if (obj.containsKey("action") && obj.get("action") instanceof String) {
					String action = (String) obj.get("action");

					if (action.equals("register")) {
						if (obj.containsKey("name") && obj.get("name") instanceof String) {
							MicroService.register((String) obj.get("name"), ctx.channel());
						} else {
							this.error(ctx.channel(), "name not found");
						}
					} else if (action.equals("address")) {
						if (obj.containsKey("user") && obj.get("user") instanceof String && obj.containsKey("data")
								&& obj.get("data") instanceof JSONObject) {
							MicroService.sendToUser(UUID.fromString((String) obj.get("user")),
									(JSONObject) obj.get("data"));
						} else {
							this.error(ctx.channel(), "name not found");
						}
					} else {
						this.error(ctx.channel(), "unknown action");
					}
				}
			}
		} catch (ParseException e) {
			this.error(ctx.channel(), "unsupported format");
			ctx.channel().close();
		}
	}

	/**
	 * send error to microservice
	 * 
	 * @param channel channel of receiver
	 * @param error
	 */
	private void error(Channel channel, String error) {
		Map<String, String> jsonMap = new HashMap<String, String>();

		jsonMap.put("error", error);

		SocketServerUtils.sendJson(channel, new JSONObject(jsonMap));
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		MicroService.unregister(ctx.channel());
	}

}
