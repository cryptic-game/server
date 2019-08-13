package net.cryptic_game.server.microservice;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.cryptic_game.server.utils.JSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

import static net.cryptic_game.server.error.ServerError.*;
import static net.cryptic_game.server.socket.SocketServerUtils.sendWebsocket;

public class MicroServiceHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(msg);
        } catch (ParseException ignored) {
            sendWebsocket(channel, UNSUPPORTED_FORMAT);
            return;
        }

        JSON json = new JSON(obj);

        MicroService ms = MicroService.get(channel);

        if (ms != null && ms.send(obj)) {
            return;
        }

        String action = json.get("action");

        if (action == null) {
            sendWebsocket(channel, MISSING_ACTION);
            return;
        }

        switch (action) {
            case "register": {
                String name = json.get("name");

                if (name == null) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                MicroService.register(name, channel);

                break;
            }
            case "address": {
                UUID user;
                try {
                    user = UUID.fromString(json.get("user"));
                } catch (IllegalArgumentException | NullPointerException e) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                JSONObject data = json.get("data", JSONObject.class);
                if (data == null) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                MicroService.sendToUser(user, data);

                break;
            }
            default: {
                sendWebsocket(channel, UNKNOWN_ACTION);

                break;
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        MicroService.unregister(ctx.channel());
    }

}
