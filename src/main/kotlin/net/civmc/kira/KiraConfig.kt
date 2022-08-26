package net.civmc.kira

import com.github.maxopoly.kira.database.DBConnection
import com.rabbitmq.client.ConnectionFactory
import org.apache.logging.log4j.Logger

class KiraConfig(val logger: Logger) {

    fun getAPIInetAdress() = System.getenv("KIRA_API_ADDRESS") ?: "localhost"
    fun getAPIPort() = (System.getenv("KIRA_API_PORT") ?: "80").toInt()
    fun getAPIRate() = (System.getenv("KIRA_API_RATE") ?: "500ms").toInt()
    fun getAPISSLCertPassword() = System.getenv("KIRA_API_CERTPASSWORD") ?: null
    fun getAPISSLCertPath() = System.getenv("KIRA_API_CERTPATH") ?: null

    fun getAuthroleID() = (System.getenv("KIRA_DISCORD_AUTHROLEID") ?: "-1").toLong()
    fun getBotToken() = System.getenv("KIRA_DISCORD_TOKEN") ?: null
    fun getCommandPrefix() = System.getenv("KIRA_DISCORD_COMMANDPREFIX") ?: "!kira "
    fun getRelaySectionID() = (System.getenv("KIRA_DISCORD_RELAYSECTIONID") ?: "-1").toLong()
    fun getServerID() = (System.getenv("KIRA_DISCORD_SERVERID") ?: "-1").toLong()

    fun getConsoleForwardingMapping() = mapOf<String, Long>() // TODO

    fun getDatabase(): DBConnection {
        val user = System.getenv("KIRA_DB_USER")
        val password = System.getenv("KIRA_DB_PASSWORD")
        val host = System.getenv("KIRA_DB_HOST")
        val port = System.getenv("KIRA_DB_PORT").toInt()
        val database = System.getenv("KIRA_DB_DATABASE")

        return DBConnection(logger, user, password, host, port, database, 5, 10000, 600000, 1800000)
    }

    fun getIncomingQueueName() = System.getenv("KIRA_RABBIT_INCOMING") ?: "gateway-to-kira"
    fun getOutgoingQueueName() = System.getenv("KIRA_RABBIT_OUTGOING") ?: "kira-to-gateway"
    fun getRabbitConfig(): ConnectionFactory {
        return ConnectionFactory().apply {
            username = System.getenv("KIRA_RABBIT_USER")
            password = System.getenv("KIRA_RABBIT_PASSWORD")
            host = System.getenv("KIRA_RABBIT_HOST")
            port = System.getenv("KIRA_RABBIT_PORT").toInt()
        }
    }
}