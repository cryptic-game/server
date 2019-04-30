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
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = Logger.getLogger(WebSocketHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        Channel channel = ctx.channel();
        Client client = Client.getClient(channel);
        if (client == null) {
            logger.error("Unexpected error: no client was found for channel");
            this.error(channel, "unexpected error");
            ctx.channel().close();
            return;
        }

        try {
            Object parseResult = new JSONParser().parse(frame.text());
            if (!(parseResult instanceof JSONObject)) {
                this.error(ctx.channel(), "unsupported format");
                return;
            }
            JSONObject obj = (JSONObject) parseResult;

            if (client.isValid() || !Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
                if (obj.containsKey("action") && obj.get("action").equals("info")) {  // info action
                    Map<String, Object> jsonMap = new HashMap<>();

                    jsonMap.put("name", client.getUser().getName());
                    jsonMap.put("mail", client.getUser().getMail());
                    jsonMap.put("created", client.getUser().getCreated().getTime());
                    jsonMap.put("last", client.getUser().getLast().getTime());
                    jsonMap.put("online", Client.getOnlineCount());

                    this.respond(channel, jsonMap);
                } else if (obj.containsKey("ms") && obj.get("ms") instanceof String && obj.containsKey("data")  // microservice
                        && obj.get("data") instanceof JSONObject && obj.containsKey("endpoint")
                        && obj.get("endpoint") instanceof JSONArray) {
                    MicroService ms = MicroService.get((String) obj.get("ms"));

                    if (ms != null) {
                        ms.receive(client, (JSONArray) obj.get("endpoint"), (JSONObject) obj.get("data"));
                    }
                } else {
                    this.error(channel, "invalid action");
                }
                return;
            }


            if (obj.containsKey("action") && obj.get("action") instanceof String) {
                String action = (String) obj.get("action");

                if (action.equals("status")) {
                    Map<String, Object> status = new HashMap<>();

                    status.put("online", Client.getOnlineCount());

                    this.respond(channel, status);

                } else if (action.equals("session") && obj.containsKey("token") && obj.get("token") instanceof String) {
                    Session session = Session.get(UUID.fromString((String) obj.get("token")));

                    if (session != null && session.isValid()) {
                        client.setUser(session.getUser());

                        Map<String, Object> jsonMap = new HashMap<>();

                        jsonMap.put("token", session.getToken().toString());

                        this.respond(channel, jsonMap);
                    } else {
                        this.error(channel, "invalid token");
                    }

                } else if (obj.containsKey("name") && obj.get("name") instanceof String && obj.containsKey("password")
                        && obj.get("password") instanceof String) {
                    String name = (String) obj.get("name");
                    String password = (String) obj.get("password");

                    if (action.equals("login")) {
                        User user = User.get(name);
                        if (user == null || !user.checkPassword(password)) {
                            this.error(channel, "permission denied");
                            return;
                        }

                        login(channel, client, user);

                    } else if (action.equals("register") && obj.containsKey("mail")
                            && obj.get("mail") instanceof String) {
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

                    } else if (action.equals("password") && obj.containsKey("new")
                            && obj.get("new") instanceof String) {
                        User user = User.get(name);

                        if (user != null && user.changePassword(password, (String) obj.get("new"))) {
                            Map<String, Object> jsonMap = new HashMap<>();

                            jsonMap.put("result", true);

                            this.respond(channel, jsonMap);
                        } else {
                            this.error(channel, "permission denied");
                        }
                    } else {
                        this.error(channel, "missing action");
                    }

                } else {
                    this.error(channel, "unknown action");
                }
            } else {
                this.error(channel, "missing action");
            }
        } catch (ParseException e) {
            this.error(ctx.channel(), "unsupported format");
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
