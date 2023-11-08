package net.civmc.kira.command.user

import net.civmc.kira.Kira
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger

class AuthCommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name = "auth"
    override val requireUser = true
    override val requiredPermission = "canauth"
    override val global = false

    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier) {
        val authManager = Kira.instance!!.authManager

        val code = event.getOption("code")?.asString

        if (code == null) {
            event.reply("Please supply a code generated in-game with /discordauth").queue()
            return
        }

        if (sender.user.hasIngameAccount()) {
            event.reply("You already have a linked in-game account").queue()
            return
        }

        val uuid = authManager!!.getUserForCode(code)
        if (uuid == null) {
            event.reply("Invalid auth code. Please check that you have copied it correctly.").queue()
            return
        }

        val name = authManager.getName(uuid)
        logger.info("Adding $name:$uuid as in-game account for ${sender.user}")
        sender.user.updateIngame(uuid, name)
        Kira.instance!!.userManager!!.addUser(sender.user)
        Kira.instance!!.discordRoleManager!!.giveDiscordRole(Kira.instance!!.guild, sender.user)
        Kira.instance!!.discordRoleManager!!.setName(Kira.instance!!.guild, sender.user)
        Kira.instance!!.dao!!.updateUser(sender.user)
        Kira.instance!!.discordRoleManager!!.syncUser(sender.user)
        Kira.instance!!.kiraRoleManager!!.giveRoleToUser(sender.user, Kira.instance!!.kiraRoleManager!!.getRole("auth"))
        authManager.removeCode(code)

        event.reply("Successfully authenticated as $name").queue()
    }

    override fun getCommandData(): CommandData {
        return CommandData("auth", "Allows linking your discord account to an in-game account. Run '/discordauth' in-game to get a code.").apply {
            addOption(OptionType.STRING, "code", "Your auth code", true)
        }
    }
}