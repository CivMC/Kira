package com.github.maxopoly.kira.rabbit.input;

import java.util.UUID;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.DiscordCommandChannelSupplier;
import com.github.maxopoly.kira.command.model.discord.DiscordCommandPMSupplier;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.user.KiraUser;

import net.dv8tion.jda.api.entities.TextChannel;

public class ReplyToUserMessage extends RabbitMessage {

	public ReplyToUserMessage() {
		super("replytouser");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		UUID uuid = UUID.fromString(json.getString("user"));
		String msg = json.getString("msg");
		long channelId = json.getLong("channel");
		TextChannel channel = null;
		if (channelId != -1) {
			channel = Kira.Companion.getInstance().getJda().getTextChannelById(channelId);
		}
		KiraUser user = Kira.Companion.getInstance().getUserManager().getUserByIngameUUID(uuid);
		if (user == null)  {
			Kira.Companion.getInstance().getLogger().warn("Failed to find user with uuid " + uuid + " for message  " + msg);
			return;
		}
		if (channel == null) {
				new DiscordCommandPMSupplier(user).reportBack(msg);
			return;
		}
		//we have a valid channel id, so reply there
		new DiscordCommandChannelSupplier(user, channel.getGuild().getIdLong(), channelId).reportBack(msg);;
	}
}
