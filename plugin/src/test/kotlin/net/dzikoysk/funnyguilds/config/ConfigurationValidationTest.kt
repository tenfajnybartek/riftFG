package net.dzikoysk.funnyguilds.config

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.config.tablist.TablistConfiguration
import net.dzikoysk.funnyguilds.config.sections.ScoreboardConfiguration
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertFailsWith

class ConfigurationValidationTest : FunnyGuildsSpec() {
    @Test
    fun `reload keeps section references used by hooks and updates private values`() {
        val current = config.scoreboard
        val original = current.nametag.prefix
        val candidate = ScoreboardConfiguration()
        candidate.nametag.prefix = ScoreboardConfiguration.NameTag.Value("", "changed")
        ConfigurationValidation.copyValues(candidate, current)
        assertSame(original, current.nametag.prefix)
        assertEquals("changed", original.ourGuild)
    }
    @Test
    fun `nested command changes require a restart but runtime values may change`() {
        val before = "commands:\n  create:\n    name: zaloz\ndata-interval: 1\n"
        ConfigurationValidation.requireReloadable(before, before.replace("data-interval: 1", "data-interval: 2"))
        val failure = assertFailsWith<IllegalArgumentException> {
            ConfigurationValidation.requireReloadable(before, before.replace("zaloz", "different"))
        }
        assertTrue(failure.message!!.contains("commands"))
    }
    @Test
    fun `inverted guild limits are rejected`() {
        config.war.maxLives = config.war.lives - 1
        assertFailsWith<IllegalArgumentException> { ConfigurationValidation.validate(config) }
    }

    @Test
    fun `empty animated pages are rejected before renderer creation`() {
        val tab = TablistConfiguration().apply { animated = true; pages = emptyList() }
        assertFailsWith<IllegalArgumentException> { ConfigurationValidation.validate(tab) }
    }

    @Test
    fun `out of range slots and textures are rejected`() {
        val tab = TablistConfiguration().apply { cells = mapOf(81 to Component.empty()) }
        assertFailsWith<IllegalArgumentException> { ConfigurationValidation.validate(tab) }
        tab.cells = mapOf(1 to Component.empty())
        tab.heads.textures = mapOf(NumberRange(0, 80) to tab.heads.textures.values.first())
        assertFailsWith<IllegalArgumentException> { ConfigurationValidation.validate(tab) }
    }
}
