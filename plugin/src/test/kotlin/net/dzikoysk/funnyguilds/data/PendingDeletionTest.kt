package net.dzikoysk.funnyguilds.data

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.data.flat.FlatDataModel
import net.dzikoysk.funnyguilds.data.util.YamlWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PendingDeletionTest : FunnyGuildsSpec() {
    @TempDir lateinit var directory: Path

    @Test
    fun `an interrupted explicit deletion is replayed without deleting unrelated data`() {
        val model = mock(FlatDataModel::class.java)
        `when`(funnyGuilds.dataModel).thenReturn(model)
        `when`(funnyGuilds.pluginDataFolder).thenReturn(directory.toFile())
        val blocked = Files.createDirectories(directory.resolve("guilds/Target.yml"))
        Files.writeString(blocked.resolve("blocker"), "test")
        val unrelated = directory.resolve("guilds/Other.yml")
        Files.writeString(unrelated, "name: Other")
        val operation = PendingDeletion.prepare(funnyGuilds, "guild", UUID.randomUUID().toString(), "Target", null)
        assertFailsWith<IllegalStateException> { operation.run() }
        val journal = directory.resolve("pending-deletions.yml").toFile()
        assertEquals(1, YamlWrapper(journal).getMapList("deletions").size)
        Files.delete(blocked.resolve("blocker"))
        PendingDeletion.replay(funnyGuilds, model)
        assertFalse(Files.exists(blocked))
        assertTrue(Files.exists(unrelated))
        assertTrue(YamlWrapper(journal).getMapList("deletions").isEmpty())
    }
}
