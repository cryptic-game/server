package net.cryptic_game.server.microservice;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.cryptic_game.server.socket.SocketServerUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MicroServiceHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        JSONObject obj;

        try {
            obj = (JSONObject) new JSONParser().parse(msg);
        } catch (ParseException e) {
            e.printStackTrace();
            this.error(ctx.channel(), "unsupported format");
            return;
        }

        MicroService ms = MicroService.get(ctx.channel());

        if (ms == null || !ms.send(obj)) {
            if (!obj.containsKey("action") || !(obj.get("action") instanceof String)) {
                this.error(ctx.channel(), "missing action");
                return;
            }

            String action = (String) obj.get("action");

            switch (action) {
                case "register":
                    if (obj.containsKey("name") && obj.get("name") instanceof String) {
                        MicroService.register((String) obj.get("name"), ctx.channel());
                    } else {
                        this.error(ctx.channel(), "missing parameter");
                    }
                    break;
                case "address":
                    if (obj.containsKey("user") && obj.get("user") instanceof String && obj.containsKey("data")
                            && obj.get("data") instanceof JSONObject) {
                        MicroService.sendToUser(UUID.fromString((String) obj.get("user")), (JSONObject) obj.get("data"));
                    } else {
                        this.error(ctx.channel(), "missing parameter");
                    }
                    break;
                default:
                    this.error(ctx.channel(), "unknown action");
                    break;
            }
        }
    }

    /**
     * Sends an error to a microservice
     *
     * @param channel channel of receiver
     * @param error   the error message
     */
    private void error(Channel channel, String error) {
        Map<String, String> jsonMap = new HashMap<>();

        jsonMap.put("error", error);

        SocketServerUtils.sendJson(channel, new JSONObject(jsonMap));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        MicroService.unregister(ctx.channel());
    }

}
