package net.civmc.kira.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource


data class Config(
    val rabbit: RabbitConfigSection,
    val proximityChat: ProximityChatConfigSection,
)

data class ProximityChatConfigSection(
    val enabled: Boolean,
    val categoryId: String,
    val lobbyChannelId: String,
)

data class RabbitConfigSection(
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
)

class ConfigService {

    val config = ConfigLoaderBuilder.default()
        .addFileSource("config.yaml") // TODO
        .build()
        .loadConfigOrThrow<Config>()
}
