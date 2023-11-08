package com.github.maxopoly.kira.command.model.discord;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.discord.admin.*;
import com.github.maxopoly.kira.command.discord.api.GenerateAPIToken;
import com.github.maxopoly.kira.command.discord.api.ListTokens;
import com.github.maxopoly.kira.command.discord.api.RevokeAPIToken;
import com.github.maxopoly.kira.command.discord.admin.ConsoleCommand;
import com.github.maxopoly.kira.command.discord.user.RunIngameCommand;
import com.github.maxopoly.kira.command.discord.relay.*;
import com.github.maxopoly.kira.command.discord.user.*;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.command.model.top.TextInputHandler;
import org.apache.logging.log4j.Logger;

public class CommandHandler extends TextInputHandler<Command, String, InputSupplier> {

	public CommandHandler(Logger logger) {
		super(logger);
	}

	@Override
	protected String convertIntoArgument(String raw) {
		return raw;
	}

	@Override
	protected String getCommandArguments(String fullArgument) {
		int index =  fullArgument.indexOf(' ');
		if (index == -1) {
			return "";
		}
		return fullArgument.substring(index + 1, fullArgument.length());
	}

	@Override
	protected String getCommandIdentifier(String argument) {
		int index =  argument.indexOf(' ');
		if (index == -1) {
			return argument;
		}
		return argument.substring(0, index);
	}

	@Override
	protected String getHandlerName() {
		return "Discord Command Handler";
	}

	@Override
	protected void handleError(InputSupplier supplier, String input) {
		supplier.reportBack("Invalid command");
	}

	@Override
	public void registerCommand(Command command) {
		if (command.getRequiredPermission() != null) {
			Kira.Companion.getInstance().getKiraRoleManager().getOrCreatePermission(command.getRequiredPermission());
		}
		super.registerCommand(command);
	}

	@Override
	protected void registerCommands() {
		// Admin
		registerCommand(new CreateDefaultPermsCommand());
		registerCommand(new DeauthDiscordCommand());
		registerCommand(new GiveDefaultPermission());
		registerCommand(new GivePermissionToRoleCommand());
		registerCommand(new GiveRoleCommand());
		registerCommand(new LeaveDiscordServerCommand());
		registerCommand(new ListDiscordRelaysCommand());
		registerCommand(new ListDiscordServersCommand());
		registerCommand(new ListPermissionsForUserCommand());
		registerCommand(new ManageDiscordBansCommand());
		registerCommand(new ReloadPermissionCommand());
		registerCommand(new StopCommand());
		registerCommand(new SyncUserCommand());
		// API
		registerCommand(new GenerateAPIToken());
		registerCommand(new ListTokens());
		registerCommand(new RevokeAPIToken());
		// Game
		registerCommand(new ConsoleCommand());
		registerCommand(new RunIngameCommand());
		// Relay
		registerCommand(new ConfigureRelayConfigCommand());
		registerCommand(new CreateRelayChannelHereCommand());
		registerCommand(new CreateRelayConfig());
		registerCommand(new DeleteRelayCommand());
		registerCommand(new TieRelayConfigCommand());
		// User
		registerCommand(new AuthCommand());
		registerCommand(new ChannelInfoCommand());
		registerCommand(new GetWeightCommand());
		registerCommand(new HelpCommand());
		registerCommand(new InfoCommand());
		registerCommand(new JoinDiscordCommand());
		registerCommand(new QuoteCommand());
		registerCommand(new SelfInfoCommand());
		registerCommand(new UpdateRolesCommand());
		logger.info("Loaded total of " + commands.values().size() + " commands");
	}

}
