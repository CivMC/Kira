package com.github.maxopoly.kira.relay.actions;

import org.json.JSONObject;
import java.util.UUID;

public class GroupChatMessageAction extends MinecraftAction {
	
	private String group;
	private String sender;
	private UUID senderUUID;
	private String message;

	public GroupChatMessageAction(long timeStamp, String group, String sender, UUID senderUUID, String message) {
		super(timeStamp);
		this.group = group;
		this.sender = sender;
		this.senderUUID = senderUUID;
		this.message = message;
	}
	
	public String getGroupName() {
		return group;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getSender() {
		return sender;
	}

	public UUID getSenderUUID() {
		return senderUUID;
	}

	@Override
	protected void internalConstructJSON(JSONObject json) {
		json.put("group", group);
		json.put("player", sender);
		json.put("playerUUID", senderUUID.toString());
		json.put("message", message);
	}

}
