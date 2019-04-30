package net.cryptic_game.server.client;

import io.netty.channel.Channel;
import net.cryptic_game.server.socket.SocketServerUtils;
import net.cryptic_game.server.user.User;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Client {

    private static List<Client> clients = new ArrayList<Client>();

    private User user;
    private Channel channel;
    private ClientType type;

    private Client(User user, Channel channel, ClientType type) {
        this.user = user;
        this.channel = channel;

        this.type = type;
    }

    private Client(Channel channel, ClientType type) {
        this(null, channel, type);
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setUser(User user) {
        this.user = user;
        if (this.user != null) {
            this.user.updateLast();
        }
    }

    public boolean isValid() {
        return this.getUser() != null;
    }

    public ClientType getType() {
        return type;
    }

    public void send(JSONObject data) {
        if (this.getType().equals(ClientType.HTTP)) {
            SocketServerUtils.sendJsonToHTTPClient(this.getChannel(), data);
        } else if (this.getType().equals(ClientType.WEBSOCKET)) {
            SocketServerUtils.sendJsonToClient(this.getChannel(), data);
        }
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

    public static boolean existsClient(Channel channel) {
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

}
