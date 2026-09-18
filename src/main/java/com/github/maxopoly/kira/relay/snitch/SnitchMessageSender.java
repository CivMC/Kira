package com.github.maxopoly.kira.relay.snitch;

import java.util.concurrent.CompletableFuture;

// Sends one message to a Discord channel.
public interface SnitchMessageSender {

	// no-op if retrying same nonce
	CompletableFuture<Void> send(long guildId, long channelId, String content, String nonce);
}
