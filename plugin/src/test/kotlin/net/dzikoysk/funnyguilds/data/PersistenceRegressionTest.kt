package net.dzikoysk.funnyguilds.data

import net.dzikoysk.funnyguilds.data.util.AtomicFiles
import net.dzikoysk.funnyguilds.data.util.YamlWrapper
import net.dzikoysk.funnyguilds.guild.Guild
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistenceRegressionTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `saving an older revision must not clear newer changes`() {
        val guild = Guild("Guild", "TAG")
        val captured = guild.revision
        guild.lives = 4
        guild.markSaved(captured)
        assertTrue(guild.wasChanged())
        guild.markSaved(guild.revision)
        assertFalse(guild.wasChanged())
        guild.markSaved(captured)
        assertFalse(guild.wasChanged(), "an out of order acknowledgement cannot move the saved revision backwards")
    }

    @Test
    fun `failed deletion is retried before a later save and close drains writes`() {
        val available = AtomicBoolean(false)
        val completed = mutableListOf<String>()
        PersistenceCoordinator().use { coordinator ->
            val failed = coordinator.submit {
                check(available.get())
                completed.add("delete")
            }
            assertFailsWith<CompletionException> { failed.join() }
            available.set(true)
            coordinator.submit { completed.add("save") }.join()
            coordinator.submit { completed.add("final") }
        }
        assertEquals(listOf("delete", "save", "final"), completed)
    }

    @Test
    fun `YAML snapshot writes captured data and acknowledges only after success`() {
        val target = directory.resolve("user.yml")
        val yaml = YamlWrapper(target.toFile(), false)
        yaml.set("points", 12)
        var acknowledged = false
        val snapshot = yaml.snapshot { acknowledged = true }
        yaml.set("points", 99)
        assertFalse(acknowledged)
        snapshot.run()
        assertTrue(acknowledged)
        assertEquals(12, YamlWrapper(target.toFile()).getInt("points"))
    }

    @Test
    fun `failed write does not acknowledge a revision or leave a temporary file`() {
        val parent = directory.resolve("blocked")
        val yaml = YamlWrapper(parent.resolve("user.yml").toFile(), false)
        var acknowledged = false
        val snapshot = yaml.snapshot { acknowledged = true }
        Files.writeString(parent, "not a directory")
        assertFailsWith<java.io.UncheckedIOException> { snapshot.run() }
        assertFalse(acknowledged)
        assertEquals("not a directory", Files.readString(parent))
    }

    @Test
    fun `atomic replacement preserves previous valid content as backup`() {
        val target = directory.resolve("guild.yml")
        AtomicFiles.write(target, "lives: 2\n")
        AtomicFiles.write(target, "lives: 3\n")
        assertEquals("lives: 3\n", Files.readString(target))
        assertEquals("lives: 2\n", Files.readString(directory.resolve("guild.yml.bak")))
        Files.list(directory).use { assertEquals(2L, it.count()) }
    }

    @Test
    fun `malformed YAML is rejected without modifying the file`() {
        val target = directory.resolve("bad.yml")
        val content = "members: [broken\n"
        Files.writeString(target, content)
        assertFailsWith<IllegalStateException> { YamlWrapper(target.toFile()) }
        assertEquals(content, Files.readString(target))
    }
}
