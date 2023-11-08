package com.github.maxopoly.kira.util;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.user.KiraUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class DiscordMessageSender {

	private static final int MAX_MSG_LENGTH = 1950;

	private static final Logger logger = Kira.Companion.getInstance().getLogger();

	/**
	 * Splits up arbitrary messages into ones not exceeding the character limit of
	 * 2000 per message and sends them
	 * 
	 * @param user     User to ping at the beginning of each message, may be null
	 *                 for no ping
	 * @param guild    Guild sending to. May be null, but must not be null when
	 *                 pinging a user
	 * @param receiver Consumer for actually sending the message. Having a consumer
	 *                 here allows using this for both pms and text channels
	 * @param msg      Message to send
	 */
	private static void sendMessageInternal(KiraUser user, Guild guild, Consumer<String> receiver, String msg) {
		if (msg.trim().length() == 0) {
			return;
		}
		msg = msg
				.replaceAll("§\\w", "")
				.replaceAll("_", "\\_")
				.replaceAll("\\*", "\\*")
				.replaceAll("~", "\\~");
		String tag = "";
		if (guild != null && user != null) {
			try {
				Member member = guild.retrieveMemberById(user.getDiscordID()).complete();
				if (member != null) {
					tag = member.getAsMention() + "\n";
				}
			} catch (Exception e) {
				// NO-OP
			}
		}
		if (msg.length() + tag.length() <= MAX_MSG_LENGTH) {
			receiver.accept(tag + msg);
			return;
		}
		int allowedLengthWithoutTag = MAX_MSG_LENGTH - tag.length();
		String[] split = msg.split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			String currString = split[i];
			if (sb.length() + currString.length() > allowedLengthWithoutTag) {
				if (sb.length() == 0) {
					if (currString.length() > allowedLengthWithoutTag) {
						int beginIndex = 0;
						while (beginIndex != currString.length()) {
							int endIndex = Math.min(beginIndex + allowedLengthWithoutTag, currString.length());
							String subPart = currString.substring(beginIndex, endIndex);
							receiver.accept(tag + subPart);
							beginIndex = endIndex;
						}
					} else {
						receiver.accept(tag + currString);
					}
				} else {
					receiver.accept(sb.toString());
					sb = new StringBuilder();
				}
			} else {
				if (sb.length() == 0) {
					sb.append(tag);
				}
				sb.append(currString);
				sb.append('\n');
			}
		}
		if (sb.length() != 0) {
			receiver.accept(sb.toString());
		}
	}

	public static void sendPrivateMessage(KiraUser user, String msg) {
		JDA jda = Kira.Companion.getInstance().getJda();

		jda.retrieveUserById(user.getDiscordID()).submit()
				.whenComplete((discordUser, error) -> {
					if (error != null) {
						logger.warn("Failed to send PM to user " + user.getDiscordID(), error);
						return;
					}

					PrivateChannel pm = discordUser.openPrivateChannel().complete();
					sendMessageInternal(null, null, s -> {
						pm.sendMessage(s).queue();
						logger.info(String.format("PM [Kira -> %s]: %s", user.getName(), s));
					}, msg);
				});
	}

	public static void sendTextChannelMessage(KiraUser user, TextChannel channel, String msg) {
		sendMessageInternal(user, channel.getGuild(), s -> {
			channel.sendMessage(s).queue();
			logger.info(String.format("CHAT [%s][%s] Kira: %s", channel.getGuild().getName(), channel.getName(), s));
		}, msg);
	}
}
