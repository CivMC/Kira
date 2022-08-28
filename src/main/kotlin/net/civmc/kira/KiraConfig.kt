package net.civmc.kira

import com.github.maxopoly.kira.database.DBConnection
import com.rabbitmq.client.ConnectionFactory
import org.apache.logging.log4j.Logger
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

class KiraConfig(val logger: Logger) {

    val apiAddress by EnvConfig("KIRA_API_ADDRESS", default = "localhost", required = true)
    val apiPort by EnvConfig("KIRA_API_PORT", default = "80", required = true)
    val apiRate by EnvConfig("KIRA_API_RATE", default = "500ms")
    val apiSslCertPassword by EnvConfig("KIRA_API_CERT_PASSWORD")
    val apiSslCertPath by EnvConfig("KIRA_API_CERTPATH")

    val discordAuthRoleId by EnvConfig("KIRA_DISCORD_AUTHROLEID", required = true)
    val discordBotToken by EnvConfig("KIRA_DISCORD_TOKEN", required = true)
    val discordCommandPrefix by EnvConfig("KIRA_DISCORD_COMMANDPREFIX", default = "!kira ", required = true)
    val discordRelaySectionId by EnvConfig("KIRA_DISCORD_RELAYSECTIONID", required = true)
    val discordServerId by EnvConfig("KIRA_DISCORD_SERVERID", required = true)

    val consoleForwardingMapping = mapOf<String, Long>()  // TODO

    val rabbitIncomingQueueName by EnvConfig("KIRA_RABBIT_INCOMING", default = "gateway-to-kira", required = true)
    val rabbitOutgoingQueueName by EnvConfig("KIRA_RABBIT_OUTGOING", default = "kira-to-gateway", required = true)

    fun getDatabase(): DBConnection {
        val user = System.getenv("KIRA_DB_USER")
        val password = System.getenv("KIRA_DB_PASSWORD")
        val host = System.getenv("KIRA_DB_HOST")
        val port = System.getenv("KIRA_DB_PORT").toInt()
        val database = System.getenv("KIRA_DB_DATABASE")

        return DBConnection(logger, user, password, host, port, database, 5, 10000, 600000, 1800000)
    }

    fun getRabbitConfig(): ConnectionFactory {
        return ConnectionFactory().apply {
            username = System.getenv("KIRA_RABBIT_USER")
            password = System.getenv("KIRA_RABBIT_PASSWORD")
            host = System.getenv("KIRA_RABBIT_HOST")
            port = System.getenv("KIRA_RABBIT_PORT").toInt()
        }
    }

    class EnvConfig(
            val envVar: String,
            val default: String? = null,
            val required: Boolean = false
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
            val value = System.getenv(envVar) ?: default

            if (value.isNullOrEmpty() && required) {
                throw IllegalStateException("Environment variable $envVar is required, but not set.")
            }

            return value
        }
    }

    class Default
}
