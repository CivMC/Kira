package com.github.maxopoly.kira.rabbit.input;

import com.github.maxopoly.kira.relay.GroupId;
import org.json.JSONObject;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.actions.GroupChatMessageAction;
import java.util.UUID;

public class SendGroupChatMessage extends RabbitMessage {

	public SendGroupChatMessage() {
		super("groupchatmessage");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
        String server = json.getString("server");
		String msg = json.getString("msg");
		String sender = json.getString("sender");
		String group = json.getString("group");
		UUID senderUUID = UUID.fromString(json.getString("senderUUID"));
		long timestamp = json.optLong("timestamp", System.currentTimeMillis());

		GroupChatMessageAction action = new GroupChatMessageAction(timestamp, group, sender, senderUUID, msg);
		KiraMain.getInstance().getAPISessionManager().handleGroupMessage(action);
		GroupChatManager man = KiraMain.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(new GroupId(server, group.toLowerCase()));
		if (chat != null && chat.getConfig().shouldRelayToDiscord()) {
			chat.sendMessage(action);
		}
	}
}
