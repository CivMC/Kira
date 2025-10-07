package com.github.maxopoly.kira.rabbit.input;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.github.maxopoly.kira.relay.GroupId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.user.KiraUser;
import com.github.maxopoly.kira.user.UserManager;

public class SyncGroupChatMembers extends RabbitMessage {

	public SyncGroupChatMembers() {
		super("syncgroupchatmembers");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
        String server = json.getString("server");
		JSONArray memberArray = json.getJSONArray("members");
		String group = json.getString("group");
		UUID sender = UUID.fromString(json.getString("sender"));
		GroupChatManager man = KiraMain.getInstance().getGroupChatManager();
		UserManager userMan = KiraMain.getInstance().getUserManager();
		GroupChat chat = man.getGroupChat(new GroupId(server, group.toLowerCase()));
		if (chat == null) {
			KiraMain.getInstance().getMCRabbitGateway().sendMessage(server, sender,
					"That group does not have a relay setup");
			return;
		}
		if (KiraMain.getInstance().getGuild().getIdLong() != chat.getGuildId()) {
			KiraMain.getInstance().getMCRabbitGateway().sendMessage(server, sender,
					"This relay is not managed by Kira, it can not be synced");
			return;
		}
		Set<Integer> shouldBeMembers = new HashSet<>();
		for (int i = 0; i < memberArray.length(); i++) {
			UUID uuid = UUID.fromString(memberArray.getString(i));
			KiraUser user = userMan.getUserByIngameUUID(uuid);
			if (user == null) {
				continue;
			}
			shouldBeMembers.add(user.getID());
		}
		man.syncAccess(chat, shouldBeMembers);
	}
}
