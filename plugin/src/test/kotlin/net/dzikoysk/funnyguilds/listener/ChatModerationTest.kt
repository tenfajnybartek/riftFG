package net.dzikoysk.funnyguilds.listener

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.event.SimpleEventHandler
import net.dzikoysk.funnyguilds.event.guild.GuildChatEvent
import net.dzikoysk.funnyguilds.event.guild.GuildPreChatEvent
import net.dzikoysk.funnyguilds.guild.Guild
import net.dzikoysk.funnyguilds.guild.permission.GuildPermissionChecker
import net.dzikoysk.funnyguilds.user.FakeUserProfile
import net.dzikoysk.funnyguilds.user.User
import net.dzikoysk.funnyguilds.user.UserUtils
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import panda.std.Option
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatModerationTest : FunnyGuildsSpec() {
    @Test
    fun `moderated pre event text is sent to members and included in the final event`() {
        val sent = mutableListOf<Component>()
        val receiver = userManager.createFake(UUID.randomUUID(), "Receiver", object : FakeUserProfile(true, false, 0) {
            override fun sendMessage(message: Component) { sent.add(message) }
        })
        val sender = userManager.createFake(UUID.randomUUID(), "Sender")
        val guild = Guild("Guild", "TAG").apply { owner = sender; addMember(receiver) }
        val player = mock(Player::class.java)
        `when`(player.name).thenReturn("Sender")
        val checker = mock(GuildPermissionChecker::class.java)
        `when`(checker.handlePermission(any(), any(), any())).thenReturn(true)
        `when`(checker.getPermissionValue<Boolean>(any(), any(), any())).thenReturn(Option.of(true))
        val listener = PlayerChat().apply {
            config = this@ChatModerationTest.config
            messageService = this@ChatModerationTest.messageService
            userManager = this@ChatModerationTest.userManager
        }
        PlayerChat::class.java.getDeclaredField("permissionChecker").apply { isAccessible = true }.set(listener, checker)
        val method = PlayerChat::class.java.getDeclaredMethod("sendMessageToGuilds", User::class.java, Player::class.java, Guild::class.java,
            Component::class.java, String::class.java, Component::class.java, Set::class.java, GuildChatEvent.Type::class.java)
        method.isAccessible = true
        val moderated = Component.text("redacted")
        var finalText: Component? = null
        mockStatic(UserUtils::class.java).use { utils ->
            utils.`when`<Component> { UserUtils.getUserPosition(checker, sender) }.thenReturn(Component.empty())
            mockStatic(SimpleEventHandler::class.java).use { events ->
                events.`when`<Boolean> { SimpleEventHandler.handle(any()) }.thenAnswer {
                    when (val event = it.getArgument<Any>(0)) {
                        is GuildPreChatEvent -> event.formattedMessage = moderated
                        is GuildChatEvent -> finalText = event.formattedMessage
                    }
                    true
                }
                assertTrue(method.invoke(listener, sender, player, guild, Component.text("{MESSAGE}"), "!", Component.text("!original"), setOf(guild), GuildChatEvent.Type.PRIVATE) as Boolean)
            }
        }
        assertEquals<List<Component>>(listOf(moderated), sent)
        assertEquals(moderated, finalText)
    }
}
