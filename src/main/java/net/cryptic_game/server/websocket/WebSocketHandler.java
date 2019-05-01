package net.cryptic_game.server.websocket;

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
import net.cryptic_game.server.user.Session;
import net.cryptic_game.server.user.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        Channel channel = ctx.channel();
        Client client = Client.getClient(channel);
        if (client == null) {
            this.error(channel, "unexpected error");
            throw new IllegalStateException("Unexpected error: no client was found for channel");
        }

        JSONObject obj;

        try {
            obj = (JSONObject) new JSONParser().parse(frame.text());
        } catch (ParseException | ClassCastException e) {
            this.error(ctx.channel(), "unsupported format");
            return;
        }

        // Logged in actions

        if (client.isValid() || !Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
            if (obj.containsKey("ms") && obj.get("ms") instanceof String && obj.containsKey("data")  // microservice
                    && obj.get("data") instanceof JSONObject && obj.containsKey("endpoint")
                    && obj.get("endpoint") instanceof JSONArray) {
                MicroService ms = MicroService.get((String) obj.get("ms"));

                if (ms != null) {
                    ms.receive(client, (JSONArray) obj.get("endpoint"), (JSONObject) obj.get("data"));
                }
            } else {
                if (!obj.containsKey("action") || !(obj.get("action") instanceof String)) {
                    this.error(channel, "missing action");
                    return;
                }

                switch ((String) obj.get("action")) {
                    case "info": {
                        Map<String, Object> jsonMap = new HashMap<>();
                        jsonMap.put("name", client.getUser().getName());
                        jsonMap.put("mail", client.getUser().getMail());
                        jsonMap.put("created", client.getUser().getCreated().getTime());
                        jsonMap.put("last", client.getUser().getLast().getTime());
                        jsonMap.put("online", Client.getOnlineCount());

                        this.respond(channel, jsonMap);

                        break;
                    }
                    case "logout": {
                        // TODO
                    }
                    default: {
                        this.error(channel, "invalid action");
                        break;
                    }
                }
            }
            return;
        }

        // Not logged in actions

        if (!obj.containsKey("action") || !(obj.get("action") instanceof String)) {
            this.error(channel, "missing action");
            return;
        }

        switch ((String) obj.get("action")) {
            case "status":
                Map<String, Object> status = new HashMap<>();

                status.put("online", Client.getOnlineCount());

                this.respond(channel, status);

                break;
            case "session":
                if (!obj.containsKey("token") || !(obj.get("token") instanceof String)) {
                    this.error(channel, "missing parameters");
                    return;
                }

                Session session = Session.get(UUID.fromString((String) obj.get("token")));

                if (session != null && session.isValid()) {
                    client.setUser(session.getUser());

                    Map<String, Object> jsonMap = new HashMap<>();

                    jsonMap.put("token", session.getToken().toString());

                    this.respond(channel, jsonMap);
                } else {
                    this.error(channel, "invalid token");
                }

                break;
            case "login": {
                if (!(obj.containsKey("name") && obj.get("name") instanceof String && obj.containsKey("password")
                        && obj.get("password") instanceof String)) {
                    this.error(channel, "missing parameters");
                    return;
                }

                User user = User.get((String) obj.get("name"));
                if (user == null || !user.checkPassword((String) obj.get("password"))) {
                    this.error(channel, "permission denied");
                    return;
                }

                login(channel, client, user);

                break;
            }
            case "register": {
                if (!obj.containsKey("name") || !(obj.get("name") instanceof String) || !obj.containsKey("password")
                        || !(obj.get("password") instanceof String) || !obj.containsKey("mail")
                        || !(obj.get("mail") instanceof String)) {
                    this.error(channel, "missing parameters");
                    return;
                }

                String name = (String) obj.get("name");
                String password = (String) obj.get("password");
                String mail = (String) obj.get("mail");

                if (!User.isValidPassword(password)) {
                    this.error(channel, "password invalid (condition: minimum 8 chars, one digit)");
                    return;
                }

                if (!User.isValidMailAddress(mail)) {
                    this.error(channel, "email invalid");
                    return;
                }

                User user = User.create(name, mail, password);
                if (user == null) {
                    this.error(channel, "username already exists");
                    return;
                }

                login(channel, client, user);

                break;
            }
            case "password": {
                if (!obj.containsKey("name") || !(obj.get("name") instanceof String) || !obj.containsKey("password")
                        || !(obj.get("password") instanceof String) || !obj.containsKey("new")
                        || !(obj.get("new") instanceof String)) {
                    this.error(channel, "missing parameters");
                    return;
                }

                User user = User.get((String) obj.get("name"));

                if (user != null && user.changePassword((String) obj.get("password"), (String) obj.get("new"))) {
                    Map<String, Object> jsonMap = new HashMap<>();

                    jsonMap.put("success", true);

                    this.respond(channel, jsonMap);
                } else {
                    this.error(channel, "permission denied");
                }

                break;
            }
            default:
                this.error(channel, "unknown action");
                break;
        }

    }

    private void login(Channel channel, Client client, User user) {
        Session session = Session.create(user);

        client.setUser(user);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("token", session.getToken().toString());

        this.respond(channel, jsonMap);
    }

    private void error(Channel channel, String error) {
        Map<String, Object> jsonMap = new HashMap<>();

        jsonMap.put("error", error);

        this.respond(channel, jsonMap);
    }

    private void respond(Channel channel, Map<String, Object> data) {
        SocketServerUtils.sendJsonToClient(channel, new JSONObject(data));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Client.addClient(ctx.channel(), ClientType.WEBSOCKET);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Client.removeClient(ctx.channel());
    }

}
