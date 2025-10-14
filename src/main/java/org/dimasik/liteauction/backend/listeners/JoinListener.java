package org.dimasik.liteauction.backend.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.storage.models.HistoryItem;
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;

import java.util.List;

public class JoinListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Player player = event.getPlayer();
        try {
            LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getAllPlayerItems(player.getName()).thenAccept((unsoldItems) -> {
                for(UnsoldItem unsoldItem : unsoldItems){
                    ItemStack itemStack = unsoldItem.decodeItemStack();
                    ItemHoverUtil.sendHoverItemMessage(player, PlaceholderUtils.replace(
                            player,
                            ConfigManager.getString("design/commands/history.yml", "unsold", "&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx%amount% &fоказался слишком дорогой или никому не нужен. Заберите предмет с Аукциона!"),
                            true,
                            new Pair<>("%amount%", String.valueOf(unsoldItem.getAmount()))
                    ), itemStack);
                }
            });
            LiteAuction.getInstance().getDatabaseManager().getHistoryItems().getPlayerHistory(player.getName(), true).thenAccept((historyItems) -> {
                if(!historyItems.isEmpty()){
                    player.sendMessage(Parser.color(ConfigManager.getString("design/commands/history.yml", "history-entry", "&#00D5FC▶ &#FCFCFCПока вас не было, у вас купили предметы:")));
                }
                for(HistoryItem historyItem : historyItems){
                    ItemStack itemStack = historyItem.decodeItemStack();
                    player.sendMessage(PlaceholderUtils.replace(
                            player,
                            ConfigManager.getString("design/commands/history.yml", "history", "&7[%datetime%] &#00B4D4%buyer% &#FCFCFCкупил у вас &#9AF6FC%item% x%amount% &#FCFCFCза &#FCC700%format:full_price%"),
                            true,
                            new Pair<>("%datetime%", Formatter.formatDateTime(historyItem.getTime())),
                            new Pair<>("%buyer%", historyItem.getBuyer()),
                            new Pair<>("%item%", itemStack.getType().toString().toUpperCase()),
                            new Pair<>("%amount%", String.valueOf(historyItem.getAmount())),
                            new Pair<>("%price%", String.valueOf(historyItem.getPrice())),
                            new Pair<>("%full_price%", String.valueOf(historyItem.getPrice() * historyItem.getAmount())),
                            new Pair<>("%format:price%", Formatter.formatPrice(historyItem.getPrice())),
                            new Pair<>("%format:full_price%", Formatter.formatPrice(historyItem.getPrice() * historyItem.getAmount()))
                    ));
                    LiteAuction.getInstance().getDatabaseManager().getHistoryItems().setDisplayed(historyItem.getId(), true);
                }
            });
        }
        catch (Exception ignored){}
    }
}
