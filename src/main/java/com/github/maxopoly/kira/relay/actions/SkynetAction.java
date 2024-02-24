package com.github.maxopoly.kira.relay.actions;

import org.json.JSONObject;
import java.util.UUID;

public class SkynetAction extends MinecraftAction {

	private SkynetType type;
	private String player;
	private UUID playerUUID;

	public SkynetAction(long timestamp, String player, UUID playerUUID, SkynetType type) {
		super(timestamp);
		this.player = player;
		this.playerUUID = playerUUID;
		this.type = type;
	}

	public String getPlayer() {
		return player;
	}

	public SkynetType getType() {
		return type;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	@Override
	protected void internalConstructJSON(JSONObject json) {
		json.put("player", player);
		json.put("action", type.toString());
	}

}
