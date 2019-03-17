package net.cryptic_game.server.http;

import static io.netty.buffer.Unpooled.copiedBuffer;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.cryptic_game.server.microservice.MicroService;
import net.cryptic_game.server.socket.SocketServerUtils;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = ctx.channel();
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;

			String payload = request.content().toString(Charset.forName("utf-8"));

			try {
				JSONObject input = (JSONObject) new JSONParser().parse(payload);

				String[] args = request.uri().substring(1).split("/");

				if (args.length > 0) {
					MicroService ms = MicroService.get(args[0]);

					if (ms != null) {
						JSONArray endpoint = new JSONArray();

						for (int i = 1; i < args.length; i++) {
							endpoint.add(args[i]);
						}

						ms.receiveHTTP(channel, endpoint, input);
						return;
					}
				}
			} catch (ParseException e) {
			}

			Map<String, String> jsonMap = new HashMap<String, String>();

			jsonMap.put("error", "unsupportet format");

			SocketServerUtils.sendJsonToHTTPClient(channel, new JSONObject(jsonMap));
		} else {
			super.channelRead(ctx, msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
				copiedBuffer(cause.getMessage().getBytes())));
	}

}
