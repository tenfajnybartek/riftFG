package net.dzikoysk.funnyguilds.feature.items.gui;

import java.util.HashMap;
import java.util.Map;
import net.dzikoysk.funnyguilds.config.message.MessageService;
import net.dzikoysk.funnyguilds.config.sections.items.GuildItemSet;
import net.dzikoysk.funnyguilds.config.sections.items.ItemsConfiguration;
import net.dzikoysk.funnyguilds.user.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuildItemsGui implements InventoryHolder {

    private final Player player;
    private final User user;
    private final GuildItemSet set;
    private final String setName;
    private final ItemsConfiguration config;
    private final MessageService messageService;

    private final Inventory inventory;
    private final Map<Integer, Runnable> actions = new HashMap<>();

    private boolean showOnlyMissing = false;

    public GuildItemsGui(
            Player player,
            User user,
            GuildItemSet set,
            String setName,
            ItemsConfiguration config,
            MessageService messageService
    ) {
        this.player = player;
        this.user = user;
        this.set = set;
        this.setName = setName;
        this.config = config;
        this.messageService = messageService;
        this.inventory = GuildItemsGuiFactory.buildInventory(this);
    }

    public void open() {
        this.player.openInventory(this.inventory);
    }

    public void close() {
        if (this.isViewing()) {
            this.player.closeInventory();
        }
    }

    public void toggleView() {
        this.showOnlyMissing = !this.showOnlyMissing;
        GuildItemsGuiFactory.populateInventory(this, this.inventory);
    }

    void clearActions() {
        this.actions.clear();
    }

    void bindAction(int slot, Runnable action) {
        this.actions.put(slot, action);
    }

    boolean hasAction(int slot) {
        return this.actions.containsKey(slot);
    }

    void handleClick(int slot) {
        if (!this.isViewing()) {
            return;
        }
        Runnable action = this.actions.get(slot);
        if (action != null) {
            action.run();
        }
    }

    private boolean isViewing() {
        return this.player.isOnline() && this.player.getOpenInventory().getTopInventory() == this.inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() { return player; }
    public User getUser() { return user; }
    public GuildItemSet getSet() { return set; }
    public String getSetName() { return setName; }
    public ItemsConfiguration getConfig() { return config; }
    public MessageService getMessageService() { return messageService; }
    public boolean isShowOnlyMissing() { return showOnlyMissing; }

}
