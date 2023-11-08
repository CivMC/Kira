package net.civmc.kira.command.user

import net.civmc.kira.Kira
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.civmc.kira.command.CommandManager
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger
import java.util.*

class HelpCommand(logger: Logger, userManager: UserManager) : Command(logger, userManager) {

    override val name = "help"

    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier) {
        val command = CommandManager.commands.find { it.name == event.getOption("command")?.asString }

        // TODO: Handle excluding commands without permissions better
        if (command != null && sender.hasPermission(command.requiredPermission)) {
            event.reply(formatCommandData(command.getCommandData())).queue()
            return
        }

        event.reply(CommandManager.commands.filter { sender.hasPermission(it.requiredPermission) }.joinToString("\n") {
            formatCommandData(it.getCommandData())
        }).queue()
    }

    // TODO: A nicer format
    private fun formatCommandData(commandData: CommandData): String {
        val format = """
            - %command% - %description%
              %options%
        """.trimIndent()

        var optionsFormat = """
            - %option% - %description%
        """.trimIndent()

        val options = commandData.options.joinToString("\n") {
            optionsFormat
                    .replace("%option%", it.name)
                    .replace("%description%", it.description)
        }

        return format
                .replace("%command%", commandData.name)
                .replace("%description%", commandData.description)
                .replace("%options%", options)
    }

    override fun getCommandData(): CommandData {
        return CommandData("help", "Shows all available commands").apply {
            addOption(OptionType.STRING, "command", "The command to get help for")
        }
    }
}
