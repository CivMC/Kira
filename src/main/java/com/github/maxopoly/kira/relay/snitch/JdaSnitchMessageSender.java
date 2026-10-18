package com.github.maxopoly.kira.relay.snitch;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

// Sends batched snitch messages through JDA
public class JdaSnitchMessageSender implements SnitchMessageSender {

	private final Supplier<JDA> jda;

	public JdaSnitchMessageSender(Supplier<JDA> jda) {
		this.jda = jda;
	}

	@Override
	public CompletableFuture<Void> send(long guildId, long channelId, String content, String nonce) {
		JDA api = jda.get();
		Guild guild = api == null ? null : api.getGuildById(guildId);
		if (guild == null) {
			return CompletableFuture.failedFuture(new IllegalStateException("Guild " + guildId + " is not available"));
		}
		TextChannel channel = guild.getTextChannelById(channelId);
		if (channel == null) {
			return CompletableFuture.failedFuture(new IllegalStateException("Channel " + channelId + " does not exist"));
		}
		return channel.sendMessage(content).setNonce(nonce).submit().thenApply(message -> null);
	}
}
