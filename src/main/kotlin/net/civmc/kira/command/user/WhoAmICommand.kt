package net.civmc.kira.command.user

import com.github.maxopoly.kira.command.model.top.InputSupplier
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.Command
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.apache.logging.log4j.Logger

class WhoAmICommand(logger: Logger, userManager: UserManager): Command(logger, userManager) {

    override val name ="whoami"

    override fun dispatchCommand(event: SlashCommandEvent, sender: InputSupplier) {
        event.reply("Your details are as follows: ```json\n${sender.identifier}\n```").queue()
    }

    override fun getCommandData(): CommandData {
        return CommandData("whoami", "Shows your linked accounts")
    }
}