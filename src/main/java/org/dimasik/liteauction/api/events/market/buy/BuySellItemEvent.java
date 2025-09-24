package org.dimasik.liteauction.api.events.market.buy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

@Getter
public class BuySellItemEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final SellItem item;

    public BuySellItemEvent(Player player, SellItem item){
        this.player = player;
        this.item = item;
    }
}
