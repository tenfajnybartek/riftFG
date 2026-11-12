package net.dzikoysk.funnyguilds.nms.v26_3;

import net.dzikoysk.funnyguilds.nms.api.NmsAccessor;
import net.dzikoysk.funnyguilds.nms.api.entity.EntityAccessor;
import net.dzikoysk.funnyguilds.nms.api.packet.PacketAccessor;
import net.dzikoysk.funnyguilds.nms.api.playerlist.PlayerListAccessor;
import net.dzikoysk.funnyguilds.nms.api.statistics.StatisticsAccessor;
import net.dzikoysk.funnyguilds.nms.v26_3.entity.V26_3EntityAccessor;
import net.dzikoysk.funnyguilds.nms.v26_3.packet.V26_3PacketAccessor;
import net.dzikoysk.funnyguilds.nms.v26_3.playerlist.V26_3PlayerList;
import net.dzikoysk.funnyguilds.nms.v26_3.statistics.V26_3StatisticsAccessor;

public class V26_3NmsAccessor implements NmsAccessor {

    @Override
    public PacketAccessor getPacketAccessor() {
        return new V26_3PacketAccessor();
    }

    @Override
    public PlayerListAccessor getPlayerListAccessor() {
        return V26_3PlayerList::new;
    }

    @Override
    public StatisticsAccessor getStatisticsAccessor() {
        return new V26_3StatisticsAccessor();
    }

    @Override
    public EntityAccessor getEntityAccessor() {
        return new V26_3EntityAccessor();
    }

}
