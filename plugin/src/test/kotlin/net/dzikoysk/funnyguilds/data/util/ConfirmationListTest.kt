package net.dzikoysk.funnyguilds.data.util

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfirmationListTest {
    @Test
    fun `confirmation is restricted to its guild and expires`() {
        val player = UUID.randomUUID()
        val guild = UUID.randomUUID()
        ConfirmationList.add(player, guild)
        assertTrue(ConfirmationList.contains(player, guild))
        assertFalse(ConfirmationList.contains(player, UUID.randomUUID()))
        ConfirmationList.add(player, guild, Instant.now().minusSeconds(61))
        assertFalse(ConfirmationList.contains(player, guild))
        ConfirmationList.remove(player)
    }
}
