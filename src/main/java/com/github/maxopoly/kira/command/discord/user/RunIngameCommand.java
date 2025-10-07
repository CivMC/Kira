package com.github.maxopoly.kira.command.discord.user;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.util.CommandUtil;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.rabbit.session.SendIngameCommandSession;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

public class RunIngameCommand extends ArgumentBasedCommand {

	public static final Pattern commandPattern = Pattern.compile("[a-zA-Z0-9_\\- !?\\.]+");

	public RunIngameCommand() {
		super("ingame", 1, 100, "mc");
		doesRequireIngameAccount();
		this.deprecated = true;
	}

	@Override
	public String getFunctionality() {
		return "Allows you to run ingame commands from discord";
	}

	@Override
	public String getRequiredPermission() {
		return "ingame_command";
	}

	@Override
	public String getUsage() {
		return "ingame [command]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		if (sender.getUser() == null) {
			return "You are not allowed to do that";
		}
		UUID uuid = sender.getUser().getIngameUUID();
		if (uuid == null) {
			return "You are not tied to an ingame account from which this command could be run";
		}
		StringBuilder sb = new StringBuilder();
		Arrays.stream(args).forEach(s -> sb.append(s + " "));
		String commandString = sb.toString().trim();

		if (!commandPattern.matcher(commandString).matches()) {
			return "Your command contained illegal characters";
		}

        CommandUtil.CommandRoute route = CommandUtil.getRoute(commandString, KiraMain.getInstance().getConfig().getServers());
        if (route.command().length() > 255) {
			return "Your command is too long";
		}
        KiraMain.getInstance().getRequestSessionManager()
                .request(route.server(), new SendIngameCommandSession(sender, route.command()));
		return "Running command `" + route.command() + "` as `" + sender.getUser().getName() + "` on server `" + route.server() + "`";
	}
}
