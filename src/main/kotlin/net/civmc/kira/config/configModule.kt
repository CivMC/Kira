package net.civmc.kira.config

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val configModule = module {
    singleOf(::ConfigService)

    single<RabbitConfigSection> {
        get<ConfigService>().config.rabbit
    }

    single<ProximityChatConfigSection> {
        get<ConfigService>().config.proximityChat
    }
}
