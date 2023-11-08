package net.civmc.kira.event

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val eventModule = module {
    singleOf(::EventBus)
}
