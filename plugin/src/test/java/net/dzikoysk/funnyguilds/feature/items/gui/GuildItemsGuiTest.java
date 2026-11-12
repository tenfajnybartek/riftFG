package net.dzikoysk.funnyguilds.feature.items.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuildItemsGuiTest {

    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<GuildItemsGuiFactory> factory;
    private Player player;
    private Inventory inventory;
    private InventoryView view;
    private GuildItemsGui gui;
    private GuildItemsGuiListener listener;
    private final List<Runnable> scheduled = new ArrayList<>();

    @BeforeEach
    void prepareMenu() {
        this.bukkit = mockStatic(Bukkit.class);
        this.factory = mockStatic(GuildItemsGuiFactory.class);
        this.player = mock(Player.class);
        this.inventory = mock(Inventory.class);
        this.view = mock(InventoryView.class);
        this.factory.when(() -> GuildItemsGuiFactory.buildInventory(any(GuildItemsGui.class))).thenReturn(this.inventory);
        this.gui = new GuildItemsGui(this.player, null, null, "test", null, null);
        this.listener = new GuildItemsGuiListener();
        this.listener.plugin = mock(FunnyGuilds.class);

        when(this.player.isOnline()).thenReturn(true);
        when(this.player.getUniqueId()).thenReturn(new UUID(0, 1));
        when(this.player.getOpenInventory()).thenReturn(this.view);
        when(this.view.getPlayer()).thenReturn(this.player);
        when(this.view.getTopInventory()).thenReturn(this.inventory);
        when(this.inventory.getHolder(false)).thenReturn(this.gui);
        when(this.inventory.getSize()).thenReturn(27);

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        this.bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        doAnswer(invocation -> {
            this.scheduled.add(invocation.getArgument(1));
            return mock(BukkitTask.class);
        }).when(scheduler).runTask(any(Plugin.class), any(Runnable.class));
    }

    @AfterEach
    void closeMocks() {
        this.factory.close();
        this.bukkit.close();
    }

    @Test
    void filterClickIsCancelledAndUpdatesOnTheNextTick() {
        this.gui.bindAction(4, this.gui::toggleView);
        InventoryClickEvent event = this.click(4, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        this.listener.onClick(event);

        assertTrue(event.isCancelled());
        assertFalse(this.gui.isShowOnlyMissing());
        this.scheduled.getFirst().run();
        assertTrue(this.gui.isShowOnlyMissing());
        this.factory.verify(() -> GuildItemsGuiFactory.populateInventory(this.gui, this.inventory));
    }

    @Test
    void shiftClickAndOutsideClickCannotMoveItemsOrTriggerMenuActions() {
        for (int slot : new int[] {27, 35, -999}) {
            InventoryClickEvent event = this.click(slot, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
            this.listener.onClick(event);
            assertTrue(event.isCancelled());
        }
        assertTrue(this.scheduled.isEmpty());
    }

    @Test
    void closeButtonClosesTheCurrentMenuOnTheNextTick() {
        this.gui.bindAction(8, this.gui::close);
        InventoryClickEvent event = this.click(8, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        this.listener.onClick(event);
        assertTrue(event.isCancelled());
        verify(this.player, never()).closeInventory();
        this.scheduled.getFirst().run();
        verify(this.player).closeInventory();
    }

    @Test
    void queuedCloseDoesNotCloseAnotherMenuOpenedInTheSameTick() {
        this.gui.bindAction(8, this.gui::close);
        this.listener.onClick(this.click(8, ClickType.LEFT, InventoryAction.PICKUP_ALL));
        when(this.view.getTopInventory()).thenReturn(mock(Inventory.class));

        this.scheduled.getFirst().run();
        verify(this.player, never()).closeInventory();
    }

    @Test
    void queuedActionIsDiscardedAfterDisconnect() {
        this.gui.bindAction(4, this.gui::toggleView);
        this.listener.onClick(this.click(4, ClickType.LEFT, InventoryAction.PICKUP_ALL));
        when(this.player.isOnline()).thenReturn(false);

        this.scheduled.getFirst().run();
        assertFalse(this.gui.isShowOnlyMissing());
    }

    @Test
    void draggingInAnOpenMenuIsBlocked() {
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getView()).thenReturn(this.view);
        this.listener.onDrag(event);
        verify(event).setCancelled(true);
    }

    @Test
    void ordinaryInventoriesAreUnaffected() {
        when(this.inventory.getHolder(false)).thenReturn(mock(InventoryHolder.class));
        InventoryClickEvent event = this.click(4, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        this.listener.onClick(event);
        assertFalse(event.isCancelled());
        assertTrue(this.scheduled.isEmpty());
    }

    private InventoryClickEvent click(int slot, ClickType click, InventoryAction action) {
        return new InventoryClickEvent(this.view, SlotType.CONTAINER, slot, click, action);
    }
}
