package net.dzikoysk.funnyguilds.data

import net.dzikoysk.funnyguilds.data.database.Database
import net.dzikoysk.funnyguilds.guild.Guild
import org.junit.jupiter.api.Test
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.sql.Connection
import java.sql.SQLException
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatabaseTransactionTest {
    @Test
    fun `acknowledgement is delayed until commit and withheld when commit fails`() {
        val database = mock(Database::class.java, CALLS_REAL_METHODS)
        val connection = mock(Connection::class.java)
        doReturn(connection).`when`(database).connection
        val guild = Guild("Guild", "TAG")
        val revision = guild.revision
        doThrow(SQLException("commit failed")).`when`(connection).commit()
        assertFailsWith<IllegalStateException> {
            database.transaction { Database.afterCommit { guild.markSaved(revision) } }
        }
        verify(connection).rollback()
        assertTrue(guild.wasChanged())
        assertNull(Database.currentTransaction())
        doAnswer { assertTrue(guild.wasChanged()); null }.`when`(connection).commit()
        database.transaction { Database.afterCommit { guild.markSaved(revision) } }
        assertFalse(guild.wasChanged())
        assertNull(Database.currentTransaction())
    }
}
