package net.cryptic_game.server.microservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.netty.channel.Channel;
import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.config.Config;
import net.cryptic_game.server.config.DefaultConfig;
import net.cryptic_game.server.socket.SocketServerUtils;
import net.cryptic_game.server.user.User;

/**
 * microservice wrapper
 * 
 * @author use-to
 *
 */
public class MicroService {

	private static final Logger logger = Logger.getLogger(MicroService.class);

	// open requests of client
	private static Map<UUID, Client> open = new HashMap<UUID, Client>();

	// online microservices
	private static List<MicroService> services = new ArrayList<MicroService>();

	private String name; // name of ms
	private Channel channel; // socket-channel of ms

	public MicroService(String name, Channel channel) {
		this.name = name;
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public Channel getChannel() {
		return channel;
	}

	/**
	 * send data to ms
	 * 
	 * @param sender   channel of client (for response)
	 * @param endpoint endpoint on ms (string-array)
	 * @param input    data sending to ms
	 */
	public void receive(Client client, JSONArray endpoint, JSONObject input) {
		UUID tag = UUID.randomUUID();

		Map<String, Object> jsonMap = new HashMap<String, Object>();

		jsonMap.put("tag", tag.toString());
		jsonMap.put("data", input);
		jsonMap.put("endpoint", endpoint);

		if (Config.getBoolean(DefaultConfig.AUTH_ENABLED)) {
			if (!client.isValid()) {
				return;
			} else {
				jsonMap.put("user", client.getUser().getUUID().toString());
			}
		} else {
			jsonMap.put("user", UUID.fromString("00000000-0000-0000-0000-000000000000").toString());
		}

		SocketServerUtils.sendJson(this.getChannel(), new JSONObject(jsonMap));

		open.put(tag, client);
	}

	/**
	 * send data back to client
	 * 
	 * param output data from ms
	 * 
	 * @return success
	 */
	public boolean send(JSONObject output) {
		try {
			if (output.containsKey("data") && output.get("data") instanceof JSONObject) {
				JSONObject data = (JSONObject) output.get("data");

				if (output.containsKey("tag") && output.get("tag") instanceof String) {
					UUID tag = UUID.fromString((String) output.get("tag"));

					if (output.containsKey("ms") && output.get("ms") instanceof String && output.containsKey("endpoint")
							&& output.get("endpoint") instanceof JSONArray) {
						MicroService ms = MicroService.get((String) output.get("ms"));

						if (ms != null) {
							ms.receiveFromMicroService(this, (JSONArray) output.get("endpoint"), tag, data);
							return true;
						}
					} else {
						Client client = open.get(tag);
						if (client != null) {
							client.send(data);
							return true;
						}
					}
				}
			}
		} catch (ClassCastException e) {
		}
		return false;
	}

	/**
	 * receives data from another microservice no requests
	 * 
	 * @param ms   microservice
	 * @param data data of sender
	 */
	private void receiveFromMicroService(MicroService ms, JSONArray endpoint, UUID tag, JSONObject data) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		jsonMap.put("ms", ms.getName());
		jsonMap.put("endpoint", endpoint);
		jsonMap.put("tag", tag.toString());
		jsonMap.put("data", data);

		SocketServerUtils.sendJson(this.getChannel(), new JSONObject(jsonMap));
	}

	/**
	 * register microservice
	 * 
	 * @param name    name of ms
	 * @param channel channel of ms
	 */
	@SuppressWarnings("deprecation")
	public static void register(String name, Channel channel) {
		services.add(new MicroService(name, channel));
		logger.log(Priority.INFO, "microservice registered: " + name);
	}

	/**
	 * unregister microservice
	 * 
	 * @param channel channel of ms
	 */
	@SuppressWarnings("deprecation")
	public static void unregister(Channel channel) {
		MicroService ms = MicroService.get(channel);

		if (ms != null) {
			services.remove(ms);
		}
		
		logger.log(Priority.INFO, "microservice unregistered: " + ms.getName());
	}

	/**
	 * @param name name of ms
	 * @return ms by name
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
	 * @param channel channel of ms
	 * @return ms by channel
	 */
	public static MicroService get(Channel channel) {
		for (MicroService ms : services) {
			if (ms.getChannel().equals(channel)) {
				return ms;
			}
		}
		return null;
	}

	public static void sendToUser(UUID user, JSONObject data) {
		User userAccount = User.get(user);

		if (userAccount != null) {
			Client clientOfUser = Client.getClient(userAccount);

			if (clientOfUser != null) {
				clientOfUser.send(data);
			}
		}
	}

}
