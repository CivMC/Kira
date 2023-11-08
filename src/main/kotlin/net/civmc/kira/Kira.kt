package net.civmc.kira

import com.github.maxopoly.kira.ConfigManager
import com.github.maxopoly.kira.api.APISessionManager
import com.github.maxopoly.kira.command.model.discord.CommandHandler
import com.github.maxopoly.kira.command.model.discord.CommandLineInputSupplier
import com.github.maxopoly.kira.database.DAO
import com.github.maxopoly.kira.listener.DiscordMessageListener
import com.github.maxopoly.kira.permission.KiraRoleManager
import com.github.maxopoly.kira.rabbit.MinecraftRabbitGateway
import com.github.maxopoly.kira.rabbit.RabbitHandler
import com.github.maxopoly.kira.rabbit.session.RequestSessionManager
import com.github.maxopoly.kira.relay.GroupChatManager
import com.github.maxopoly.kira.relay.RelayConfigManager
import com.github.maxopoly.kira.user.AuthManager
import com.github.maxopoly.kira.user.DiscordRoleManager
import com.github.maxopoly.kira.user.UserManager
import net.civmc.kira.command.CommandManager.registerCommands
import net.civmc.kira.config.configModule
import net.civmc.kira.event.eventModule
import net.civmc.kira.proximityChat.ProximityChatListener
import net.civmc.kira.proximityChat.proximityChatModule
import net.civmc.kira.rabbit.rabbitModule
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import org.apache.logging.log4j.LogManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*
import javax.security.auth.login.LoginException

class Kira : KoinComponent {

    val proximityChatListener: ProximityChatListener by inject()

    val logger = LogManager.getLogger("Main")

    var shutdown = false
    var guildId: Long? = null
    val guild: Guild?
        get() = jda!!.getGuildById(guildId!!)

    // Immediate DI possiblity
    var jda: JDA? = null
    var commandHandler: CommandHandler? = null
    var userManager: UserManager? = null
    var config: ConfigManager? = null
    var discordRoleManager: DiscordRoleManager? = null
    var dao: DAO? = null
    var rabbit: RabbitHandler? = null
    var mcRabbitGateway: MinecraftRabbitGateway? = null
    var authManager: AuthManager? = null
    var kiraRoleManager: KiraRoleManager? = null
    var groupChatManager: GroupChatManager? = null
    var relayConfigManager: RelayConfigManager? = null
    var requestSessionManager: RequestSessionManager? = null
    var apiSessionManager: APISessionManager? = null

    fun start() {
        instance = this
        if (!instance!!.loadConfig()) {
            return
        }
        instance!!.authManager = AuthManager()
        instance!!.userManager = UserManager(instance!!.logger)
        if (!instance!!.loadDatabase()) {
            return
        }
        if (!instance!!.loadPermission()) {
            return
        } else {
            instance!!.kiraRoleManager!!.setupDefaultPermissions()
        }
        if (!instance!!.startJDA()) {
            return
        }
        if (!instance!!.setupAuthManager()) {
            return
        }
        if (!instance!!.startRabbit()) {
            return
        }
        instance!!.commandHandler = CommandHandler(instance!!.logger)
        if (!instance!!.loadGroupChats()) {
            return
        }
        if (!instance!!.setupListeners()) {
            return
        }
        registerCommands()

        instance!!.apiSessionManager = APISessionManager(instance!!.logger, 500)
        instance!!.rabbit!!.beginAsyncListen()
        instance!!.parseInput()
    }

    private fun loadConfig(): Boolean {
        config = ConfigManager(logger)
        return config!!.reload()
    }

    private fun loadDatabase(): Boolean {
        val dbConn = config!!.database ?: return false
        dao = DAO(dbConn, logger)
        for (user in dao!!.loadUsers()) {
            userManager!!.addUser(user)
        }
        return true
    }

    private fun loadGroupChats(): Boolean {
        if (config!!.relaySectionID == -1L) {
            return false
        }
        relayConfigManager = RelayConfigManager(dao)
        groupChatManager = GroupChatManager(logger, dao, config!!.relaySectionID, relayConfigManager)
        return true
    }

    private fun loadPermission(): Boolean {
        kiraRoleManager = dao!!.loadAllRoles()
        return kiraRoleManager != null
    }

    private fun parseInput() {
        val c = System.console()
        var scanner: Scanner? = null
        if (c == null) {
            logger.warn("System console not detected, using scanner as fallback behavior")
            scanner = Scanner(System.`in`)
        }
        while (!shutdown) {
            var msg: String?
            msg = if (c == null) {
                scanner!!.nextLine()
            } else {
                c.readLine("")
            }
            if (msg == null) {
                continue
            }
            commandHandler!!.handle(msg, CommandLineInputSupplier())
        }
    }

    private fun setupAuthManager(): Boolean {
        discordRoleManager = DiscordRoleManager(config!!.authroleID, logger, userManager)
        return true
    }

    private fun setupListeners(): Boolean {
        jda?.addEventListener(
            proximityChatListener,
            DiscordMessageListener(commandHandler, logger, userManager, jda!!.selfUser.idLong)
        )
        return true
    }

    private fun startJDA(): Boolean {
        val token = config!!.botToken
        if (token == null) {
            logger.error("No bot token was supplied")
            return false
        }
        try {
            jda = JDABuilder.create(
                token, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_INVITES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES
            ).build()
            jda!!.awaitReady()
        } catch (e: LoginException) {
            logger.error("Failed to start jda", e)
            return false
        } catch (e: InterruptedException) {
            logger.error("Failed to start jda", e)
            return false
        }
        val serverID = config!!.serverID
        if (serverID == -1L) {
            logger.error("No server id was provided")
            return false
        }
        val guild = jda!!.getGuildById(serverID)
        if (guild == null) {
            logger.error("No guild with the provided id $serverID could be found")
            return false
        }
        guildId = serverID
        val authID = config!!.authroleID
        if (authID == -1L) {
            logger.error("No auth role id was provided")
            return false
        }
        val authRole = guild.getRoleById(authID)
        if (authRole == null) {
            logger.error("No auth role with the provided id $authID could be found")
            return false
        }
        return true
    }

    private fun startRabbit(): Boolean {
        val incomingQueue = config!!.incomingQueueName
        val outgoingQueue = config!!.outgoingQueueName
        val connFac = config!!.rabbitConfig
        if (incomingQueue == null || outgoingQueue == null || connFac == null) {
            return false
        }
        rabbit = RabbitHandler(connFac, incomingQueue, outgoingQueue, logger)
        if (!rabbit!!.setup()) {
            return false
        }
        mcRabbitGateway = MinecraftRabbitGateway(rabbit)
        requestSessionManager = RequestSessionManager(rabbit, logger)
        return true
    }

    fun stop() {
        rabbit!!.shutdown()
        apiSessionManager!!.shutdown()
        shutdown = true
        Thread(object : Runnable {
            override fun run() {
                try {
                    synchronized(this) { (this as Object).wait(2000) }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                System.exit(0)
            }
        }).start()
    }

    // Temporary Companion Object to bridge old Java functionality,
    // Once everything has been DI'd we can remove this.
    companion object {
        var instance: Kira? = null
    }

}

fun main() {
    startKoin {
        printLogger(Level.INFO)
         modules(
             configModule,
             eventModule,
             rabbitModule,
             proximityChatModule,
        )
    }

    val kira = Kira()

    kira.start()
}