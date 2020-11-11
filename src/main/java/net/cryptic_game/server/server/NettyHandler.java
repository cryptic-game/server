package net.cryptic_game.server.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyHandler<T> extends SimpleChannelInboundHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);

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
