package net.cryptic_game.server.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;

public class Request {
	
	private Client client;
	private UUID tag;
	
	public Request(Client client, UUID tag) {
		this.client = client;
		this.tag = tag;
	}
	
	public Client getClient() {
		return client;
	}
	
	public UUID getTag() {
		return tag;
	}

	public void send(JSONObject data) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		jsonMap.put("tag", this.getTag().toString());
		jsonMap.put("data", data);
		
		this.getClient().send(new JSONObject(jsonMap));
	}
	
	

}
