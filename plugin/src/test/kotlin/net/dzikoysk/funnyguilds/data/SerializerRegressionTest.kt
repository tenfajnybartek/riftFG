package net.dzikoysk.funnyguilds.data

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.data.database.Database
import net.dzikoysk.funnyguilds.data.database.SQLDataModel
import net.dzikoysk.funnyguilds.data.database.element.SQLTable
import net.dzikoysk.funnyguilds.data.database.element.SQLType
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseUserSerializer
import net.dzikoysk.funnyguilds.data.flat.FlatDataModel
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatUserSerializer
import net.dzikoysk.funnyguilds.data.util.YamlWrapper
import net.dzikoysk.funnyguilds.feature.ban.BanUtils
import net.dzikoysk.funnyguilds.guild.Guild
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import panda.std.Option
import java.nio.file.Path
import java.nio.file.Files
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SerializerRegressionTest : FunnyGuildsSpec() {
    @TempDir lateinit var directory: Path

    @Test
    fun `username and point changes also invalidate the persisted guild snapshot`() {
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val guild = Guild("Guild", "TAG").apply { owner = user }
        user.setGuild(guild)
        user.markUnchanged()
        guild.markUnchanged()
        userManager.updateUsername(user, "Renamed")
        assertTrue(user.wasChanged())
        assertTrue(guild.wasChanged())
        guild.markUnchanged()
        user.rank.points = 50
        assertTrue(guild.wasChanged())
    }

    @Test
    fun `invalid user data aborts FLAT loading without deleting guild files`() {
        val data = Files.createDirectories(directory.resolve("data"))
        `when`(funnyGuilds.dataFolder).thenReturn(directory.toFile())
        `when`(funnyGuilds.pluginDataFolder).thenReturn(data.toFile())
        Files.createDirectories(data.resolve("users"))
        Files.createDirectories(data.resolve("guilds"))
        val user = data.resolve("users/${UUID.randomUUID()}.yml")
        Files.writeString(user, "uuid: malformed\nname: Tester\n")
        val guild = data.resolve("guilds/Guild.yml")
        Files.writeString(guild, "name: Guild\nowner: Tester\n")
        val model = FlatDataModel(funnyGuilds)
        assertFailsWith<IllegalArgumentException> { model.load() }
        assertEquals("name: Guild\nowner: Tester\n", Files.readString(guild))
        assertEquals("uuid: malformed\nname: Tester\n", Files.readString(user))
    }

    @Test
    fun `flat unban survives save and the snapshot keeps newer edits dirty`() {
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val model = mock(FlatDataModel::class.java)
        val file = directory.resolve("user.yml").toFile()
        `when`(funnyGuilds.dataModel).thenReturn(model)
        `when`(model.getUserFile(user)).thenReturn(Option.of(file))
        BanUtils.ban(user, Duration.ofDays(1), "test")
        FlatUserSerializer.serialize(user)
        assertTrue(YamlWrapper(file).contains("ban"))
        BanUtils.unban(user)
        user.rank.points = 123
        val snapshot = FlatUserSerializer.prepare(user)
        user.rank.points = 456
        snapshot.run()
        val stored = YamlWrapper(file)
        assertFalse(stored.contains("ban"))
        assertFalse(stored.contains("reason"))
        assertEquals(123, stored.getInt("points"))
        assertTrue(user.wasChanged())
    }

    @Test
    fun `SQL execution failure leaves the entity dirty`() {
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val model = mock(SQLDataModel::class.java)
        val table = SQLTable("users").apply {
            add("uuid", SQLType.VARCHAR, 36, true)
            add("name", SQLType.TEXT, true)
            setPrimaryKey("uuid")
        }
        val database = mock(Database::class.java)
        val connection = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        `when`(funnyGuilds.dataModel).thenReturn(model)
        `when`(funnyGuilds.database).thenReturn(Option.of(database))
        `when`(model.usersTable).thenReturn(table)
        `when`(database.connection).thenReturn(connection)
        `when`(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenThrow(SQLException("database unavailable"))
        assertFailsWith<IllegalStateException> { DatabaseUserSerializer.serialize(user) }
        assertTrue(user.wasChanged())
    }

    @Test
    fun `guild unban clears both guild and member state`() {
        val user = userManager.createFake(UUID.randomUUID(), "Tester")
        val guild = Guild("Guild", "TAG").apply { owner = user }
        BanUtils.ban(guild, Duration.ofDays(1), "test")
        BanUtils.unban(guild)
        assertFalse(guild.isBanned)
        assertFalse(user.isBanned)
    }

    @Test
    fun `anti farming cooldown survives serialization and expires independently`() {
        val attacker = Guild("Attacker", "ATK")
        val victim = Guild("Victim", "VIC")
        attacker.setGainLifeCooldownOn(victim, Duration.ofHours(12))
        val restored = Guild(attacker.uuid, attacker.name, attacker.tag)
        restored.restoreLifeGainCooldowns(attacker.serializeLifeGainCooldowns())
        assertFalse(restored.canGainLifeOn(victim, Duration.ofHours(12)))
        assertTrue(restored.canGainLifeOn(Guild("Other", "OTH"), Duration.ofHours(12)))
        restored.restoreLifeGainCooldowns("${victim.uuid}=1")
        assertTrue(restored.canGainLifeOn(victim, Duration.ofHours(12)))
    }
}
