package net.civmc.kira.proximityChat

import com.rabbitmq.client.Connection
import net.civmc.kira.config.ConfigService
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ProximityChatListener(
    private val configService: ConfigService,
    private val proximityChatChannelManager: ProximityChatChannelManager,
) : ListenerAdapter() {

    /**
     * When a player joins the proximity voice channel, TODO
     */
    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.channelJoined.id != configService.config.proximityChat.lobbyChannelId) {
            return
        }

        proximityChatChannelManager.sortPlayersIntoChannels()
    }

    /**
     * clean up channels if the last player leaves them
     */
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        // Make sure it's a proximity channel
        if (event.channelLeft.parent?.id != configService.config.proximityChat.categoryId) {
            return
        }

        proximityChatChannelManager.sortPlayersIntoChannels()
    }
}
