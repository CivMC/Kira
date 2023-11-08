package net.civmc.kira.rabbit

import com.rabbitmq.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.civmc.kira.config.RabbitConfigSection
import net.civmc.kira.event.Event
import net.civmc.kira.event.EventBus

val serializeModule = SerializersModule {
    polymorphic(RabbitMessage::class) {
        subclass(PlayerUpdateLocationRabbitMessage::class)
        subclass(PlayerLogOffRabbitMessage::class)
        defaultDeserializer { RabbitMessage.serializer() }
    }
}

val format = Json { serializersModule = serializeModule }

data class RabbitMessageEvent(val message: RabbitMessage) : Event()

class RabbitService(
        private val rabbitConfig: RabbitConfigSection,
        private val eventBus: EventBus,
) {

    // TODO: Connection Durability
    private val connection = ConnectionFactory().apply {
        username = rabbitConfig.username
        password = rabbitConfig.password
        host = rabbitConfig.host
        port = rabbitConfig.port
    }.newConnection()

    private val incomingChannel = connection.createChannel()
    private val outgoingChannel = connection.createChannel()

    init {
        incomingChannel.queueDeclare("gateway-to-kira-v2", false, false, false, null)
        outgoingChannel.queueDeclare("kira-to-gateway-v2", false, false, false, null)

        incomingChannel.basicConsume("gateway-to-kira-v2", true, object : DefaultConsumer(incomingChannel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                try {
                    handleMessage(String(body))
                } catch (e: Exception) {
                    println(e)
                }
            }
        })
    }

    fun sendMessage() {
        // TODO
        outgoingChannel.basicPublish("", "kira-to-gateway-v2", null, null)
    }

    private fun handleMessage(message: String) {
        val data = format.decodeFromString<RabbitMessage>(message)

        GlobalScope.launch {
            eventBus.emit(RabbitMessageEvent(data))
        }
    }
}
