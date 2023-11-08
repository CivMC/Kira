package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.collections4.CollectionUtils;

public class ListDiscordServersCommand extends ArgumentBasedCommand {

	public ListDiscordServersCommand() {
		super("listdiscordservers", 0, "listdiscords");
	}

	@Override
	public String getFunctionality() {
		return "Lists all Discord servers Kira is in.";
	}

	@Override
	public String getRequiredPermission() {
		return "admin";
	}

	@Override
	public String getUsage() {
		return "listdiscordservers";
	}

	@Override
	public String handle(final InputSupplier sender, final String[] arguments) {
		final var discordBot = Kira.Companion.getInstance().getJda();
		final var botServers = discordBot.getGuilds();
		if (CollectionUtils.isEmpty(botServers)) {
			return "Kira is not in any Discord servers.";
		}
		final var response = new StringBuilder("Kira is currently in: \n• [server id]: server name");
		for (final Guild server : botServers) {
			response.append('\n')
					.append("• Server[`")
					.append(server.getName())
					.append("`:`")
					.append(server.getId())
					.append("`]");
		}
		return response.toString();
	}

}
