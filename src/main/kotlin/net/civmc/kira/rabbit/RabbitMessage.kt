package net.civmc.kira.rabbit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class RabbitMessage

/**
 * Rabbit message fired when a player location is updated
 */
@Serializable
@SerialName("playerupdatelocation")
data class PlayerUpdateLocationRabbitMessage(
    val name: String,
    val dimension: String,
    val x: Int,
    val y: Int,
    val z: Int,
) : RabbitMessage()

/**
 * Rabbit message fired when a player logs off
 */
@Serializable
@SerialName("playerlogoff")
data class PlayerLogOffRabbitMessage(
        val name: String
): RabbitMessage()
