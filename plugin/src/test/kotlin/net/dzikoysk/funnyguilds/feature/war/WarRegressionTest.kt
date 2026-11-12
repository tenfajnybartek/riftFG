package net.dzikoysk.funnyguilds.feature.war

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.data.DataPersistenceHandler
import net.dzikoysk.funnyguilds.event.SimpleEventHandler
import net.dzikoysk.funnyguilds.event.guild.GuildLivesChangeEvent
import net.dzikoysk.funnyguilds.guild.Guild
import net.dzikoysk.funnyguilds.shared.bukkit.FunnyServer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.bukkit.scheduler.BukkitScheduler
import net.dzikoysk.funnyguilds.user.FakeUserProfile
import panda.std.Option
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class WarRegressionTest : FunnyGuildsSpec() {
    @Test
    fun `packet bursts enqueue one action per kind without accessing world services on the network thread`() {
        val user = userManager.createFake(UUID.randomUUID(), "Attacker", FakeUserProfile.online())
        val scheduler = mock(BukkitScheduler::class.java)
        `when`(Bukkit.getScheduler()).thenReturn(scheduler)
        `when`(funnyGuilds.isEnabled).thenReturn(true)
        val callbacks = WarPacketCallbacks(funnyGuilds, user)
        repeat(10) {
            callbacks.handleAttackEntity(123, true)
            callbacks.handleRightClickEntity(123, true)
        }
        verify(scheduler, times(2)).runTask(eq(funnyGuilds), any(Runnable::class.java))
        verify(funnyGuilds, never()).guildEntityHelper
    }
    private fun scenario(victimLives: Int): Triple<Player, Guild, Guild> {
        config.regionsEnabled = false
        config.war.requireDeclaration = false
        val user = userManager.createFake(UUID.randomUUID(), "Attacker")
        val victim = userManager.createFake(UUID.randomUUID(), "Defender")
        val attacker = Guild("Attackers", "ATK").apply { owner = user; lives = 2 }
        val defender = Guild("Defenders", "DEF").apply {
            owner = victim
            lives = victimLives
            heartLives = 1
            born = Instant.now().minus(Duration.ofDays(7))
            protection = Instant.now().minusSeconds(60)
        }
        user.setGuild(attacker)
        victim.setGuild(defender)
        guildManager.addGuild(attacker)
        guildManager.addGuild(defender)
        val player = mock(Player::class.java)
        `when`(player.uniqueId).thenReturn(user.uuid)
        `when`(player.isOnline).thenReturn(true)
        val server = mock(FunnyServer::class.java)
        lenient().`when`(server.getPlayer(user)).thenReturn(Option.of(player))
        lenient().`when`(funnyGuilds.funnyServer).thenReturn(server)
        lenient().`when`(funnyGuilds.individualNameTagManager).thenReturn(Option.none())
        lenient().`when`(funnyGuilds.dataPersistenceHandler).thenReturn(mock(DataPersistenceHandler::class.java))
        `when`(Bukkit.isPrimaryThread()).thenReturn(true)
        return Triple(player, attacker, defender)
    }

    @Test
    fun `cancelled life loss changes neither protection nor rewards`() {
        val (player, attacker, defender) = scenario(2)
        val before = defender.protection
        mockStatic(SimpleEventHandler::class.java).use { events ->
            events.`when`<Boolean> { SimpleEventHandler.handle(any()) }.thenAnswer {
                it.getArgument<Any>(0) !is GuildLivesChangeEvent
            }
            WarSystem().attack(player, defender)
        }
        assertEquals(2, defender.lives)
        assertEquals(1, defender.heartLives)
        assertEquals(before, defender.protection)
        assertEquals(2, attacker.lives)
    }

    @Test
    fun `war win cannot reward farming on a one member victim guild`() {
        val (player, attacker, defender) = scenario(1)
        config.war.antiFarming.minVictimMembers = 2
        mockStatic(SimpleEventHandler::class.java).use { events ->
            events.`when`<Boolean> { SimpleEventHandler.handle(any()) }.thenReturn(true)
            WarSystem().attack(player, defender)
        }
        assertEquals(2, attacker.lives)
        assertEquals(1, attacker.warsWon)
        assertEquals(1, guildManager.countGuilds())
    }
}
