package net.cryptic_game.server.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.*;
import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.client.ClientType;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.microservice.MicroService;
import net.cryptic_game.server.socket.SocketServerUtils;
import net.cryptic_game.server.user.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        Client client = Client.getClient(channel);

        if (client != null && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            if (request.getMethod().equals(HttpMethod.GET)) {
                Map<String, Integer> jsonMap = new HashMap<>();

                jsonMap.put("online", Client.getOnlineCount());

                SocketServerUtils.sendJsonToHTTPClient(channel, new JSONObject(jsonMap));

                return;
            }

            boolean authSuccess = false;

            if (Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
                String auth = request.headers().get("Authorization");

                if (auth == null) {
                    error(channel, "permissions denied");
                    return;
                } else if (auth.split(" ").length != 2 || !auth.split(" ")[0].equals("Basic")) {
                    error(channel, "invalid authorization");
                }

                String tuple = Base64
                        .decode(Unpooled.copiedBuffer(auth.split(" ")[1].getBytes(Charset.forName("utf-8"))))
                        .toString(Charset.forName("utf-8"));

                if (tuple.contains(":")) {
                    String name = tuple.split(":")[0];
                    String password = tuple.substring(name.length() + 1);

                    User user = User.get(name);

                    if (user != null && user.checkPassword(password)) {
                        authSuccess = true;
                        client.setUser(user);
                    }
                }
            }

            if (authSuccess || !Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
                String payload = request.content().toString(Charset.forName("utf-8"));

                try {
                    JSONObject input = (JSONObject) new JSONParser().parse(payload);

                    if (request.uri().length() > 1) {
                        String[] args = request.uri().substring(1).split("/");

                        if (args.length > 0) {
                            MicroService ms = MicroService.get(args[0]);

                            if (ms != null) {
                                JSONArray endpoint = new JSONArray();

                                endpoint.addAll(Arrays.asList(args).subList(1, args.length));

                                ms.receive(client, endpoint, input);
                                return;
                            }
                        }
                    }
                } catch (ParseException e) {
                }
            } else {
                error(channel, "permissions denied");
                return;
            }

            error(channel, "unsupported format");
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void error(Channel channel, String error) {
        Map<String, String> jsonMap = new HashMap<>();

        jsonMap.put("error", error);

        SocketServerUtils.sendJsonToHTTPClient(channel, new JSONObject(jsonMap));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Client.addClient(ctx.channel(), ClientType.HTTP);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Client.removeClient(ctx.channel());
    }

}
