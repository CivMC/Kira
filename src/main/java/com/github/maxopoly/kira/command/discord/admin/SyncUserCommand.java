package com.github.maxopoly.kira.command.discord.admin;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.user.KiraUser;

public class SyncUserCommand extends ArgumentBasedCommand {

    public SyncUserCommand() {
        super ("syncuser", 1);
    }

    @Override
    public String getFunctionality() {
        return "Syncs roles for a user";
    }

    @Override
    public String getRequiredPermission() {
        return "admin";
    }

    @Override
    public String getUsage() {
        return "syncuser [user]";
    }

    @Override
    public String handle(InputSupplier sender, String[] args) {
        StringBuilder sb = new StringBuilder();

        KiraUser user = Kira.Companion.getInstance().getUserManager().parseUser(args[0], sb);

        if (user == null) {
            sb.append("User not found");
            return sb.toString();
        }

        Kira.Companion.getInstance().getDiscordRoleManager().syncUser(user);
        sb.append("Syncing user ").append(user);

        return sb.toString();
    }

}
