package org.dimasik.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.market.menus.CountBuyItem;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;

import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class CountBuyItemListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof CountBuyItem countBuyItem) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if(slot == 0){
                    int newCount = countBuyItem.getCount() - 10;

                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(countBuyItem.getSellItem().getId()).get();
                    if(sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        player.closeInventory();
                        return;
                    }
                    SellItem sellItem = sellItemOptional.get();
                    newCount = Math.min(newCount, sellItem.getAmount());
                    newCount = Math.max(newCount, 1);

                    countBuyItem.setForceClose(true);
                    new CountBuyItem(sellItem, countBuyItem.getBack(), newCount).setPlayer(player).compile().open();
                }
                else if(slot == 1){
                    int newCount = countBuyItem.getCount() - 1;

                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(countBuyItem.getSellItem().getId()).get();
                    if(sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        player.closeInventory();
                        return;
                    }
                    SellItem sellItem = sellItemOptional.get();
                    newCount = Math.min(newCount, sellItem.getAmount());
                    newCount = Math.max(newCount, 1);

                    countBuyItem.setForceClose(true);
                    new CountBuyItem(sellItem, countBuyItem.getBack(), newCount).setPlayer(player).compile().open();
                }
                else if(slot == 2){
                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(countBuyItem.getSellItem().getId()).get();
                    if(sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        player.closeInventory();
                        return;
                    }

                    SellItem sellItem = sellItemOptional.get();
                    if(sellItem.getAmount() < countBuyItem.getCount()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        player.closeInventory();
                        return;
                    }

                    int price = sellItem.getPrice() * countBuyItem.getCount();
                    double money = LiteAuction.getEconomyEditor().getBalance(player.getName());
                    if(money < price){
                        player.sendMessage(Parser.color("&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки."));
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                        }
                        player.closeInventory();
                        return;
                    }

                    ItemStack itemStack = countBuyItem.getSellItem().decodeItemStack();
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#FEA900▶ &fВы купили &#FEA900%item%&f &#FEA900x" + countBuyItem.getCount() + " &fу &#FEA900" + sellItem.getPlayer() + " &fза &#FEA900" + Formatter.formatPrice(price)), itemStack);
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1f, 1f);
                    }

                    LiteAuction.getInstance().getRedisManager().publishMessage("hover", sellItem.getPlayer() + " " + Parser.color(ItemHoverUtil.getHoverItemMessage("&#00D4FB▶ &#00D5FB" + player.getName() + " &fкупил у вас &#9AF5FB%item%&f &#9AF5FBx" + countBuyItem.getCount() + " &fза &#FEA900" + price + Formatter.CURRENCY_SYMBOL, sellItem.decodeItemStack().asQuantity(sellItem.getAmount()))));
                    LiteAuction.getInstance().getRedisManager().publishMessage("sound", sellItem.getPlayer() + " " + Sound.ENTITY_WANDERING_TRADER_YES.toString().toLowerCase() + " 1.0 1.0");

                    LiteAuction.getEconomyEditor().addBalance(sellItem.getPlayer().toLowerCase(), price);
                    LiteAuction.getEconomyEditor().subtractBalance(player.getName().toLowerCase(), price);

                    addItemInventory(player.getInventory(), itemStack.asQuantity(countBuyItem.getCount()), player.getLocation());
                    if(countBuyItem.getCount() == sellItem.getAmount()) {
                        LiteAuction.getInstance().getRedisManager().publishMessage("update", "market " + sellItem.getId());
                        LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(sellItem.getId());
                    }
                    else{
                        sellItem.setAmount(sellItem.getAmount() - countBuyItem.getCount());
                        LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().updateItem(sellItem);
                    }

                    player.closeInventory();
                }
                else if(slot == 3){
                    int newCount = countBuyItem.getCount() + 1;

                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(countBuyItem.getSellItem().getId()).get();
                    if(sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        player.closeInventory();
                        return;
                    }
                    SellItem sellItem = sellItemOptional.get();
                    newCount = Math.min(newCount, sellItem.getAmount());
                    newCount = Math.max(newCount, 1);

                    countBuyItem.setForceClose(true);
                    new CountBuyItem(sellItem, countBuyItem.getBack(), newCount).setPlayer(player).compile().open();
                }
                else if(slot == 4){
                    int newCount = countBuyItem.getCount() + 10;

                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(countBuyItem.getSellItem().getId()).get();
                    if(sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        return;
                    }
                    SellItem sellItem = sellItemOptional.get();
                    newCount = Math.min(newCount, sellItem.getAmount());
                    newCount = Math.max(newCount, 1);

                    countBuyItem.setForceClose(true);
                    new CountBuyItem(sellItem, countBuyItem.getBack(), newCount).setPlayer(player).compile().open();
                }
            } catch (Exception e) {
                player.closeInventory();
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."));
            }
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof CountBuyItem countBuyItem) {
            if(countBuyItem.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = countBuyItem.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}