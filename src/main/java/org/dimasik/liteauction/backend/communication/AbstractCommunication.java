package org.dimasik.liteauction.backend.communication;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.nms.ContainerUtil;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.frontend.menus.bids.menus.ItemBids;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;
import org.dimasik.liteauction.frontend.menus.market.menus.Sell;

import java.util.Arrays;
import java.util.Map;

public abstract class AbstractCommunication {
    protected String channel;

    public AbstractCommunication(String channel){
        this.channel = channel;
    }

    public void connect() {}
    public void onMessage(String channel, String message) {
        try {
            if (channel.equals(this.channel + "_msg")) {
                String[] splitted = message.split(" ");
                String msg = String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length));
                String playerName = splitted[0];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.sendMessage(msg);
                }
            } else if (channel.equals(this.channel + "_hover")) {
                String[] splitted = message.split(" ");
                String msg = String.join(" ", Arrays.copyOfRange(splitted, 2, splitted.length));
                String playerName = splitted[0];
                String encodedItemStack = splitted[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    ItemHoverUtil.sendHoverItemMessage(player, msg, ItemEncryptUtil.decodeItem(encodedItemStack));
                }
            } else if (channel.equals(this.channel + "_sound")) {
                String[] splitted = message.split(" ");
                String playerName = splitted[0];
                Sound sound = Sound.valueOf(splitted[1].toUpperCase());
                float volume = Float.parseFloat(splitted[2]);
                float pitch = Float.parseFloat(splitted[3]);
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(playerName).get()) {
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }
                }
            }
            else if (channel.equals(this.channel + "_update")) {
                String[] splitted = message.split(" ");
                int id = Integer.parseInt(splitted[1]);
                if(splitted[0].equalsIgnoreCase("market")){
                    for(Player player : Bukkit.getOnlinePlayers()){
                        Inventory inventory = ContainerUtil.getActiveContainer(player);
                        InventoryHolder holder = inventory.getHolder();
                        if(holder instanceof Sell gui){
                            if(
                                    gui
                                            .getItems()
                                            .values()
                                            .stream()
                                            .anyMatch(i -> i.getId() == id)
                            ){
                                gui.setForceClose(true);

                                int newPage = gui.getPage();
                                int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItemsCount(gui.getViewer().getName()).get();
                                int pages = items / 45 + (items % 45 == 0 ? 0 : 1);
                                newPage = Math.min(pages, newPage);
                                newPage = Math.max(1, newPage);

                                Sell newSell = new Sell(newPage, gui.getBack());
                                newSell.setPlayer(player).compile().open();
                            }
                        }
                        else if(holder instanceof Main gui){
                            for(Map.Entry<Integer, SellItem> entry : gui.getItems().entrySet()){
                                if(entry.getValue().getId() == id){
                                    Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                        inventory.setItem(entry.getKey(), LiteAuction.getInstance().getBoughtItem().clone());
                                    });
                                }
                            }
                        }
                    }
                }
                else if(splitted[0].equalsIgnoreCase("bids")){
                    String action = splitted[2];
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        Inventory inventory = ContainerUtil.getActiveContainer(player);
                        InventoryHolder holder = inventory.getHolder();
                        if(holder instanceof org.dimasik.liteauction.frontend.menus.bids.menus.Sell gui){
                            if(
                                    gui
                                            .getItems()
                                            .values()
                                            .stream()
                                            .anyMatch(i -> i.getId() == id)
                            ){
                                gui.setForceClose(true);

                                int newPage = gui.getPage();
                                int items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getPlayerItemsCount(gui.getViewer().getName()).get();
                                int pages = items / 45 + (items % 45 == 0 ? 0 : 1);
                                newPage = Math.min(pages, newPage);
                                newPage = Math.max(1, newPage);

                                org.dimasik.liteauction.frontend.menus.bids.menus.Sell newSell = new org.dimasik.liteauction.frontend.menus.bids.menus.Sell(newPage, gui.getBack());
                                newSell.setPlayer(player).compile().open();
                            }
                        }
                        else if(holder instanceof org.dimasik.liteauction.frontend.menus.bids.menus.Main gui){
                            if(action.equalsIgnoreCase("delete")){
                                for(Map.Entry<Integer, BidItem> entry : gui.getItems().entrySet()){
                                    if(entry.getValue().getId() == id){
                                        Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                            inventory.setItem(entry.getKey(), LiteAuction.getInstance().getBoughtItem().clone());
                                        });
                                    }
                                }
                            }
                        }
                        else if(holder instanceof ItemBids gui){
                            if(action.equalsIgnoreCase("delete")){
                                if(gui.getBidItem().getId() == id){
                                    Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                        inventory.setItem(4, LiteAuction.getInstance().getBoughtItem().clone());
                                    });
                                }
                            }
                            else if(action.equalsIgnoreCase("refresh")){
                                if(gui.getBidItem().getId() == id) {
                                    gui.setForceClose(true);

                                    ItemBids itemBids = new ItemBids(gui.getBidItem(), gui.getBack());
                                    itemBids.setPlayer(player).compile().open();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) { }
    }

    public abstract void publishMessage(String channel, String message);
    public void publishMessage(String channel, int message) {
        this.publishMessage(channel, String.valueOf(message));
    }

    public void close() {}
}
