package net.civmc.kira.command

import com.github.maxopoly.kira.KiraMain
import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger
import java.util.*

class HelpCommand(logger: Logger, userManager: UserManager) : Command(logger, userManager) {

    override val name = "help"

    // TODO: Clean up from direct move
    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier, options: List<OptionMapping>) {
        val cmdHandler = KiraMain.getInstance().commandHandler


        if (options.isEmpty()) {
            val commands: List<com.github.maxopoly.kira.command.model.discord.Command> = LinkedList(cmdHandler.allInputs)
            Collections.sort(commands) { o1, o2 -> o1.identifier.compareTo(o2.identifier) }

            val sb = StringBuilder()
            for (cmd in commands) {
                if (!sender.hasPermission(cmd.requiredPermission)) {
                    continue
                }
                sb.append(" - ${cmd.usage.split("\n")[0]}\n")
                sb.append("      ${cmd.functionality}\n")
            }
            event.reply(sb.toString()).queue()

        } else {
            val command = cmdHandler.getHandler(options[0].asString)

            if (command == null) {
                event.reply("The command " + options[0].asString + " is not known").queue()
            } else {
                event.reply("""
                    ${command.functionality}
                    ${command.usage}
                """.trimIndent()).queue()
            }
        }
    }

    override fun getCommandData(): CommandData {
        return CommandData("help", "Shows all available commands").apply {
            addOption(OptionType.STRING, "command", "The command to get help for")
        }
    }
}