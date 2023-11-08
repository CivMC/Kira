package net.civmc.kira.command.user

import net.civmc.kira.Kira
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.rabbit.session.SendIngameCommandSession
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger
import java.util.regex.Pattern

class IngameCommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name ="in-game"
    override val requiredPermission = "ingame_command"
    override val requireIngameAccount = true

    private val commandPattern = Pattern.compile("[a-zA-Z0-9_\\- !?\\.]+")

    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier) {
        val command = event.getOption("command")?.asString

        if (command == null) {
            event.reply("Command option is missing.").queue()
            return
        }

        if (!commandPattern.matcher(command).matches()) {
            event.reply("Your command contained illegal characters").queue()
            return
        }

        if (command.length > 255) {
            event.reply("Your command is too long").queue()
            return
        }

        // TODO: Reply to command by event
        Kira.instance!!.requestSessionManager!!.request(SendIngameCommandSession(sender, command))
        event.reply("Running command `$command` as `${sender.user.name}`").queue()
    }

    override fun getCommandData(): CommandData {
        return CommandData("in-game", "Allows you to run in-game commands from discord").apply {
            addOption(OptionType.STRING, "command", "The command to run in-game")
        }
    }
}