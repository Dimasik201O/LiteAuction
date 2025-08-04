package org.dimasik.liteauction.frontend.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.enums.SortingType;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.menus.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.dimasik.liteauction.LiteAuction.removeClosedUpdates;

public class MainListener implements Listener {
    @EventHandler
    public void on(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Main){
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Main main = (Main) inventory.getHolder();
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if (slot < 45) {
                    if(event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER){
                        return;
                    }
                    SellItem sellItem = main.getItems().get(slot);
                    if(sellItem != null){
                        if(sellItem.getPlayer().equalsIgnoreCase(player.getName())){
                            new RemoveItem(sellItem, main).setPlayer(player).compile().open();
                            return;
                        }
                        else{
                            if(event.getClick() == ClickType.SWAP_OFFHAND && player.hasPermission("liteauction.admin")){
                                player.sendMessage("");
                                player.sendMessage("Вы нажали 'F' - означает удаление предмета.");
                                player.sendMessage("Для подтверждения удаления предмета напишите команду:");
                                player.sendMessage("/ah admin deleteItem " + sellItem.getId());
                                player.sendMessage("");
                                player.closeInventory();
                                player.updateInventory();
                                Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), player::updateInventory, 1);
                            }
                            else if(event.getClick() == ClickType.MIDDLE || event.getClick() == ClickType.DROP){
                                if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                                }
                                Main newMain = new Main(1);
                                newMain.setFilters(TagUtil.getPartialTags(sellItem.decodeItemStack()));
                                newMain.setCategoryType(main.getCategoryType());
                                newMain.setSortingType(main.getSortingType());
                                newMain.setPlayer(player).compile().open();
                            }
                            else if(event.isLeftClick() || sellItem.isByOne() || sellItem.getAmount() == 1) {
                                double money = LiteAuction.getEconomyEditor().getBalance(player.getName());
                                int price = sellItem.getPrice() * sellItem.getAmount();
                                if(money < price){
                                    player.sendMessage(Parser.color("&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки."));
                                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                                    }
                                    ItemStack origItem = inventory.getItem(slot).clone();
                                    if(true){
                                        ItemStack itemStack = new ItemStack(Material.BARRIER);
                                        ItemMeta itemMeta = itemStack.getItemMeta();
                                        itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CУ вас &x&F&F&2&2&2&2нет денег &x&D&5&D&B&D&Cна это!"));
                                        itemStack.setItemMeta(itemMeta);
                                        inventory.setItem(slot, itemStack);
                                    }
                                    Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                                        if(!inventory.getViewers().isEmpty()) {
                                            inventory.setItem(slot, origItem);
                                        }
                                    }, 20);
                                    return;
                                }

                                new ConfirmItem(sellItem, main).setPlayer(player).compile().open();
                            }
                            else if(event.isRightClick()){
                                new CountBuyItem(sellItem, main, 1).setPlayer(player).compile().open();
                            }
                        }
                    }
                } else if (slot == 45) {
                    new Sell(1, main).setPlayer(player).compile().open();
                } else if (slot == 46) {
                    new Unsold(1, main).setPlayer(player).compile().open();
                } else if (slot == 47) {
                    player.sendMessage(Parser.color("&#00D4FB▶ &fАукцион обновлен."));
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1f);
                    }

                    int newPage = main.getPage();

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(main.getSortingType());
                    newMain.setPlayer(player).compile().open();
                } else if (slot == 48) {
                    int newPage = main.getPage() - 1;

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != main.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        Main newMain = new Main(newPage);
                        newMain.setTarget(main.getPlayer());
                        newMain.setFilters(main.getFilters());
                        newMain.setCategoryType(main.getCategoryType());
                        newMain.setSortingType(main.getSortingType());
                        newMain.setPlayer(player).compile().open();
                    }
                } else if (slot == 50) {
                    int newPage = main.getPage() + 1;

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != main.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        Main newMain = new Main(newPage);
                        newMain.setTarget(main.getPlayer());
                        newMain.setFilters(main.getFilters());
                        newMain.setCategoryType(main.getCategoryType());
                        newMain.setSortingType(main.getSortingType());
                        newMain.setPlayer(player).compile().open();
                    }
                }
                else if (slot == 52) {
                    int newPage = main.getPage();

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    if(event.isLeftClick()){
                        newMain.setSortingType(main.getSortingType().relative(true));
                        newMain.setPlayer(player).compile().open();
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
                    }
                    else if(event.isRightClick()){
                        newMain.setSortingType(main.getSortingType().relative(false));
                        newMain.setPlayer(player).compile().open();
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
                    }
                }
                else if (slot == 53) {
                    int newPage = main.getPage();

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setSortingType(main.getSortingType());
                    if(event.isLeftClick()){
                        newMain.setCategoryType(main.getCategoryType().relative(true));
                        newMain.setPlayer(player).compile().open();
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
                    }
                    else if(event.isRightClick()){
                        newMain.setCategoryType(main.getCategoryType().relative(false));
                        newMain.setPlayer(player).compile().open();
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
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
        if(inventory.getHolder() instanceof Main){
            removeClosedUpdates();
        }
    }
}
