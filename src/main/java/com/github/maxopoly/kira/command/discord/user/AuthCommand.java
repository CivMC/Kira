package com.github.maxopoly.kira.command.discord.user;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.permission.KiraRole;
import com.github.maxopoly.kira.permission.KiraRoleManager;
import com.github.maxopoly.kira.user.AuthManager;
import com.github.maxopoly.kira.user.KiraUser;

import java.util.UUID;

public class AuthCommand extends ArgumentBasedCommand {

	public AuthCommand() {
		super("auth", 1);
		setRequireUser();
		this.deprecated = true;
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
		AuthManager authMan = Kira.Companion.getInstance().getAuthManager();
		UUID uuid = authMan.getUserForCode(code);
		if (uuid == null) {
			return "Invalid auth code";
		}
		String name = authMan.getName(uuid);
		logger.info("Adding " + name + ":" + uuid.toString() + " as ingame account for " + user.toString());
		user.updateIngame(uuid, name);
		KiraRoleManager kiraRoleMan = Kira.Companion.getInstance().getKiraRoleManager();
		Kira.Companion.getInstance().getUserManager().addUser(user);
		Kira.Companion.getInstance().getDiscordRoleManager().giveDiscordRole(Kira.Companion.getInstance().getGuild(), user);
		Kira.Companion.getInstance().getDiscordRoleManager().setName(Kira.Companion.getInstance().getGuild(), user);
		Kira.Companion.getInstance().getDao().updateUser(user);
		Kira.Companion.getInstance().getDiscordRoleManager().syncUser(user);
		KiraRole authRole = kiraRoleMan.getRole("auth");
		if (authRole != null) {
			kiraRoleMan.giveRoleToUser(user, authRole);
		}
		authMan.removeCode(code);
		return "Successfully authenticated as " + name;
	}

}
