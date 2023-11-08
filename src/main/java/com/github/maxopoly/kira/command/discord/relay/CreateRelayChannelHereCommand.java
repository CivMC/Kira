package com.github.maxopoly.kira.command.discord.relay;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.user.KiraUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.EnumSet;


public class CreateRelayChannelHereCommand extends ArgumentBasedCommand {

	public CreateRelayChannelHereCommand() {
		super("createrelayhere", 1, "createrelay", "makerelay", "setupsnitchchannel", "setuprelayhere");
		doesRequireIngameAccount();
	}

	@Override
	public String getFunctionality() {
		return "Attempts to create a relay in the channel this message was sent in for the group it was sent by";
	}

	@Override
	public String getRequiredPermission() {
		return "isauth";
	}

	@Override
	public String getUsage() {
		return "createrelayhere [group]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		KiraUser user = sender.getUser();
		long channelID = sender.getChannelID();
		if (channelID <= -1) {
			return "You can't do this from here";
		}
		TextChannel channel = Kira.Companion.getInstance().getJda().getTextChannelById(channelID);
		if (channel == null) {
			return "Something went wrong, tell an admin";
		}
		if (channel.getGuild().getIdLong() == Kira.Companion.getInstance().getGuild().getIdLong() && !sender.hasPermission("admin")) {
			return "You can't create relays here";
		}

		try {
			Member member  = channel.getGuild().retrieveMemberById(sender.getUser().getDiscordID()).complete();
			EnumSet<Permission> perms = member.getPermissions(channel);
			if (!perms.contains(Permission.MANAGE_CHANNEL)) {
				return "You need the 'MANAGE_CHANNEL' permission to add a relay to this channel";
			}
			Kira.Companion.getInstance().getMcRabbitGateway().requestRelayCreation(user, args [0], channel);
			return "Checking permissions for channel handling...";
		} catch (Exception e) {
			return "Something went wrong, tell and admin.";
		}
	}
}

