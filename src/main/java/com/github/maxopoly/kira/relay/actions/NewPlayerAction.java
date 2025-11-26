package com.github.maxopoly.kira.relay.actions;

import org.json.JSONObject;
import java.util.UUID;

public class NewPlayerAction extends MinecraftAction {

	private final String player;
	private UUID playerUUID;

	public NewPlayerAction(long timestamp, String player, UUID playerUUID) {
		super(timestamp);
		this.player = player;
		this.playerUUID = playerUUID;
	}

	public String getPlayer() {
		return player;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	@Override
	protected void internalConstructJSON(JSONObject json) {
		json.put("player", player);
        json.put("playerUUID", playerUUID.toString());
	}

}
