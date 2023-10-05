package net.civmc.kira.command

import com.github.maxopoly.kira.KiraMain
import net.civmc.kira.command.user.HelpCommand
import net.civmc.kira.command.user.InviteCommand
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object CommandManager {

    // TODO: Dependency Injection
    private val logger = KiraMain.getInstance().logger
    private val userManager = KiraMain.getInstance().userManager
    private val configManager = KiraMain.getInstance().config
    private val jda = KiraMain.getInstance().jda

    // TODO: Move this to config
    val devMode = true

    val commands = listOf(
            HelpCommand(logger, userManager),
            InviteCommand(logger, userManager),
    )

    fun registerCommands() {
        // TODO: Handle error from updating commands
        jda.updateCommands()
                .addCommands(getGlobalCommands().map { it.getCommandData() })
                .queue()

        jda.getGuildById(configManager.serverID)!!.updateCommands()
                .addCommands(getGuildCommands().map { it.getCommandData() })
                .queue()

        jda.addEventListener(*commands.toTypedArray())
    }

    private fun getGlobalCommands(): List<Command> {
        if (devMode) {
            return emptyList()
        }

        return commands.filter { it.global }
    }

    private fun getGuildCommands(): List<Command> {
        if (devMode) {
            return commands;
        }

        return commands.filter { !it.global }
    }
}
