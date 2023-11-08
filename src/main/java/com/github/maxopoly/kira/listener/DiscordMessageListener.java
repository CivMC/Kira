package com.github.maxopoly.kira.listener;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.CommandHandler;
import com.github.maxopoly.kira.command.model.discord.DiscordCommandChannelSupplier;
import com.github.maxopoly.kira.command.model.discord.DiscordCommandPMSupplier;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.user.KiraUser;
import com.github.maxopoly.kira.user.UserManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Set;

public class DiscordMessageListener extends ListenerAdapter {

	private String keyWord;
	private CommandHandler cmdHandler;
	private Logger logger;
	private UserManager userManager;
	private long ownID;

	public DiscordMessageListener(CommandHandler cmdHandler, Logger logger, UserManager userManager, long ownID) {
		this.cmdHandler = cmdHandler;
		this.logger = logger;
		this.ownID = ownID;
		this.userManager = userManager;
		this.keyWord = Kira.Companion.getInstance().getConfig().getCommandPrefix();
		// Temporary hack
		if (!this.keyWord.endsWith(" ")) {
			this.keyWord = this.keyWord + " ";
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!isValidDiscordAccount(event.getAuthor())) {
			return;
		}
		KiraUser user = userManager.getOrCreateUserByDiscordID(event.getAuthor().getIdLong());
		String content = event.getMessage().getContentRaw();
		if (event.isFromType(ChannelType.PRIVATE)) {
			user.setCurrentDiscordUser(event.getAuthor());
			logger.info(String.format("PM [%s -> Kira]: %s", event.getAuthor().getName(),
					event.getMessage().getContentDisplay()));
			cmdHandler.handle(content, new DiscordCommandPMSupplier(user));
		} else {
			if (content.startsWith(keyWord)) {
				logger.info(
						String.format("CHAT [%s][%s] %s: %s", event.getGuild().getName(), event.getTextChannel().getName(),
								event.getMember().getEffectiveName(), event.getMessage().getContentDisplay()));
				InputSupplier supplier = new DiscordCommandChannelSupplier(user, event.getGuild().getIdLong(),
						event.getChannel().getIdLong());
				cmdHandler.handle(content.substring(keyWord.length()), supplier);
				return;
			}
			GroupChatManager chatMan = Kira.Companion.getInstance().getGroupChatManager();
			Set<GroupChat> chats = chatMan.getChatByChannelID(event.getChannel().getIdLong());
			if (!chats.isEmpty() && user.hasIngameAccount()) {
				String message = event.getMessage().getContentDisplay();
				message = sanitize(message);
				boolean delete = false;
				if (!message.equals("")) {
					for (GroupChat chat : chats) {
						if (chat.getConfig().shouldRelayFromDiscord()) {
							Kira.Companion.getInstance().getMcRabbitGateway().sendGroupChatMessage(user, chat, message);
						}
						if (chat.getConfig().shouldDeleteDiscordMessage()) {
							delete = true;
						}
					}
				}
				if (delete) {
					event.getMessage().delete().queue();
				}
			}
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (!isValidDiscordAccount(event.getUser())) {
			return;
		}
		KiraUser user = userManager.getOrCreateUserByDiscordID(event.getUser().getIdLong());
		if (user.hasIngameAccount()) {
			Kira.Companion.getInstance().getDiscordRoleManager().giveDiscordRole(Kira.Companion.getInstance().getGuild(), user);
		}
	}

	private boolean isValidDiscordAccount(User user) {
		if (user == null) {
			return false;
		}
		return !(user.isBot() || user.getIdLong() == ownID);
	}

	private String sanitize(String input) {
		String result = input.replace("\n", "");
		result = result.replace("\r", "");
		result = result.replace("\t", "");
		result = result.replace("§", "");
		result = result.trim();
		if (result.length() > 255) {
			result = result.substring(0, 255);
		}
		return result;
	}

	// Refuse to join a Discord server if it's banned.
	@Override
	public void onGuildJoin(@Nonnull final GuildJoinEvent event) {
		final var discordServer = event.getGuild();
		if (Kira.Companion.getInstance().getDao().isServerBanned(discordServer.getIdLong())) {
			discordServer.leave().queue();
		}
	}

}
