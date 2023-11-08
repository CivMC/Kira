package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;

public class ManageDiscordBansCommand extends ArgumentBasedCommand {

	public ManageDiscordBansCommand() {
		super("managediscordbans", 2);
	}

	@Override
	public String getFunctionality() {
		return "Manages Discord bans.";
	}

	@Override
	public String getRequiredPermission() {
		return "admin";
	}

	@Override
	public String getUsage() {
		return "managediscordbans [isbanned/ban/unban] <server id>";
	}

	@Override
	public String handle(final InputSupplier sender, final String[] arguments) {
		switch (arguments[0].toUpperCase()) {
			case "IS":
			case "ISBANNED":
				return isServerBanned(sender, arguments[1]);
			case "BAN":
				return banServer(sender, arguments[1]);
			case "UNBAN":
				return unbanServer(sender, arguments[1]);
			default:
				return "Unrecognised subcommand \"" + arguments[0] + "\"";
		}
	}

	private String isServerBanned(final InputSupplier sender, final String rawServerID) {
		final long serverID;
		try {
			serverID = Long.parseUnsignedLong(rawServerID);
		}
		catch (final NumberFormatException ignored) {
			return "That Discord server ID was invalid.";
		}
		final var databaseManager = Kira.Companion.getInstance().getDao();
		if (databaseManager.isServerBanned(serverID)) {
			return "That Discord server is banned.";
		}
		return "That Discord server is not banned.";
	}

	private String banServer(final InputSupplier sender, final String rawServerID) {
		final long serverID;
		try {
			serverID = Long.parseUnsignedLong(rawServerID);
		}
		catch (final NumberFormatException ignored) {
			return "That Discord server ID was invalid.";
		}
		Kira.Companion.getInstance().getDao().banServer(serverID);
		final var discordServer = Kira.Companion.getInstance().getJda().getGuildById(serverID);
		if (discordServer != null) {
			discordServer.leave().queue(
					(success) -> sender.reportBack("Kira has also left that server."),
					(failure) -> sender.reportBack("Kira could not leave that server. Try again with /leavediscordserver"));
		}
		return "That Discord server has now been banned";
	}

	private String unbanServer(final InputSupplier sender, final String rawServerID) {
		final long serverID;
		try {
			serverID = Long.parseUnsignedLong(rawServerID);
		}
		catch (final NumberFormatException ignored) {
			return "That Discord server ID was invalid.";
		}
		Kira.Companion.getInstance().getDao().unbanServer(serverID);
		return "That Discord server has now been unbanned.";
	}

}
