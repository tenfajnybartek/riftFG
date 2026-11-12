package net.dzikoysk.funnyguilds.feature.command.user

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.config.sections.BuyLifeConfiguration.BuyLifePaymentType
import net.dzikoysk.funnyguilds.event.SimpleEventHandler
import net.dzikoysk.funnyguilds.feature.hooks.vault.VaultHook
import net.dzikoysk.funnyguilds.guild.Guild
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuyLifeRegressionTest : FunnyGuildsSpec() {
    @Test
    fun `failed Vault withdrawal never charges items or grants a life`() {
        config.buyLife.paymentType = BuyLifePaymentType.MONEY
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val guild = Guild("Guild", "TAG").apply { owner = user; lives = 2 }
        user.setGuild(guild)
        guildManager.addGuild(guild)
        val player = mock(Player::class.java)
        val inventory = mock(PlayerInventory::class.java)
        lenient().`when`(player.inventory).thenReturn(inventory)
        `when`(player.isOnline).thenReturn(true)
        val command = BuyLifeCommand().apply {
            config = this@BuyLifeRegressionTest.config
            messageService = this@BuyLifeRegressionTest.messageService
            guildManager = this@BuyLifeRegressionTest.guildManager
        }
        mockStatic(VaultHook::class.java).use { vault ->
            vault.`when`<Boolean> { VaultHook.isEconomyHooked() }.thenReturn(true)
            vault.`when`<Boolean> { VaultHook.canAfford(player, config.buyLife.money) }.thenReturn(true)
            vault.`when`<EconomyResponse> { VaultHook.withdrawFromPlayerBank(player, config.buyLife.money) }
                .thenReturn(EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "denied"))
            mockStatic(SimpleEventHandler::class.java).use { events ->
                events.`when`<Boolean> { SimpleEventHandler.handle(any()) }.thenReturn(true)
                command.execute(player, user, guild)
            }
        }
        assertEquals(2, guild.lives)
        assertTrue(guild.lastLifeBuy.isEmpty)
        verifyNoInteractions(inventory)
    }

    @Test
    fun `missing economy provider does not call Vault balance or withdrawal`() {
        config.buyLife.paymentType = BuyLifePaymentType.MONEY
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val guild = Guild("Guild", "TAG").apply { owner = user; lives = 2 }
        val player = mock(Player::class.java)
        val command = BuyLifeCommand().apply {
            config = this@BuyLifeRegressionTest.config
            messageService = this@BuyLifeRegressionTest.messageService
        }
        mockStatic(VaultHook::class.java).use { vault ->
            vault.`when`<Boolean> { VaultHook.isEconomyHooked() }.thenReturn(false)
            command.execute(player, user, guild)
            vault.verify { VaultHook.isEconomyHooked() }
            vault.verifyNoMoreInteractions()
        }
        assertEquals(2, guild.lives)
    }
}
