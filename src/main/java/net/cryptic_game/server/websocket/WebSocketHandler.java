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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @SuppressWarnings({"unchecked"})
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        Channel channel = ctx.channel();
        Client client = Client.getClient(channel);
        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(frame.text());
            if (!Config.getBoolean(DefaultConfig.AUTH_ENABLED) || client.isValid()) {
                if (obj.containsKey("action") && obj.get("action") instanceof String
                        && obj.get("action").equals("info")) {
                    Map<String, Object> jsonMap = new HashMap<String, Object>();

                    jsonMap.put("name", client.getUser().getName());
                    jsonMap.put("mail", client.getUser().getMail());
                    jsonMap.put("created", client.getUser().getCreated().getTime());
                    jsonMap.put("last", client.getUser().getLast().getTime());
                    jsonMap.put("online", Client.getOnlineCount());

                    this.respond(channel, jsonMap);
                } else if (obj.containsKey("ms") && obj.get("ms") instanceof String && obj.containsKey("data")
                        && obj.get("data") instanceof JSONObject && obj.containsKey("endpoint")
                        && obj.get("endpoint") instanceof JSONArray) {
                    try {
                        MicroService ms = MicroService.get((String) obj.get("ms"));

                        if (ms != null) {
                            ms.receive(client, (JSONArray) obj.get("endpoint"), (JSONObject) obj.get("data"));
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.error(channel, "invalid data");
                }
                return;
            } else if (obj.containsKey("action") && obj.get("action") instanceof String) {
                String action = (String) obj.get("action");

                if (action.equals("status")) {
                    Map<String, Object> status = new HashMap<String, Object>();

                    status.put("online", Client.getOnlineCount());

                    this.respond(channel, status);
                    return;
                } else if (action.equals("session") && obj.containsKey("token") && obj.get("token") instanceof String) {
                    Session session = Session.get(UUID.fromString((String) obj.get("token")));

                    if (session != null && session.isValid()) {
                        client.setUser(session.getUser());

                        Map<String, String> jsonMap = new HashMap<String, String>();

                        jsonMap.put("token", session.getToken().toString());

                        respond(channel, new JSONObject(jsonMap));
                    } else {
                        error(channel, "permissions denied");
                    }
                    return;
                } else if (obj.containsKey("name") && obj.get("name") instanceof String && obj.containsKey("password")
                        && obj.get("password") instanceof String) {
                    String name = (String) obj.get("name");
                    String password = (String) obj.get("password");
                    if (action.equals("login")) {
                        User user = User.get(name);

                        if (user != null && user.checkPassword(password)) {
                            Session session = Session.create(user);

                            if (session != null) {
                                client.setUser(user);

                                Map<String, String> jsonMap = new HashMap<String, String>();

                                jsonMap.put("token", session.getToken().toString());

                                respond(channel, new JSONObject(jsonMap));
                                return;
                            }
                        } else {
                            this.error(channel, "permissions denied");
                        }
                        return;
                    } else if (action.equals("register") && obj.containsKey("mail")
                            && obj.get("mail") instanceof String) {
                        String mail = (String) obj.get("mail");

                        if (User.isValidPassword(password)) {
                            if (User.isValidMailAddress(mail)) {
                                User user = User.create(name, mail, password);

                                if (user != null) {
                                    Session session = Session.create(user);

                                    if (session != null) {
                                        client.setUser(user);

                                        Map<String, String> jsonMap = new HashMap<String, String>();

                                        jsonMap.put("token", session.getToken().toString());

                                        respond(channel, new JSONObject(jsonMap));
                                        return;
                                    }
                                } else {
                                    error(channel, "username already exists");
                                    return;
                                }
                            } else {
                                error(channel, "no valid mail");
                                return;
                            }
                        } else {
                            error(channel, "no valid password (condition: minimum 8 chars, one digit, one digit)");
                            return;
                        }
                    } else if (action.equals("password") && obj.containsKey("new")
                            && obj.get("new") instanceof String) {
                        User user = User.get(name);

                        if (user.changePassword(password, (String) obj.get("new"))) {
                            Map<String, Boolean> jsonMap = new HashMap<String, Boolean>();

                            jsonMap.put("result", true);

                            this.respond(channel, new JSONObject(jsonMap));
                        } else {
                            this.error(channel, "permissions denied");
                        }
                    }
                }
                this.error(channel, "unknown action");
                return;
            }
            this.error(channel, "permissions denied");
        } catch (Exception e) {
            this.error(ctx.channel(), "unsupported format");
            ctx.channel().close();
        }
    }

    private void error(Channel channel, String error) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();

        jsonMap.put("error", error);

        this.respond(channel, jsonMap);
    }

    private void respond(Channel channel, Map<String, Object> data) {
        SocketServerUtils.sendJsonToClient(channel, new JSONObject(data));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Client.addClient(ctx.channel(), ClientType.WEBSOCKET);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Client.removeClient(ctx.channel());
    }

}
