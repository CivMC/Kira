package net.civmc.kira.command.user

import net.civmc.kira.Kira
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger

class UpdateRolesCommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name = "updateroles"
    override val requireUser = true
    override val global = false

    // TODO: Check if anything actually happened and give more appropriate messages
    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier) {
        Kira.instance!!.discordRoleManager!!.syncUser(sender.user)
        event.reply("Your roles have been updated. If you did not get roles, you do not have a linked account").queue()
    }

    override fun getCommandData(): CommandData {
        return CommandData("updateroles", "Fixes your auth roles")
    }
}
