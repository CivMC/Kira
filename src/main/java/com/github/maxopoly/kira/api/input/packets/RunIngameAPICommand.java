package com.github.maxopoly.kira.api.input.packets;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.api.input.APIInput;
import com.github.maxopoly.kira.api.input.APISupplier;
import com.github.maxopoly.kira.api.sessions.APIIngameCommandSession;
import com.github.maxopoly.kira.command.discord.user.RunIngameCommand;

public class RunIngameAPICommand extends APIInput {

	public RunIngameAPICommand() {
		super("in-game");
	}

	@Override
	public void handle(JSONObject argument, APISupplier supplier) {
		String command = argument.optString("command");
		if (command == null) {
			return;
		}
		if (!RunIngameCommand.commandPattern.matcher(command).matches() || command.length() > 255) {
			return;
		}
		String id = argument.optString("identifier");
		if (id == null) {
			return;
		}
		APIIngameCommandSession cmd = new APIIngameCommandSession(supplier, command, id);
		Kira.Companion.getInstance().getRequestSessionManager().request(cmd);
	}

}