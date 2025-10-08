package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.actions.SkynetAction;
import com.github.maxopoly.kira.relay.actions.SkynetType;
import java.util.UUID;

public class SkynetMessage extends RabbitMessage {

	public SkynetMessage() {
		super("skynet");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		String player = json.getString("player");
		UUID playerUUID = UUID.fromString(json.getString("playerUUID"));
		SkynetType type = SkynetType.valueOf(json.getString("action").toUpperCase());
		long timestamp = json.optLong("timestamp", System.currentTimeMillis());

		SkynetAction action = new SkynetAction(timestamp, player, playerUUID, type);
		KiraMain.getInstance().getAPISessionManager().handleSkynetMessage(action);
		KiraMain.getInstance().getGroupChatManager().applyToAll(chat -> {chat.sendSkynet(action);});
	}
}
