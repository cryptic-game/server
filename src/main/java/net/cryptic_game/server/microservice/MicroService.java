package net.cryptic_game.server.microservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.netty.channel.Channel;
import net.cryptic_game.server.socket.SocketServerUtils;

/**
 * microservice wrapper
 * 
 * @author use-to
 *
 */

public class MicroService {

	// open requests of client
	private static Map<UUID, Channel> webSocketOpen = new HashMap<UUID, Channel>();
	private static Map<UUID, Channel> httpOpen = new HashMap<UUID, Channel>();
	
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
	 * @param sender channel of client (for response)
	 * @param endpoint endpoint on ms (string-array)
	 * @param input data sending to ms
	 */
	public void recive(Channel sender, JSONArray endpoint, JSONObject input) {
		UUID tag = UUID.randomUUID();

		Map<String, Object> jsonMap = new HashMap<String, Object>();

		jsonMap.put("tag", tag.toString());
		jsonMap.put("data", input);
		jsonMap.put("endpoint", endpoint);

		SocketServerUtils.sendJson(this.getChannel(), new JSONObject(jsonMap));

		webSocketOpen.put(tag, sender);
	}
	
	public void receiveHTTP(Channel channel, JSONArray endpoint, JSONObject input) {
		UUID tag = UUID.randomUUID();

		Map<String, Object> jsonMap = new HashMap<String, Object>();

		jsonMap.put("tag", tag.toString());
		jsonMap.put("data", input);
		jsonMap.put("endpoint", endpoint);

		SocketServerUtils.sendJson(this.getChannel(), new JSONObject(jsonMap));

		httpOpen.put(tag, channel);
	}

	/**
	 * send data back to client
	 * 
	 * param output data from ms
	 * @return success
	 */
	public boolean send(JSONObject output) {
		try {
			if (output.containsKey("tag") && output.get("tag") instanceof String && output.containsKey("data")
					&& output.get("data") instanceof JSONObject) {
				UUID tag = UUID.fromString((String) output.get("tag"));

				if (webSocketOpen.containsKey(tag)) {
					SocketServerUtils.sendJsonToClient(webSocketOpen.remove(tag), (JSONObject) output.get("data"));
				} else if(httpOpen.containsKey(tag)) {
					SocketServerUtils.sendJsonToHTTPClient(httpOpen.remove(tag), (JSONObject) output.get("data"));
				}
			}
		} catch (ClassCastException e) {
		}
		return false;
	}

	/**
	 * register microservice
	 * 
	 * @param name name of ms
	 * @param channel channel of ms
	 */
	public static void register(String name, Channel channel) {
		services.add(new MicroService(name, channel));
	}

	/**
	 * unregister microservice
	 * 
	 * @param channel channel of ms
	 */
	public static void unregister(Channel channel) {
		MicroService ms = MicroService.get(channel);

		if (ms != null) {
			services.remove(ms);
		}
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

}
