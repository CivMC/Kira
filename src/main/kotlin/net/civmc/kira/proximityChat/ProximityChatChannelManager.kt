package net.civmc.kira.proximityChat

import kotlinx.coroutines.GlobalScope
import net.civmc.kira.Kira
import net.civmc.kira.config.ConfigService
import net.civmc.kira.event.EventBus
import net.civmc.kira.proximityChat.grouping.ProximityChatGroupService
import net.civmc.kira.rabbit.PlayerLogOffRabbitMessage
import net.civmc.kira.rabbit.RabbitMessageEvent
import net.civmc.kira.rabbit.PlayerUpdateLocationRabbitMessage
import net.dv8tion.jda.api.Permission
import java.lang.Error
import java.lang.IllegalStateException
import kotlin.random.Random

/**
 * This class is responsible for managing proximity chat channels in Discord.
 */
class ProximityChatChannelManager(
    private val configService: ConfigService,
    eventBus: EventBus,
    private val groupService: ProximityChatGroupService,
    private val playerInfoService: PlayerInfoService,
) {

    // TODO: This doesn't belong in this file
    init {
        eventBus.subscribe(GlobalScope) { event ->
            if (event !is RabbitMessageEvent) {
                return@subscribe
            }

            when (event.message) {
                is PlayerUpdateLocationRabbitMessage -> {
                    playerInfoService.updatePlayer(PlayerInfoService.PlayerInfo(
                            event.message.name,
                            PlayerInfoService.PlayerLocation(
                                    event.message.dimension,
                                    event.message.x,
                                    event.message.y,
                                    event.message.z
                            )
                    ))
                }
                is PlayerLogOffRabbitMessage -> {
                    playerInfoService.removePlayer(event.message.name)
                }
            }

            sortPlayersIntoChannels()
        }
    }

    /**
     * Get any channels that may exist already
     */
    private val existingChannels
        get() = Kira.instance?.jda
            ?.getCategoryById(configService.config.proximityChat.categoryId)
            ?.voiceChannels
            .orEmpty()

    /**
     * Quick and dirty channel management, based on the first player in the group found.
     * This may very well cause some unintended shuffling around, but for now I just want
     * to get something functional and launched and this can be improved later. Yes, I know
     * it's really messy. Yes, I intend on cleaning it up later.
     */
    fun sortPlayersIntoChannels() {
        /** All members to move to an appropriate channel*/
        val unsortedMembers = existingChannels.flatMap { it.members }.toMutableList()

        /** Any existing channels that may be used */
        val validChannels = existingChannels.filter { it.id != configService.config.proximityChat.lobbyChannelId }

        unsortedMembers.forEach { member ->
            try {
                val kiraUser = Kira.instance?.userManager?.getUserByDiscordID(member.idLong) ?: throw IllegalStateException()

                if (kiraUser.name == null) {
                    println("Unauthed User, moving to lobby.")
                    member.voiceState?.guild?.moveVoiceMember(
                        member,
                        member.voiceState?.guild?.getVoiceChannelById(configService.config.proximityChat.lobbyChannelId)
                    )?.queue()
                    return@forEach
                }

                // Get the group that the voice member is in
                val group = groupService.getPlayerGroups().firstOrNull { it.any { it.name == kiraUser.name } }

                if (group == null) {
                    println("Not online user, moving to lobby.")
                    member.voiceState?.guild?.moveVoiceMember(
                        member,
                        member.voiceState?.guild?.getVoiceChannelById(configService.config.proximityChat.lobbyChannelId)
                    )?.queue()
                    return@forEach
                }

                // We'll send them into an existing channel if it already has at least two members in their group.
                // Otherwise, we'll make a new channel for them
                // This will almost certainly result in moving some single users around annoyingly.
                val targetChannel = validChannels.find { voiceChannel ->
                    val channelKiraUsers = voiceChannel.members.map { Kira.instance?.userManager?.getUserByDiscordID(member.idLong) }

                    channelKiraUsers.count {
                        user -> group.map { it.name }.contains(user?.name)
                    } >= 2
                } ?: Kira.instance?.jda?.getCategoryById(configService.config.proximityChat.categoryId)?.createVoiceChannel(randomChannelName())?.complete()

                targetChannel?.createPermissionOverride(member.voiceState!!.guild.publicRole)?.setDeny(Permission.VIEW_CHANNEL)?.complete()

                // If we found a channel, send them in.
                member.voiceState?.guild?.moveVoiceMember(member, targetChannel)?.queue()
            } catch (e: Error) {
                println(e)
                // TODO
            }
        }

        // Delete any unused channels
        validChannels.filter { it.members.size == 0 }.forEach { it.delete().queue() }
    }

    val chars : List<Char> = ('a'..'z') + ('0'..'9')
    fun randomChannelName() = "proximity-chat-" + (1..5)
        .map { Random.nextInt(0, chars.size).let { chars[it] } }
        .joinToString("")
}
