package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.Command;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.permission.KiraRoleManager;

public class ReloadPermissionCommand extends Command {

	public ReloadPermissionCommand() {
		super("reloadperms", "reloadpermissions");
	}

	@Override
	public String getFunctionality() {
		return "Reloads all permissions and roles from the database";
	}

	@Override
	public String getRequiredPermission() {
		return "admin";
	}

	@Override
	public String getUsage() {
		return "reloadperms";
	}

	@Override
	public String handleInternal(String argument, InputSupplier sender) {
		KiraRoleManager roleMan = Kira.Companion.getInstance().getDao().loadAllRoles();
		Kira.Companion.getInstance().getKiraRoleManager().reload(roleMan);
		return "Successfully reloaded permissions";
	}
}
