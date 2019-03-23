package net.cryptic_game.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * default variables instead of env variables
 * 
 * @author use-to
 * 
 */

public enum DefaultConfig {
	
	WEBSOCKET_HOST("0.0.0.0"),
	WEBSOCKET_PORT(80),
	MSSOCKET_HOST("127.0.0.1"),
	MSSOCKET_PORT(1239),
	HTTP_PORT(8080),
	AUTH_ENABLED(true), 
	STORAGE_LOCATION("data/");
	
	
	private Object value;
	
	DefaultConfig(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	
	/**
	 * @return map with all key-value-pairs 
	 */
	public static Map<String, String> defaults() {
		Map<String, String> defaults = new HashMap<String, String>();
		
		for(DefaultConfig e : DefaultConfig.values()) {
			defaults.put(e.toString(), e.getValue().toString());
		}
		
		return defaults;
	}

}
