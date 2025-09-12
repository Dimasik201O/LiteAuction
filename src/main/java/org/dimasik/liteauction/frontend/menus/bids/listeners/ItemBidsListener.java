package org.dimasik.liteauction.frontend.menus.bids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.Bid;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.bids.menus.ItemBids;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;

import java.util.List;
import java.util.Optional;

public class ItemBidsListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof ItemBids itemBids) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if(slot == 45){
                    Main main = itemBids.getBack();
                    main.compile().open();
                }
                else if(itemBids.getAvailableBids().containsKey(slot) && !itemBids.getBidItem().getPlayer().equalsIgnoreCase(player.getName())){
                    Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(itemBids.getBidItem().getId()).get();
                    if (bidItemOptional.isEmpty()){
                        player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                        return;
                    }

                    ItemStack itemStack = itemBids.getBidItem().decodeItemStack();
                    List<Bid> bids = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemId(itemBids.getBidItem().getId()).get();
                    Bid lastBid = null;
                    if(!bids.isEmpty()) lastBid = bids.get(bids.size() - 1);
                    int addPrice = lastBid != null && lastBid.getPlayer().equalsIgnoreCase(player.getName()) ? bids.get(bids.size() - 1).getPrice() : 0;
                    int finalPrice = itemBids.getAvailableBids().get(slot);

                    if(finalPrice - addPrice > LiteAuction.getEconomyEditor().getBalance(player.getName())){
                        player.sendMessage(Parser.color("&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки."));
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                        }
                        return;
                    }

                    player.sendMessage(Parser.color("&#00D4FB▶ &fВы поставили ставку."));
                    LiteAuction.getEconomyEditor().subtractBalance(player.getName(), finalPrice - addPrice);
                    if(lastBid != null && !lastBid.getPlayer().equalsIgnoreCase(player.getName())){
                        LiteAuction.getEconomyEditor().addBalance(lastBid.getPlayer(), finalPrice - addPrice);
                        LiteAuction.getInstance().getCommunicationManager().publishMessage(
                                "hover",
                                lastBid.getPlayer() + " " +
                                ItemHoverUtil.getHoverItemMessage(
                                        Parser.color("&#00D4FB▶ &fВаша ставка на предмет &6%item%&6 x" + itemStack.getAmount() + " &fу &6" + itemBids.getBidItem().getPlayer() + " &fбыла перебита игроком &6" + player.getName() + "!"),
                                        itemStack
                                )
                        );
                    }
                    List<Integer> slotsList = itemBids.getAvailableBidsSlots();
                    for(int i = 0; i < slotsList.size(); i++){
                        if(slotsList.get(i) == slot){
                            BidItem bidItem = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(itemBids.getBidItem().getId()).get().get();
                            bidItem.setCurrentPrice(finalPrice);
                            if(bidItem.getExpiryTime() - System.currentTimeMillis() < 5000) {
                                bidItem.setExpiryTime(System.currentTimeMillis() + 30000);
                            }
                            LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().updateItem(bidItem);
                            LiteAuction.getInstance().getDatabaseManager().getBidsManager().addBid(
                                    itemBids.getBidItem().getId(),
                                    player.getName(),
                                    itemBids.getAvailableBids().get(slotsList.get(i))
                            );
                            LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "bids " + itemBids.getBidItem().getId() + " refresh");
                            return;
                        }
                        LiteAuction.getInstance().getDatabaseManager().getBidsManager().addBid(
                                itemBids.getBidItem().getId(),
                                player.getName(),
                                itemBids.getAvailableBids().get(slotsList.get(i))
                        );
                    }
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
        if(inventory.getHolder() instanceof ItemBids itemBids){
            if(itemBids.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = itemBids.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
