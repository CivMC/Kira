package net.civmc.kira.command.user

import com.github.maxopoly.kira.KiraMain
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.apache.logging.log4j.Logger

class AuthCommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name = "auth"
    override val requireUser = true
    override val requiredPermission = "canauth"
    override val global = false

    override fun dispatchCommand(event: SlashCommandInteractionEvent, sender: InputSupplier) {
        val authManager = KiraMain.getInstance().authManager

        val code = event.getOption("code")?.asString

        if (code == null) {
            event.reply("Please supply a code generated in-game with /discordauth").queue()
            return
        }

        if (sender.user.hasIngameAccount()) {
            event.reply("You already have a linked in-game account").queue()
            return
        }

        val uuid = authManager.getUserForCode(code)
        if (uuid == null) {
            event.reply("Invalid auth code. Please check that you have copied it correctly.").queue()
            return
        }

        val name = authManager.getName(uuid)
        logger.info("Adding $name:$uuid as in-game account for ${sender.user}")
        sender.user.updateIngame(uuid, name)
        KiraMain.getInstance().userManager.addUser(sender.user)
        KiraMain.getInstance().discordRoleManager.giveDiscordRole(KiraMain.getInstance().guild, sender.user)
        KiraMain.getInstance().discordRoleManager.setName(KiraMain.getInstance().guild, sender.user)
        KiraMain.getInstance().dao.updateUser(sender.user)
        KiraMain.getInstance().discordRoleManager.syncUser(sender.user)
        KiraMain.getInstance().kiraRoleManager.giveRoleToUser(sender.user, KiraMain.getInstance().kiraRoleManager.getRole("auth"))
        authManager.removeCode(code)

        event.reply("Successfully authenticated as $name").queue()
    }

    override fun getCommandData(): SlashCommandData {
        return Commands.slash(
            "auth",
            "Allows linking your discord account to an in-game account. Run '/discordauth' in-game to get a code."
        ).apply {
            addOption(OptionType.STRING, "code", "Your auth code", true)
        }
    }
}
