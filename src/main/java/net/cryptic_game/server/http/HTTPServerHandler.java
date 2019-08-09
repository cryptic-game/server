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
import net.cryptic_game.server.user.User;
import net.cryptic_game.server.utils.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static net.cryptic_game.server.error.ServerError.*;
import static net.cryptic_game.server.socket.SocketServerUtils.sendHTTP;
import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class HTTPServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel channel = ctx.channel();

        Client client = Client.getClient(channel);
        if (client == null || !(msg instanceof FullHttpRequest)) {
            sendHTTP(channel, UNEXPECTED_ERROR);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        if (request.method().equals(HttpMethod.GET)) {
            sendHTTP(channel, simple("online", Client.getOnlineCount()));
            return;
        }

        if (Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
            String auth = request.headers().get("Authorization");

            if (auth == null) {
                sendHTTP(channel, PERMISSION_DENIED);
                return;
            }

            if (auth.split(" ").length != 2 || !auth.split(" ")[0].equals("Basic")) {
                sendHTTP(channel, INVALID_AUTHORIZATION);
                return;
            }

            String tuple = Base64
                    .decode(Unpooled.copiedBuffer(auth.split(" ")[1].getBytes(StandardCharsets.UTF_8)))
                    .toString(StandardCharsets.UTF_8);

            if (!tuple.contains(":")) {
                sendHTTP(channel, PERMISSION_DENIED);
                return;
            }

            String name = tuple.split(":")[0];
            String password = tuple.substring(name.length() + 1);

            User user = User.get(name);

            if (user == null || !user.checkPassword(password)) {
                sendHTTP(channel, PERMISSION_DENIED);
                return;
            }

            client.setUser(user);
        }

        String payload = request.content().toString(StandardCharsets.UTF_8);

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(payload);
        } catch (ParseException | ClassCastException ignored) {
            sendHTTP(channel, UNSUPPORTED_FORMAT);
            return;
        }

        JSON json = new JSON(obj);

        JSONObject data = json.get("data");
        if (data == null) {
            sendHTTP(channel, MISSING_PARAMETERS);
            return;
        }

        UUID tag;
        try {
            tag = UUID.fromString(json.get("tag"));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            sendHTTP(channel, MISSING_PARAMETERS);
            return;
        }

        if (request.uri().length() <= 1) {
            sendHTTP(channel, UNSUPPORTED_FORMAT);
            return;
        }

        String[] args = request.uri().substring(1).split("/");

        if (args.length == 0) {
            sendHTTP(channel, UNSUPPORTED_FORMAT);
            return;
        }

        MicroService ms = MicroService.get(args[0]);
        if (ms == null) {
            sendHTTP(channel, UNSUPPORTED_FORMAT);
            return;
        }

        JSONArray endpoint = new JSONArray();

        endpoint.addAll(Arrays.asList(args).subList(1, args.length));

        ms.receive(client, endpoint, data, tag);
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
