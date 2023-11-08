package com.github.maxopoly.kira.command.discord.user;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.Command;
import com.github.maxopoly.kira.command.model.top.InputSupplier;

public class JoinDiscordCommand extends Command {
	public JoinDiscordCommand() {
		super("invite");
		doesRequireIngameAccount();
		this.deprecated = true;
	}

	@Override
	public String getFunctionality() {
		return "Allows you to add Kira to other discords";
	}

	@Override
	public String getRequiredPermission() {
		return "isauth";
	}

	@Override
	public String getUsage() {
		return "invite";
	}

	@Override
	public String handleInternal(String argument, InputSupplier sender) {
		return Kira.Companion.getInstance().getJda().getInviteUrl();
	}
}
