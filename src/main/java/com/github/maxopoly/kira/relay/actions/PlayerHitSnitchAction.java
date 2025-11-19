package com.github.maxopoly.kira.relay.actions;

import org.json.JSONObject;
import java.util.UUID;

public class PlayerHitSnitchAction extends MinecraftAction {

	private String playerName;
	private UUID playerUUID;
	private String snitchName;
	private MinecraftLocation location;
	private SnitchHitType hitType;
	private String groupName;
	private SnitchType snitchType;

	public PlayerHitSnitchAction(long timestamp, String playerName, UUID playerUUID, String snitchname, String groupName,
								 MinecraftLocation location, SnitchHitType hitType, SnitchType snitchType) {
		super(timestamp);
		this.playerName = playerName;
		this.playerUUID = playerUUID;
		this.snitchName = snitchname;
		this.location = location;
		this.hitType = hitType;
		this.groupName = groupName;
		this.snitchType = snitchType;
	}

	public String getGroupName() {
		return groupName;
	}

	public SnitchHitType getHitType() {
		return hitType;
	}

	public MinecraftLocation getLocation() {
		return location;
	}

	public String getPlayerName() {
		return playerName;
	}
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public String getSnitchName() {
		return snitchName;
	}

	@Override
	protected void internalConstructJSON(JSONObject json) {
		json.put("player", playerName);
        json.put("playerUUID", playerUUID.toString());
		json.put("action", hitType.toString());
		JSONObject snitch = new JSONObject();
		snitch.put("name", snitchName);
		snitch.put("group", groupName);
		snitch.put("type", snitchType.toString());
		snitch.put("location", location.toJson());
		json.put("snitch", snitch);
	}

}
