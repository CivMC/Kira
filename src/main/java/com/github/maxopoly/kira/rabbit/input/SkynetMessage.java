package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.actions.SkynetAction;
import com.github.maxopoly.kira.relay.actions.SkynetType;

public class SkynetMessage extends RabbitMessage {

	public SkynetMessage() {
		super("skynet");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		String player = json.getString("player");
		SkynetType type = SkynetType.valueOf(json.getString("action").toUpperCase());
		long timestamp = json.optLong("timestamp", System.currentTimeMillis());
		SkynetAction action = new SkynetAction(timestamp, player, type);
		Kira.Companion.getInstance().getApiSessionManager().handleSkynetMessage(action);
		Kira.Companion.getInstance().getGroupChatManager().applyToAll(chat -> {chat.sendSkynet(action);});
	}
}
