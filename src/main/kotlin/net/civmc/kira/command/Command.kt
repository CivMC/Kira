package net.civmc.kira.command

import com.github.maxopoly.kira.command.model.discord.DiscordCommandChannelSupplier
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger

abstract class Command(val logger: Logger, val userManager: UserManager) : ListenerAdapter() {

    abstract val name: String
    open val requireUser = false
    open val requireIngameAccount = false
    open val requiredPermission = "default"

    abstract fun getCommandData(): CommandData

    abstract fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier, options: List<OptionMapping>)

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name != name) {
            return
        }

        val user = userManager.getOrCreateUserByDiscordID(event.user.idLong)
        val supplier: InputSupplier = DiscordCommandChannelSupplier(user, event.guild!!.idLong,
                event.channel.idLong)

        if (requireUser && supplier.user == null) {
            supplier.reportBack("You have to be a user to run this command")
            return
        }

        if (requireIngameAccount && !supplier.user.hasIngameAccount()) {
            supplier.reportBack("You need to have an ingame account linked to use this command")
            return
        }

        if (!supplier.hasPermission(requiredPermission)) {
            supplier.reportBack("You don't have the required permission to do this")
            logger.info(supplier.identifier + " attempted to run forbidden command: " + name)
            return
        }

        dispatchCommand(event, supplier, event.options)
    }
}
