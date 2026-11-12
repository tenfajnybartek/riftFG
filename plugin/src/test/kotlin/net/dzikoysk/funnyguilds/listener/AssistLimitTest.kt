package net.dzikoysk.funnyguilds.listener

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.damage.DamageState
import net.dzikoysk.funnyguilds.event.SimpleEventHandler
import net.dzikoysk.funnyguilds.rank.RankSystem
import net.dzikoysk.funnyguilds.user.User
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import java.util.UUID
import kotlin.test.assertEquals

class AssistLimitTest : FunnyGuildsSpec() {
    @Test
    fun `limited assists select highest damage even when hash order differs`() {
        config.assistsLimit = 1
        val high = userManager.createFake(UUID(0, 15), "Highest")
        val low = userManager.createFake(UUID(0, 0), "Lowest")
        val killer = userManager.createFake(UUID(0, 1), "Killer")
        val victim = userManager.createFake(UUID(0, 2), "Victim")
        val damage = mock(DamageState::class.java)
        `when`(damage.sortedTotalDamageMap).thenReturn(linkedMapOf(high to 80.0, low to 10.0, killer to 10.0))
        `when`(damage.totalDamage).thenReturn(100.0)
        val listener = PlayerDeath(config).apply { config = this@AssistLimitTest.config }
        val method = PlayerDeath::class.java.getDeclaredMethod("handleAssists", User::class.java, DamageState::class.java, User::class.java, RankSystem.RankResult::class.java)
        method.isAccessible = true
        mockStatic(SimpleEventHandler::class.java).use { events ->
            events.`when`<Boolean> { SimpleEventHandler.handle(any()) }.thenReturn(true)
            val result = method.invoke(listener, victim, damage, killer, RankSystem.RankResult(100)) as Map<*, *>
            assertEquals(setOf(high), result.keys)
        }
        assertEquals(1, high.rank.assists)
        assertEquals(0, low.rank.assists)
    }
}
