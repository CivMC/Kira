package net.civmc.kira.proximityChat.grouping

import net.civmc.kira.proximityChat.PlayerInfoService

/**
 * Type alias. TODO: Better description.
 */
typealias ProximityGroup = List<PlayerInfoService.PlayerInfo>

/**
 * This class is responsible for getting groups of players.
 * In the future, the strategy to group players may be better implemented
 */
class ProximityChatGroupService(
    private val playerInfoService: PlayerInfoService
) {

    private val groupingStrategy = QuadrantGroupingStrategy()

    fun getPlayerGroups(): List<ProximityGroup> {
        return groupingStrategy.groupPlayers(playerInfoService.getAllPlayerInfo())
    }
}
