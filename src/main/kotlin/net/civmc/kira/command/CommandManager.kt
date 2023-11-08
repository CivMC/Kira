package net.civmc.kira.command

import net.civmc.kira.Kira
import net.civmc.kira.command.user.*
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import kotlin.math.log

object CommandManager {

    // TODO: Dependency Injection
    private val logger = Kira.instance!!.logger
    private val userManager = Kira.instance!!.userManager
    private val configManager = Kira.instance!!.config
    private val jda = Kira.instance!!.jda

    // TODO: Move this to config
    val devMode = false

    val commands = listOf(
            // Admin Commands
            // TODO
            // User Commands
            AuthCommand(logger, userManager!!),
            HelpCommand(logger, userManager),
            IngameCommand(logger, userManager),
            InviteCommand(logger, userManager),
            UpdateRolesCommand(logger, userManager),
            WhoAmICommand(logger, userManager),
            // Relay Commands
            // TODO
            // API Commands
            // TODO
    )

    fun registerCommands() {
        // TODO: Handle error from updating commands
        jda!!.updateCommands()
                .addCommands(getGlobalCommands().map { it.getCommandData() })
                .queue()

        jda.getGuildById(configManager!!.serverID)!!.updateCommands()
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
