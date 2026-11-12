package net.dzikoysk.funnyguilds.nms.api.playerlist;

import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface PlayerList {

    default void clear(Player player) {
    }

    void send(Player player, Component[] playerListCells, Component header, Component footer, SkinTexture[] cellTextures,
              int ping, Set<Integer> forceUpdateSlots);

}
