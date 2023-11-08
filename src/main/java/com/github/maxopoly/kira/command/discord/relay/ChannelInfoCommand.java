package com.github.maxopoly.kira.command.discord.relay;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;

import java.util.Set;

public class ChannelInfoCommand extends ArgumentBasedCommand {

	public ChannelInfoCommand() {
		super("channelinfo", 0, "relayinfo");
		setRequireUser();
	}

	@Override
	public String getFunctionality() {
		return "Shows all relays owned by you";
	}

	@Override
	public String getRequiredPermission() {
		return "isauth";
	}

	@Override
	public String getUsage() {
		return "getchannels";
	}

	@Override
	public String handle(InputSupplier supplier, String[] args) {
		StringBuilder reply = new StringBuilder();
		long channelID = supplier.getChannelID();
		if (channelID <= -1) {
			return "You can't do this from here";
		}
		Set <GroupChat> chats = Kira.Companion.getInstance().getGroupChatManager().getChatByChannelID(channelID);
		reply.append("A total of " + chats.size() + " relays are setup for this channel\n");
		reply.append("Channel id: " + channelID + "\n---\n");
		for(GroupChat chat : chats) {
			reply.append("**" + chat.getName() + "**");
			reply.append(String.format("  Owner: %s\n", chat.getCreator().getName()));
			reply.append(String.format("  Config: %s\n\n", chat.getConfig().getName()));
		}
		return reply.toString();
	}

}
