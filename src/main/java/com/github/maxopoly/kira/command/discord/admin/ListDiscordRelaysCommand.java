package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;

public class ListDiscordRelaysCommand extends ArgumentBasedCommand {

	public ListDiscordRelaysCommand() {
		super("listdiscordrelays", 0, "listrelays");
	}

	@Override
	public String getFunctionality() {
		return "Lists all Discord relays Kira serves.";
	}

	@Override
	public String getRequiredPermission() {
		return "admin";
	}

	@Override
	public String getUsage() {
		return "listdiscordrelays";
	}

	@Override
	public String handle(final InputSupplier sender, final String[] arguments) {
		final var groupChatManager = Kira.Companion.getInstance().getGroupChatManager();
		final var groupChats = groupChatManager.getGroupChats();
		if (groupChats.isEmpty()) {
			return "Kira is not serving any relays.";
		}
		final var discordBot = Kira.Companion.getInstance().getJda();
		final var response = new StringBuilder("Kira is currently serving relays:");
		for (final GroupChat groupChat : groupChats) {
			final var server = discordBot.getGuildById(groupChat.getGuildId());
			final var channel = server == null ? null : server.getTextChannelById(groupChat.getDiscordChannelId());
			response.append('\n')
					.append("• Relay[`").append(groupChat.getID()).append("`] ")
					.append("Server[").append(server == null ? null : server.getName()).append(":`").append(groupChat.getGuildId()).append("`] ")
					.append("Channel[").append(channel == null ? null : channel.getName()).append(":`").append(groupChat.getDiscordChannelId()).append("`] ")
					.append("Group[").append(groupChat.getName()).append("]");
		}
		return response.toString();
	}

}
