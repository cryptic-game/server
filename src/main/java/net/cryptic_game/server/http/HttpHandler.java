package net.cryptic_game.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

public abstract class HttpHandler<T> extends SimpleChannelInboundHandler<T> {

    private static final Logger logger = Logger.getLogger(HttpServer.class);

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ctx.close();
        logger.error("Failed to progress channel. \"" + ctx.channel() + "\"", cause);
    }
}
