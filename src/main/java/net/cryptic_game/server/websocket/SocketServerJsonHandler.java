package net.cryptic_game.server.websocket;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public abstract class SocketServerJsonHandler<E> extends SimpleChannelInboundHandler<E> {
	
	protected List<Channel> online = new ArrayList<Channel>();

	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
		try {
			JSONObject obj = (JSONObject) new JSONParser().parse(frame.text());
			
			this.handle(ctx, obj);
		} catch (ParseException e) {
			this.error(ctx, "unsupported format");
			ctx.channel().close();
		}
	}
	
	public int getOnlineCount() {
		return this.online.size();
	}
	
	public abstract void handle(ChannelHandlerContext ctx, JSONObject obj);
	
	protected abstract void error(ChannelHandlerContext ctx, String error);
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		online.add(ctx.channel());
		System.out.println(this.getOnlineCount());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		online.remove(ctx.channel());
	}

}
