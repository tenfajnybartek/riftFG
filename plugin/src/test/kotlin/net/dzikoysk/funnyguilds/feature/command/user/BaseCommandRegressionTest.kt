package net.dzikoysk.funnyguilds.feature.command.user

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.guild.Guild
import net.dzikoysk.funnyguilds.shared.bukkit.ItemUtils
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Duration
import java.util.UUID

class BaseCommandRegressionTest : FunnyGuildsSpec() {
    @Test
    fun `a teleport rejected by another plugin returns the charged cost exactly once`() {
        config.baseDelay = Duration.ZERO
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val world = mock(World::class.java)
        val home = Location(world, 1.0, 70.0, 1.0)
        val guild = Guild("Guild", "TAG").apply { owner = user; setHome(home) }
        user.setGuild(guild)
        guildManager.addGuild(guild)
        val player = mock(Player::class.java)
        `when`(player.isOnline).thenReturn(true)
        `when`(player.teleport(home)).thenReturn(false)
        val cost = listOf(mock(ItemStack::class.java))
        config.baseItems = cost
        val array = cost.toTypedArray()
        mockedItemUtils.`when`<Boolean> { ItemUtils.playerHasEnoughItems(eq(player), eq(cost), any()) }.thenReturn(true)
        mockedItemUtils.`when`<Boolean> { ItemUtils.takeItems(player, cost) }.thenReturn(true)
        mockedItemUtils.`when`<Array<ItemStack>> { ItemUtils.toArray(cost) }.thenReturn(array)
        val command = BaseCommand().apply {
            config = this@BaseCommandRegressionTest.config
            guildManager = this@BaseCommandRegressionTest.guildManager
            messageService = this@BaseCommandRegressionTest.messageService
        }
        command.execute(player, user, guild)
        verify(player).teleport(home)
        mockedItemUtils.verify { ItemUtils.returnItems(player, array) }
    }
}
