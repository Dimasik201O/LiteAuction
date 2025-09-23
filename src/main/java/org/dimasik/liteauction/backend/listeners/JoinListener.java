package org.dimasik.liteauction.backend.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;

import java.util.List;

public class JoinListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Player player = event.getPlayer();
        try {
            List<UnsoldItem> unsoldItems = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getAllPlayerItems(player.getName()).get();
            for(UnsoldItem unsoldItem : unsoldItems){
                ItemStack itemStack = unsoldItem.decodeItemStack();
                ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + unsoldItem.getAmount() + " &fоказался слишком дорогой или никому не нужен. Заберите предмет с Аукциона!"), itemStack);
            }
        }
        catch (Exception ignored){}
    }
}
