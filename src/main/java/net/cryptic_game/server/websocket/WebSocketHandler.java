package net.cryptic_game.server.websocket;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.client.ClientType;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.microservice.MicroService;
import net.cryptic_game.server.socket.SocketServerUtils;
import net.cryptic_game.server.user.User;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
		Channel channel = ctx.channel();
		Client client = Client.getClient(channel);
		try {
			JSONObject obj = (JSONObject) new JSONParser().parse(frame.text());
			if (!Config.getBoolean(DefaultConfig.AUTH_ENABLED) || client.isValid()) {
				if (obj.containsKey("ms") && obj.get("ms") instanceof String && obj.containsKey("data")
						&& obj.get("data") instanceof JSONObject && obj.containsKey("endpoint")
						&& obj.get("endpoint") instanceof JSONArray) {
					try {
						MicroService ms = MicroService.get((String) obj.get("ms"));

						if (ms != null) {
							ms.receive(client, (JSONArray) obj.get("endpoint"), (JSONObject) obj.get("data"));
						}
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
				} else {
					this.error(channel, "invalid data");
				}
				return;
			} else if (obj.containsKey("action") && obj.get("action") instanceof String) {
				String action = (String) obj.get("action");

				if (action.equals("status")) {
					Map<String, Object> status = new HashMap<String, Object>();

					status.put("online", Client.getOnlineCount());

					this.respond(channel, status);
				} else if (obj.containsKey("name") && obj.get("name") instanceof String && obj.containsKey("password")
						&& obj.get("password") instanceof String) {
					String name = (String) obj.get("name");
					String password = (String) obj.get("password");
					if (action.equals("login")) {
						User user = User.get(name);

						if (user != null && user.checkPassword(password)) {
							client.setUser(user);
							Map<String, Boolean> jsonMap = new HashMap<String, Boolean>();

							jsonMap.put("result", true);

							respond(channel, new JSONObject(jsonMap));
							return;
						}
					} else if (action.equals("register")) {
						User user = User.create(name, password);

						if (user != null) {
							client.setUser(user);

							Map<String, Boolean> jsonMap = new HashMap<String, Boolean>();

							jsonMap.put("result", true);

							respond(channel, new JSONObject(jsonMap));
							return;
						} else {
							error(channel, "username already exists");
							return;
						}
					}
				}
				this.error(channel, "unknown action");
				return;
			}
			this.error(channel, "permissions denied");
		} catch (ParseException e) {
			this.error(ctx.channel(), "unsupported format");
			ctx.channel().close();
		}
	}

	private void error(Channel channel, String error) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		jsonMap.put("error", error);

		this.respond(channel, jsonMap);
	}

	private void respond(Channel channel, Map<String, Object> data) {
		SocketServerUtils.sendJsonToClient(channel, new JSONObject(data));
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Client.addClient(ctx.channel(), ClientType.WEBSOCKET);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Client.removeClient(ctx.channel());
	}

}