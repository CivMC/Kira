package com.github.maxopoly.kira.command.model.discord;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.user.KiraUser;
import com.github.maxopoly.kira.util.DiscordMessageSender;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message;

/**
 * Ugly ugly ugly, a hack way to pass the message object up to the command to fix channel perm
 * checking. Eventually someone can figure out a better way to do this...
 */
public class DiscordCommandChannelMessageSupplier extends DiscordCommandSupplier {

	private long channelID;
	private long guildID;
    private Message message;

	public DiscordCommandChannelMessageSupplier(KiraUser user, long guildID, long channelID, Message message) {
		super(user);
		this.channelID = channelID;
		this.guildID = guildID;
        this.message = message;
	}

	@Override
	public long getChannelID() {
		return channelID;
	}

    public Message getMessage() {
        return message;
    }

	@Override
	public void reportBack(String msg) {
		JDA jda = KiraMain.getInstance().getJDA();
		Guild guild = jda.getGuildById(guildID);
		if (guild == null) {
			return;
		}
		TextChannel channel = guild.getTextChannelById(channelID);
		if (channel == null) {
			return;
		}
		DiscordMessageSender.sendTextChannelMessage(user, channel, msg);
	}

}
