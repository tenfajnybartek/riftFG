package net.dzikoysk.funnyguilds.feature.items.gui;

import net.dzikoysk.funnyguilds.listener.AbstractFunnyListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class GuildItemsGuiListener extends AbstractFunnyListener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof GuildItemsGui gui)) {
            return;
        }

        // Also blocks shift-click, number keys, off-hand swaps and collecting into the cursor.
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= topInventory.getSize() || !gui.hasAction(slot)
                || !event.getWhoClicked().getUniqueId().equals(gui.getPlayer().getUniqueId())) {
            return;
        }

        // Opening/closing inventories during InventoryClickEvent is unsafe.
        // handleClick also checks that the same menu is still open on the next tick.
        Bukkit.getScheduler().runTask(this.plugin, () -> gui.handleClick(slot));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof GuildItemsGui) {
            event.setCancelled(true);
        }
    }
}
