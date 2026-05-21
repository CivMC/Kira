package com.github.maxopoly.kira.relay.actions;

import org.json.JSONObject;

/**
 * Something that happened in Minecraft like a player loggin in, hitting a snitch etc.
 *
 */
public abstract class MinecraftAction {

	private final long timeStamp;
	private final String server;
	private JSONObject json;

	public MinecraftAction(long timeStamp, String server) {
		this.timeStamp = timeStamp;
		this.server = server;
	}

	public String getServer() {
		return server;
	}

	private JSONObject constructJSON() {
		JSONObject json = new JSONObject();
		json.put("time", timeStamp);
		internalConstructJSON(json);
		return json;
	}

	public JSONObject getJSON() {
		if (json != null) {
			return json;
		}
		json = constructJSON();
		return json;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	protected abstract void internalConstructJSON(JSONObject json);

}
