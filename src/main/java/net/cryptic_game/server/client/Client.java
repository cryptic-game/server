package net.cryptic_game.server.client;

import io.netty.channel.Channel;
import net.cryptic_game.server.user.Session;
import net.cryptic_game.server.user.User;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static net.cryptic_game.server.socket.SocketServerUtils.sendHTTP;
import static net.cryptic_game.server.socket.SocketServerUtils.sendWebsocket;

public class Client {

    private static final List<Client> clients = new ArrayList<>();
    private final Channel channel;
    private final ClientType type;
    private User user;
    private Session session;

    private Client(User user, Channel channel, ClientType type) {
        this.user = user;
        this.channel = channel;
        this.type = type;
    }

    private Client(Channel channel, ClientType type) {
        this(null, channel, type);
    }

    public static Client getClient(Channel channel) {
        for (Client client : clients) {
            if (client.getChannel().equals(channel)) {
                return client;
            }
        }

        return null;
    }

    public static Client getClient(User user) {
        for (Client client : clients) {
            if (client.getUser().getUUID().equals(user.getUUID())) {
                return client;
            }
        }

        return null;
    }

    private static boolean existsClient(Channel channel) {
        return getClient(channel) != null;
    }

    public static void login(Channel channel, User user) {
        Client client = getClient(channel);

        if (client != null) {
            client.setUser(user);
        }
    }

    public static void logout(Channel channel) {
        Client client = getClient(channel);

        if (client != null) {
            client.setUser(null);

            if (client.getSession() != null) {
                client.getSession().delete();
                client.setSession(null);
            }
        }
    }

    public static int getOnlineCount() {
        return clients.size();
    }

    public static void addClient(Channel channel, ClientType type) {
        if (!existsClient(channel)) {
            clients.add(new Client(channel, type));
        }
    }

    public static void removeClient(Channel channel) {
        Client client = getClient(channel);
        if (client != null) {
            clients.remove(client);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.updateLast();
        }
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isValid() {
        return getUser() != null;
    }

    public void send(JSONObject data) {
        if (type.equals(ClientType.HTTP)) {
            sendHTTP(channel, data);
        } else if (type.equals(ClientType.WEBSOCKET)) {
            sendWebsocket(channel, data);
        }
    }

}
