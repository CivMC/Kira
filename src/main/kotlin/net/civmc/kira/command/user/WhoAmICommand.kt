package net.civmc.kira.command.user

import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.apache.logging.log4j.Logger

class WhoAmICommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name ="whoami"

    override fun dispatchCommand(event: SlashCommandInteractionEvent, sender: InputSupplier) {
        event.reply("Your details are as follows: ```json\n${sender.identifier}\n```").queue()
    }

    override fun getCommandData(): SlashCommandData {
        return Commands.slash("whoami", "Shows your linked accounts")
    }
}
