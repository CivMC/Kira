package com.github.maxopoly.kira.rabbit.input;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.user.KiraUser;
import com.github.maxopoly.kira.user.UserManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreateGroupChatMessage extends RabbitMessage {

	public CreateGroupChatMessage() {
		super("creategroupchat");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		UUID creatorUUID = UUID.fromString(json.getString("creator"));
		KiraUser creator = Kira.Companion.getInstance().getUserManager().getUserByIngameUUID(creatorUUID);
		if (creator == null) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(creatorUUID,
					"Channel creation failed, " + "no discord account tied");
			return;
		}
		String group = json.getString("group");
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(group);
		if (chat != null) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(creatorUUID,
					"Channel creation failed, a channel for this group already exists");
			return;
		}
		float alreadyOwned = man.getOwnedChatCount(creator);
		float limit = GroupChatManager.getChatCountLimit();
		if (alreadyOwned >= limit) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(creatorUUID,
					"Channel creation failed, you have reached the maximum amount of linked channels possible ("
							+ limit + ")");
			return;
		}
		long channelID = json.optLong("channelID", -1L);
		long guildID = json.optLong("guildID", -1L);
		logger.info("Attempting creation of chat for " + group + " as initiated by " + creator.toString());
		if (channelID == -1) {
			// locally in own discord
			chat = man.createGroupChat(group, creator);
		} else {
			// whereever requested
			chat = man.createGroupChat(group, guildID, channelID, creator);
		}
		if (chat == null) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(creatorUUID,
					"Channel creation failed, " + "ask an admin about this");
			return;
		}
		JSONArray memberArray = json.getJSONArray("members");
		Set<Integer> shouldBeMembers = new HashSet<>();
		UserManager userMan = Kira.Companion.getInstance().getUserManager();
		for (int i = 0; i < memberArray.length(); i++) {
			UUID uuid = UUID.fromString(memberArray.getString(i));
			KiraUser user = userMan.getUserByIngameUUID(uuid);
			if (user == null) {
				continue;
			}
			shouldBeMembers.add(user.getID());
		}
		man.syncAccess(chat, shouldBeMembers);
		Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(creatorUUID, "Created channel successfully");
		JDA jda = Kira.Companion.getInstance().getJda();
		TextChannel channel = jda.getTextChannelById(chat.getDiscordChannelId());
		if (channel != null) {
			channel.getGuild().retrieveMemberById(creator.getDiscordID()).submit()
					.whenComplete((mem, error) -> {
						if (error != null) {
							logger.error("Failed to get user to notify of channel creation");
							return;
						}

						channel.sendMessage("Channel is ready " + mem.getAsMention()).queue();
					});
		}
	}

}
