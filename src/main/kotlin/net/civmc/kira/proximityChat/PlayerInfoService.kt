package net.civmc.kira.proximityChat

/**
 * This service holds up-to-date information on in-game players.
 */
class PlayerInfoService {

    data class PlayerLocation(
        val dimension: String,
        val x: Int,
        val y: Int,
        val z: Int
    )

    // TODO: Use Kira User?
    data class PlayerInfo(
        val name: String,

        val location: PlayerLocation
    )

    private val playerInfo = mutableMapOf<String, PlayerInfo>()

    fun updatePlayer(info: PlayerInfo) {
        playerInfo[info.name] = info
    }

    fun removePlayer(name: String) {
        playerInfo.remove(name)
    }

    fun getPlayerInfo(name: String): PlayerInfo? {
        return playerInfo[name]
    }

    fun getAllPlayerInfo(): List<PlayerInfo> {
        return playerInfo.values.toMutableList()
    }
}