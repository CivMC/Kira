package com.github.maxopoly.kira.rabbit.input;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import net.civmc.kira.Kira;
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
		JSONArray memberArray = json.getJSONArray("members");
		String group = json.getString("group");
		UUID sender = UUID.fromString(json.getString("sender"));
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		UserManager userMan = Kira.Companion.getInstance().getUserManager();
		GroupChat chat = man.getGroupChat(group);
		if (chat == null) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(sender,
					"That group does not have a relay setup");
			return;
		}
		if (Kira.Companion.getInstance().getGuild().getIdLong() != chat.getGuildId()) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(sender,
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