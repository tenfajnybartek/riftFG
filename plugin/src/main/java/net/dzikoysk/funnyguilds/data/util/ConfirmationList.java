package net.dzikoysk.funnyguilds.data.util;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfirmationList {

    private static final Map<UUID, Confirmation> CONFIRMATIONS = new ConcurrentHashMap<>();

    private ConfirmationList() {
    }

    public static void add(UUID player, UUID guild) {
        add(player, guild, Instant.now());
    }

    static void add(UUID player, UUID guild, Instant now) {
        CONFIRMATIONS.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        CONFIRMATIONS.put(player, new Confirmation(guild, now.plusSeconds(60)));
    }

    public static void remove(UUID player) {
        CONFIRMATIONS.remove(player);
    }

    public static boolean contains(UUID player, UUID guild) {
        Confirmation confirmation = CONFIRMATIONS.get(player);
        return confirmation != null && confirmation.guild().equals(guild) && confirmation.expiresAt().isAfter(Instant.now());
    }

    private record Confirmation(UUID guild, Instant expiresAt) {
    }
}
