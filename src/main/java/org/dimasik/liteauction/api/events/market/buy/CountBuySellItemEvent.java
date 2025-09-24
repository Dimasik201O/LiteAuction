package org.dimasik.liteauction.api.events.market.buy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

@Getter
public class CountBuySellItemEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final SellItem item;
    private final int amount;

    public CountBuySellItemEvent(Player player, SellItem item, int amount){
        this.player = player;
        this.item = item;
        this.amount = amount;
    }
}
