package net.cryptic_game.server.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;

public class Request {
	
	private Client client;
	private UUID tag;
	private String microservice;
	private JSONObject data;
	
	public Request(Client client, UUID tag, String microservice, JSONObject data) {
		this.client = client;
		this.tag = tag;
		this.microservice = microservice;
		this.data = data;
	}
	
	public Client getClient() {
		return client;
	}
	
	public UUID getTag() {
		return tag;
	}
	
	public JSONObject getData() {
		return data;
	}
	
	public String getMicroservice() {
		return microservice;
	}

	public void send(JSONObject data) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		jsonMap.put("tag", this.getTag().toString());
		jsonMap.put("data", data);
		
		this.getClient().send(new JSONObject(jsonMap));
	}
	
	

}
