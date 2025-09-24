package org.dimasik.liteauction.api.events.market.compile;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

@Getter
public class MarketSellItemAddEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final SellItem sellItem;
    private final Inventory inventory;
    private final int slot;

    public MarketSellItemAddEvent(SellItem sellItem, Inventory inventory, int slot){
        this.sellItem = sellItem;
        this.inventory = inventory;
        this.slot = slot;
    }
}
