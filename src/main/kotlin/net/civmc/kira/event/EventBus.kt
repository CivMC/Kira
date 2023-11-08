package net.civmc.kira.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

open class Event

class EventBus {

    private val eventBus = MutableSharedFlow<Event>()

    fun subscribe(scope: CoroutineScope, block: suspend (Event) -> Unit) {
        eventBus.onEach(block).launchIn(scope)
    }

    suspend fun emit(event: Event) {
        eventBus.emit(event)
    }
}
