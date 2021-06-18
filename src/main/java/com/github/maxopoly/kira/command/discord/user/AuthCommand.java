package com.github.maxopoly.kira.command.discord.user;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.permission.KiraRoleManager;
import com.github.maxopoly.kira.user.AuthManager;
import com.github.maxopoly.kira.user.KiraUser;

import java.util.UUID;

public class AuthCommand extends ArgumentBasedCommand {

	public AuthCommand() {
		super("auth", 1);
		setRequireUser();
	}

	@Override
	public String getFunctionality() {
		return "Allows linking your discord account to an ingame account. Run '/discordauth' ingame to get a code.";
	}

	@Override
	public String getRequiredPermission() {
		return "canauth";
	}

	@Override
	public String getUsage() {
		return "auth [code]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		KiraUser user = sender.getUser();
		if (user.hasIngameAccount()) {
			return "You already have a linked ingame account";
		}
		String code = args[0];
		AuthManager authMan = KiraMain.getInstance().getAuthManager();
		UUID uuid = authMan.getUserForCode(code);
		if (uuid == null) {
			return "Invalid auth code";
		}
		String name = authMan.getName(uuid);
		logger.info("Adding " + name + ":" + uuid.toString() + " as ingame account for " + user.toString());
		user.updateIngame(uuid, name);
		KiraRoleManager kiraRoleMan = KiraMain.getInstance().getKiraRoleManager();
		KiraMain.getInstance().getUserManager().addUser(user);
		KiraMain.getInstance().getDiscordRoleManager().giveDiscordRole(KiraMain.getInstance().getGuild(), user);
		KiraMain.getInstance().getDiscordRoleManager().setName(KiraMain.getInstance().getGuild(), user);
		KiraMain.getInstance().getDAO().updateUser(user);
		KiraMain.getInstance().getDiscordRoleManager().syncUser(user);
		authMan.removeCode(code);
		return "Successfully authenticated as " + name;
	}

}
