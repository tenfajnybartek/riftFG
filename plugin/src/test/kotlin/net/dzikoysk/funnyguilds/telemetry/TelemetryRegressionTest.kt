package net.dzikoysk.funnyguilds.telemetry

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.bukkit.command.CommandSender
import java.net.InetSocketAddress
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TelemetryRegressionTest {
    @Test
    fun `HTTP errors are propagated and TLS properties are untouched`() {
        val tls = System.getProperty("jdk.tls.client.protocols")
        val https = System.getProperty("https.protocols")
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/ok") { exchange ->
            exchange.requestBody.use { it.readAllBytes() }
            val bytes = "\"ok\"".toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.createContext("/fail") { exchange ->
            exchange.requestBody.close()
            exchange.sendResponseHeaders(503, -1)
            exchange.close()
        }
        server.start()
        try {
            val post = FunnyTelemetry::class.java.getDeclaredMethod("sendPost", String::class.java, String::class.java, Class::class.java)
            post.isAccessible = true
            val base = "http://127.0.0.1:${server.address.port}"
            assertEquals("ok", post.invoke(null, "$base/ok", "test", String::class.java))
            val failure = assertFailsWith<InvocationTargetException> { post.invoke(null, "$base/fail", "test", String::class.java) }
            assertTrue(failure.cause is java.io.IOException)
            assertEquals(tls, System.getProperty("jdk.tls.client.protocols"))
            assertEquals(https, System.getProperty("https.protocols"))
        }
        finally { server.stop(0) }
    }

    @Test
    fun `custom export arguments return a task without submitting anything`() {
        val sender = mock(CommandSender::class.java)
        assertTrue(FunnybinAsyncTask.of(sender, arrayOf("funnybin", "custom", "test.txt")).isPresent)
    }
}
