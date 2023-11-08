package net.civmc.kira.proximityChat

import net.civmc.kira.proximityChat.grouping.ProximityChatGroupService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proximityChatModule = module {
    singleOf(::PlayerInfoService)
    singleOf(::ProximityChatListener)
    singleOf(::ProximityChatChannelManager)
    singleOf(::ProximityChatGroupService)
}
