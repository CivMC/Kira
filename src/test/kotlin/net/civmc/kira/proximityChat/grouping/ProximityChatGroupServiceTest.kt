package net.civmc.kira.proximityChat.grouping

import net.civmc.kira.proximityChat.PlayerInfoService
import net.civmc.kira.proximityChat.proximityChatModule
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.expect

/**
 * This test class is still hideous, but I don't care right now.
 * I'm only testing the quadrant method which will get axed later, but it's
 * currently the easiest method to test and debug
 */
class ProximityChatGroupServiceTest : KoinTest {

    private val proximityChatGoupService by inject<ProximityChatGroupService>()
    private val playerInfoService by inject<PlayerInfoService>()

    private val centeredZeroZeroPlayer = PlayerInfoService.PlayerInfo(
        "Player0",
        PlayerInfoService.PlayerLocation("overworld", 0, 1, 0)
    )

    private val centeredPlusPlusPlayer = PlayerInfoService.PlayerInfo(
        "Player1",
        PlayerInfoService.PlayerLocation("overworld", 1, 1, 1)
    )

    private val centeredMinusMinusPlayer = PlayerInfoService.PlayerInfo(
        "Player2",
        PlayerInfoService.PlayerLocation("overworld", -1, 1, -1)
    )

    private val centeredPlusMinusPlayer = PlayerInfoService.PlayerInfo(
        "Player3",
        PlayerInfoService.PlayerLocation("overworld", 1, 1, -1)
    )

    private val centeredMinusPlusPlayer = PlayerInfoService.PlayerInfo(
        "Player4",
        PlayerInfoService.PlayerLocation("overworld", -1, 1, 1)
    )

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(proximityChatModule)
    }

    @Nested
    inner class QuadrantGroupingStrategy {

        fun `Players are properly grouped into their quadrant`() {
            playerInfoService.updatePlayer(centeredZeroZeroPlayer)
            playerInfoService.updatePlayer(centeredPlusPlusPlayer)
            playerInfoService.updatePlayer(centeredMinusMinusPlayer)
            playerInfoService.updatePlayer(centeredPlusMinusPlayer)
            playerInfoService.updatePlayer(centeredMinusPlusPlayer)

//            Assertions.assertEquals(listOf(
////                ProximityChatGroupService.Group(listOf(centeredZeroZeroPlayer, centeredPlusPlusPlayer)),
////                ProximityChatGroupService.Group(listOf(centeredMinusMinusPlayer)),
////                ProximityChatGroupService.Group(listOf(centeredPlusMinusPlayer)),
////                ProximityChatGroupService.Group(listOf(centeredMinusPlusPlayer)),
//            ), proximityChatGoupService.getPlayerGroups())

            println(proximityChatGoupService.getPlayerGroups())
        }
    }
}
