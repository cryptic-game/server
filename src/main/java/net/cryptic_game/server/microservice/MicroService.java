package net.cryptic_game.server.microservice;

import io.netty.channel.Channel;
import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.client.Request;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.user.User;
import net.cryptic_game.server.utils.JSON;
import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.cryptic_game.server.socket.SocketServerUtils.sendRaw;
import static net.cryptic_game.server.utils.JSONBuilder.error;

public class MicroService {

    private static final Logger logger = LoggerFactory.getLogger(MicroService.class);

    // open requests of client
    private static final Map<UUID, Request> open = new HashMap<>();

    // online microservices
    private static final List<MicroService> services = Collections.synchronizedList(new ArrayList<>());

    private final String name; // name of ms
    private final Channel channel; // socket-channel of ms

    private MicroService(String name, Channel channel) {
        this.name = name;
        this.channel = channel;
    }

    public static List<MicroService> getOnlineMicroServices() {
        return services;
    }

    /**
     * Registers a microservice
     *
     * @param name    name of the microservice
     * @param channel channel of the microservice
     */
    static void register(String name, Channel channel) {
        services.add(new MicroService(name, channel));
        logger.info("microservice registered: " + name);

        for (Request r : open.values()) {
            if (r.getMicroService().equals(name)) {
                sendRaw(channel, r.getData());
                return;
            }
        }
    }

    /**
     * Unregisters a microservice
     *
     * @param channel channel of the microservice
     */
    static void unregister(Channel channel) {
        MicroService ms = MicroService.get(channel);

        if (ms != null) {
            services.remove(ms);
            logger.info("microservice unregistered: " + ms.getName());
        }
    }

    /**
     * @param name name of the microservice
     * @return the microservice by name or null
     */
    public static MicroService get(String name) {
        for (MicroService ms : services) {
            if (ms.getName().equals(name)) {
                return ms;
            }
        }
        return null;
    }

    /**
     * @param channel channel of the microservice
     * @return the microservice by channel or null
     */
    public static MicroService get(Channel channel) {
        for (MicroService ms : services) {
            if (ms.getChannel().equals(channel)) {
                return ms;
            }
        }
        return null;
    }

    static void sendToUser(UUID user, JSONObject data) {
        User userAccount = User.get(user);

        if (userAccount != null) {
            Client clientOfUser = Client.getClient(userAccount);

            if (clientOfUser != null) {
                clientOfUser.send(data);
            }
        }
    }

    public String getName() {
        return name;
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * Sends data to microservice
     *
     * @param client   channel of client (for response)
     * @param endpoint endpoint on ms (string-array)
     * @param input    data sending to ms
     */
    public void receive(Client client, JSONArray endpoint, JSONObject input, UUID clientTag) {
        UUID tag = UUID.randomUUID();

        JSONBuilder jsonBuilder = JSONBuilder.anJSON()
                .add("tag", tag.toString())
                .add("data", input)
                .add("endpoint", endpoint);

        if (Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
            if (!client.isValid()) {
                return;
            } else {
                jsonBuilder.add("user", client.getUser().getUUID().toString());
            }
        } else {
            jsonBuilder.add("user", UUID.fromString("00000000-0000-0000-0000-000000000000").toString());
        }

        JSONObject json = jsonBuilder.build();

        sendRaw(getChannel(), json);
        open.put(tag, new Request(client, clientTag, getName(), json));

        if (!Config.getBoolean(DefaultConfig.PRODUCTIVE)) {
            logger.info(getName() + " < " + json.toString());
        }

        new Thread(() -> {
            try {
                Thread.sleep(1000L * Config.getInteger(DefaultConfig.RESPONSE_TIMEOUT));
            } catch (InterruptedException e) {
                logger.error("Interrupted", e);
            }

            if (open.containsKey(tag)) {
                open.remove(tag).send(error("timeout"));
            }
        }).start();
    }

    /**
     * Sends data back to client
     *
     * @param output data from microservice
     * @return success
     */
    boolean send(JSONObject output) {
        JSON json = new JSON(output);

        JSONObject data = json.get("data", JSONObject.class);
        if (data == null) {
            return false;
        }

        UUID tag;
        try {
            tag = UUID.fromString(json.get("tag"));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return false;
        }

        MicroService microService = MicroService.get(json.get("ms"));
        JSONArray endpoint = json.get("endpoint", JSONArray.class);
        if (microService == null || endpoint == null) {
            Request req = open.get(tag);
            if (req != null) {
                req.send(data);
                open.remove(tag);

                if (!Config.getBoolean(DefaultConfig.PRODUCTIVE)) {
                    logger.info(getName() + " > " + data.toString());
                }
            }

            return true;
        }

        microService.receiveFromMicroService(this, endpoint, tag, data);

        return true;
    }

    /**
     * Receives data from another microservice no requests
     *
     * @param ms   microservice
     * @param data data of the sender
     */
    private void receiveFromMicroService(MicroService ms, JSONArray endpoint, UUID tag, JSONObject data) {
        sendRaw(channel, JSONBuilder.anJSON()
                .add("ms", ms.getName())
                .add("endpoint", endpoint)
                .add("tag", tag.toString())
                .add("data", data).build());
    }

}
