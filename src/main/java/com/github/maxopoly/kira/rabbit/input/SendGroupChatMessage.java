package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.actions.GroupChatMessageAction;

public class SendGroupChatMessage extends RabbitMessage {

	public SendGroupChatMessage() {
		super("groupchatmessage");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		String msg = json.getString("msg");
		String sender = json.getString("sender");
		String group = json.getString("group");
		long timestamp = json.optLong("timestamp", System.currentTimeMillis());
		GroupChatMessageAction action = new GroupChatMessageAction(timestamp, group, sender, msg);
		Kira.Companion.getInstance().getApiSessionManager().handleGroupMessage(action);
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(group);
		if (chat != null && chat.getConfig().shouldRelayToDiscord()) {
			chat.sendMessage(action);
		}
	}
}
