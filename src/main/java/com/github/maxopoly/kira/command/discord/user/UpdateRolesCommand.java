package com.github.maxopoly.kira.command.discord.user;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.Command;
import com.github.maxopoly.kira.command.model.top.InputSupplier;

public class UpdateRolesCommand extends Command {

    public UpdateRolesCommand() {
        super("updateroles");
        this.deprecated = true;
    }

    @Override
    public String getFunctionality() {
        return "Fixes your auth roles";
    }

    @Override
    public String getRequiredPermission() {
        return "default";
    }

    @Override
    public String getUsage() {
        return "updateroles";
    }

    @Override
    public String handleInternal(String argument, InputSupplier sender) {

        if (sender != null) {
            Kira.Companion.getInstance().getDiscordRoleManager().syncUser(sender.getUser());
            return "Your roles have been updated";
        }

        return "Failed to update your roles! Please report this.";
    }
}
