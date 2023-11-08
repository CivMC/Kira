package net.civmc.kira.proximityChat.grouping

import net.civmc.kira.proximityChat.PlayerInfoService

interface GroupingStrategy {
    fun groupPlayers(players: List<PlayerInfoService.PlayerInfo>): List<ProximityGroup>
}

/**
 * Rudimentary grouping strategy for first implementation attempt.
 * 0,0 belongs to ++
 */
class QuadrantGroupingStrategy : GroupingStrategy {

    // TODO: Handle Dimension
    override fun groupPlayers(players: List<PlayerInfoService.PlayerInfo>): List<ProximityGroup> {
        return listOf(
            // ++
            players.filter { it.location.x >= 0 && it.location.z >= 0 },
            // --
            players.filter { it.location.x < 0 && it.location.z < 0 },
            // -+
            players.filter { it.location.x > 0 && it.location.z < 0 },
            // +-
            players.filter { it.location.x < 0 && it.location.z > 0 },
        )
    }
}