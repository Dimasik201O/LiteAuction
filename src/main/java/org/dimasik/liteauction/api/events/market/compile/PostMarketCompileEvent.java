package org.dimasik.liteauction.api.events.market.compile;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

import java.util.List;

@Getter
public class PostMarketCompileEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final List<SellItem> items;

    public PostMarketCompileEvent(Player player, List<SellItem> items){
        this.player = player;
        this.items = items;
    }
}
