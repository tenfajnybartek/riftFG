package net.dzikoysk.funnyguilds.nms.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NmsAccessorHolderTest {

    @Test
    void rejectsOlderServersWithUpgradeInstructions() {
        for (int dataVersion : new int[] {0, 3953, 4080, 4189, 4554, 4788, 5022}) {
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> NmsAccessorHolder.requireSupportedDataVersion(dataVersion));
            assertTrue(exception.getMessage().contains("Minecraft 26.3"));
            assertTrue(exception.getMessage().contains("Java 25"));
        }
    }

    @Test
    void acceptsMinimumSupportedServer() {
        assertDoesNotThrow(() -> NmsAccessorHolder.requireSupportedDataVersion(5023));
    }

    @Test
    void allowsNewerServersToTryTheCurrentAdapter() {
        assertDoesNotThrow(() -> NmsAccessorHolder.requireSupportedDataVersion(5024));
    }
}
