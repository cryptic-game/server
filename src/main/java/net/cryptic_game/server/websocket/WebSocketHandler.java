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
import net.cryptic_game.server.user.Setting;
import net.cryptic_game.server.user.User;
import net.cryptic_game.server.utils.JSON;
import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;
import java.util.UUID;

import static net.cryptic_game.server.error.ServerError.*;
import static net.cryptic_game.server.socket.SocketServerUtils.sendRaw;
import static net.cryptic_game.server.socket.SocketServerUtils.sendWebsocket;
import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        Channel channel = ctx.channel();
        Client client = Client.getClient(channel);
        if (client == null) {
            sendWebsocket(channel, UNEXPECTED_ERROR);
            return;
        }

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(frame.text());
        } catch (ParseException | ClassCastException e) {
            sendWebsocket(channel, UNSUPPORTED_FORMAT);
            return;
        }

        JSON json = new JSON(obj);

        if (client.isValid() || !Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
            MicroService microService = MicroService.get(json.get("ms"));
            UUID tag = json.getUUID("tag");
            JSONObject data = json.get("data", JSONObject.class);
            JSONArray endpoint = json.get("endpoint", JSONArray.class);

            if (data == null || endpoint == null || microService == null || tag == null) {
                String action = json.get("action");
                if (action == null) {
                    sendWebsocket(channel, MISSING_ACTION);
                    return;
                }

                switch (action) {
                    case "info": {
                        User user = client.getUser();

                        JSONBuilder jsonBuilder = JSONBuilder.anJSON()
                                .add("name", user.getName())
                                .add("uuid", user.getUUID().toString())
                                .add("created", user.getCreated().getTime())
                                .add("last", user.getLast().getTime())
                                .add("online", Client.getOnlineCount());

                        SocketServerUtils.sendWebsocket(channel, jsonBuilder.build());

                        break;
                    }
                    case "delete": {
                        MicroService.getOnlineMicroServices().forEach((microServices -> {
                            JSONBuilder jsonBuilder = JSONBuilder.anJSON()
                                    .add("ms", "server")
                                    .add("endpoint", Arrays.asList("delete_user"))
                                    .add("tag", UUID.randomUUID().toString())
                                    .add("data", JSONBuilder.anJSON()
                                            .add("user_uuid", client.getUser().getUUID().toString()).build());
                            sendRaw(microServices.getChannel(), jsonBuilder.build());
                        }));

                        Setting.getSettingsOfUser(client.getUser()).forEach(Setting::delete);
                        Session.getSessionsOfUser(client.getUser()).forEach(Session::delete);
                        client.getUser().delete();
                    }
                    case "logout": {
                        Client.logout(channel);

                        JSONBuilder jsonBuilder = JSONBuilder.anJSON().add("status", "logout");
                        sendWebsocket(channel, jsonBuilder.build());
                        break;
                    }
                    case "setting": {
                        String key = json.get("key");
                        String value = json.get("value");

                        if (key == null) {
                            sendWebsocket(channel, MISSING_PARAMETERS);
                            return;
                        }

                        if (value == null) {
                            Setting setting = Setting.getSetting(client.getUser(), key);

                            if (setting == null) {
                                sendWebsocket(channel, UNKNOWN_SETTING);
                                return;
                            }

                            if (json.get("delete") != null) {
                                setting.delete();
                                JSONBuilder jsonBuilder = JSONBuilder.anJSON().add("success", true);
                                sendWebsocket(channel, jsonBuilder.build());
                                return;
                            }

                            JSONBuilder jsonBuilder = JSONBuilder.anJSON().add("key", key).add("value", setting.getValue());
                            sendWebsocket(channel, jsonBuilder.build());
                            return;
                        }

                        if (key.length() > 50 || value.length() > 255) {
                            sendWebsocket(channel, UNSUPPORTED_PARAMETER_SIZE);
                            return;
                        }

                        if (Setting.getSetting(client.getUser(), key) != null) {
                            Setting setting = Setting.getSetting(client.getUser(), key);
                            setting.updateValue(value);

                            JSONBuilder jsonBuilder = JSONBuilder.anJSON().add("key", key).add("value", setting.getValue());
                            sendWebsocket(channel, jsonBuilder.build());
                            return;
                        }

                        Setting setting = Setting.createSetting(client.getUser(), key, value);

                        JSONBuilder jsonBuilder = JSONBuilder.anJSON().add("key", key).add("value", setting.getValue());
                        sendWebsocket(channel, jsonBuilder.build());
                        return;
                    }
                    default: {
                        sendWebsocket(channel, UNKNOWN_ACTION);
                        break;
                    }
                }

                return;
            }

            microService.receive(client, endpoint, data, tag);

            return;
        }

        String action = json.get("action");
        if (action == null) {
            sendWebsocket(channel, MISSING_ACTION);
            return;
        }

        switch (action) {
            case "status": {
                SocketServerUtils.sendWebsocket(channel, simple("online", Client.getOnlineCount()));

                break;
            }
            case "session": {
                UUID token;
                try {
                    token = UUID.fromString(json.get("token"));
                } catch (IllegalArgumentException | NullPointerException ex) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                Session session = Session.get(token);

                if (session != null && session.isValid()) {
                    client.setUser(session.getUser());
                    client.setSession(session);

                    SocketServerUtils.sendWebsocket(channel, simple("token", session.getToken().toString()));
                } else {
                    sendWebsocket(channel, INVALID_TOKEN);
                }

                break;
            }
            case "login": {
                String name = json.get("name");
                String password = json.get("password");

                if (name == null || password == null) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                User user = User.get(name);

                if (user == null || !user.checkPassword(password)) {
                    sendWebsocket(channel, PERMISSION_DENIED);
                    return;
                }

                login(channel, client, user);

                break;
            }
            case "register": {
                String name = json.get("name");
                String password = json.get("password");

                if (name == null || password == null) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                if (!User.isValidPassword(password)) {
                    sendWebsocket(channel, INVALID_PASSWORD);
                    return;
                }

                User user = User.create(name, password);
                if (user == null) {
                    sendWebsocket(channel, USERNAME_ALREADY_EXISTS);
                    return;
                }

                login(channel, client, user);

                break;
            }
            case "password": {
                String name = json.get("name");
                String password = json.get("password");
                String newPassword = json.get("new");

                if (name == null || password == null || newPassword == null) {
                    sendWebsocket(channel, MISSING_PARAMETERS);
                    return;
                }

                User user = User.get(name);

                if (user != null && user.changePassword(password, newPassword)) {
                    SocketServerUtils.sendWebsocket(channel, simple("success", true));
                } else {
                    sendWebsocket(channel, PERMISSION_DENIED);
                }

                break;
            }
            default:
                sendWebsocket(channel, UNKNOWN_ACTION);
                break;
        }
    }

    private void login(Channel channel, Client client, User user) {
        Session session = Session.create(user);

        client.setUser(user);
        client.setSession(session);

        SocketServerUtils.sendWebsocket(channel, simple("token", session.getToken().toString()));
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
