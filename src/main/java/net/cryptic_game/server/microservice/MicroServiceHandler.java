package net.cryptic_game.server.microservice;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.cryptic_game.server.user.User;
import net.cryptic_game.server.utils.JSON;
import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

import static net.cryptic_game.server.error.ServerError.*;
import static net.cryptic_game.server.socket.SocketServerUtils.sendRaw;

public class MicroServiceHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(msg);
        } catch (ParseException ignored) {
            sendRaw(channel, UNSUPPORTED_FORMAT);
            return;
        }

        JSON json = new JSON(obj);

        MicroService ms = MicroService.get(channel);

        String action = json.get("action");

        if (ms != null && ms.send(obj) && action == null) {
            return;
        }

        if (action == null) {
            sendRaw(channel, MISSING_ACTION);
            return;
        }

        switch (action) {
            case "register": {
                String name = json.get("name");

                if (name == null) {
                    sendRaw(channel, MISSING_PARAMETERS);
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
                    sendRaw(channel, MISSING_PARAMETERS);
                    return;
                }

                JSONObject data = json.get("data", JSONObject.class);
                if (data == null) {
                    sendRaw(channel, MISSING_PARAMETERS);
                    return;
                }

                MicroService.sendToUser(user, data);

                break;
            }
            case "user": {
                UUID tag = json.getUUID("tag");
                JSONObject dataJSONObject = json.get("data", JSONObject.class);
                JSON data = new JSON(dataJSONObject);

                if (tag == null || dataJSONObject == null || data.get("user") == null) {
                    sendRaw(channel, MISSING_PARAMETERS);
                    return;
                }

                User user = User.get(data.getUUID("user"));

                JSONBuilder result = JSONBuilder.anJSON()
                        .add("tag", tag.toString());

                JSONBuilder resultData = JSONBuilder.anJSON()
                        .add("valid", user != null);

                if (user != null) {
                    resultData.add("uuid", user.getUUID().toString())
                            .add("name", user.getName())
                            .add("created", user.getCreated().getTime())
                            .add("last", user.getLast().getTime()).build();
                }
                result.add("data", resultData.build());

                sendRaw(channel, result.build());

                break;
            }
            default: {
                sendRaw(channel, UNKNOWN_ACTION);

                break;
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        MicroService.unregister(ctx.channel());
    }

}
