package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;

public class LeaveDiscordServerCommand extends ArgumentBasedCommand {

	public LeaveDiscordServerCommand() {
		super("leavediscordserver", 1, "leavediscord");
	}

	@Override
	public String getFunctionality() {
		return "Make Kira leave a specified server.";
	}

	@Override
	public String getRequiredPermission() {
		return "admin";
	}

	@Override
	public String getUsage() {
		return "leavediscordserver <server id>";
	}

	@Override
	public String handle(final InputSupplier sender, final String[] arguments) {
		final var discordBot = Kira.Companion.getInstance().getJda();
		final var foundServer = discordBot.getGuildById(arguments[0]);
		if (foundServer == null) {
			return "Kira is not in that server.";
		}
		foundServer.leave().queue(
				(success) -> sender.reportBack("Successfully left " + arguments[0]),
				(failure) -> sender.reportBack("Was unable to leave " + arguments[0]));
		return "Leave operation queued.";
	}

}
