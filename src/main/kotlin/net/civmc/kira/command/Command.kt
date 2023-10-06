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
    open val global = true

    abstract fun getCommandData(): CommandData

    abstract fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier)

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name != name) {
            return
        }

        val user = userManager.getOrCreateUserByDiscordID(event.user.idLong)
        val supplier: InputSupplier = DiscordCommandChannelSupplier(user, event.guild!!.idLong,
                event.channel.idLong)

        if (requireUser && supplier.user == null) {
            event.reply("You have to be a user to run this command").queue()
            return
        }

        if (requireIngameAccount && !supplier.user.hasIngameAccount()) {
            event.reply("You need to have an in-game account linked to use this command").queue()
            return
        }

        if (!supplier.hasPermission(requiredPermission)) {
            event.reply("You don't have the required permission to do this").queue()
            logger.info(supplier.identifier + " attempted to run forbidden command: " + name)
            return
        }

        dispatchCommand(event, supplier)
    }
}
