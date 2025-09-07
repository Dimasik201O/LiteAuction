package org.dimasik.liteauction.backend.redis;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.BidItem;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.ContainerUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.dimasik.liteauction.frontend.menus.bids.menus.ItemBids;
import org.dimasik.liteauction.frontend.menus.market.menus.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.Map;

public class RedisManager {
    private JedisPool jedisPool;
    private JedisPubSub pubSub;
    private final String host;
    private final int port;
    private final String password;
    private final String channel;

    public RedisManager(String host, int port, String password, String channel) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.channel = channel;
        connect();
        subscribeToChannel();
    }

    private void connect() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            jedisPool = new JedisPool(poolConfig,
                    host,
                    port,
                    2000,
                    password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeToChannel() {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    if (channel.equals(RedisManager.this.channel + "_msg")) {
                        String[] splitted = message.split(" ");
                        String msg = String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length));
                        String playerName = splitted[0];
                        Player player = Bukkit.getPlayer(playerName);
                        if (player != null) {
                            player.sendMessage(msg);
                        }
                    } else if (channel.equals(RedisManager.this.channel + "_sound")) {
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
                    else if (channel.equals(RedisManager.this.channel + "_update")) {
                        String[] splitted = message.split(" ");
                        int id = Integer.parseInt(splitted[1]);
                        if(splitted[0].equalsIgnoreCase("market")){
                            for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()){
                                if(ContainerUtil.hasActiveContainer(player)){
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
                                            Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                gui.setForceClose(true);
                                                gui.compile().open();
                                                gui.setForceClose(false);
                                            });
                                        }
                                    }
                                    else if(holder instanceof Main gui){
                                        for(Map.Entry<Integer, SellItem> entry : gui.getItems().entrySet()){
                                            if(entry.getValue().getId() == id){
                                                Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                    inventory.setItem(entry.getKey(), LiteAuction.getBoughtItem().clone());
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if(splitted[0].equalsIgnoreCase("bids")){
                            String action = splitted[2];
                            for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                                if (ContainerUtil.hasActiveContainer(player)) {
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
                                            Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                gui.setForceClose(true);
                                                gui.compile().open();
                                                gui.setForceClose(false);
                                            });
                                        }
                                    }
                                    else if(holder instanceof org.dimasik.liteauction.frontend.menus.bids.menus.Main gui){
                                        if(action.equalsIgnoreCase("delete")){
                                            for(Map.Entry<Integer, BidItem> entry : gui.getItems().entrySet()){
                                                if(entry.getValue().getId() == id){
                                                    Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                        inventory.setItem(entry.getKey(), LiteAuction.getBoughtItem().clone());
                                                    });
                                                }
                                            }
                                        }
                                    }
                                    else if(holder instanceof ItemBids gui){
                                        if(action.equalsIgnoreCase("delete")){
                                            if(gui.getBidItem().getId() == id){
                                                Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                    inventory.setItem(4, LiteAuction.getBoughtItem().clone());
                                                });
                                            }
                                        }
                                        else if(action.equalsIgnoreCase("refresh")){
                                            Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                gui.setForceClose(true);
                                                gui.compile().open();
                                                gui.setForceClose(false);
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignore) { }
            }
        };

        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(pubSub, channel + "_msg", channel + "_sound", channel + "_update");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Auction redis thread").start();
    }

    public void publishMessage(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(this.channel + "_" + channel, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void publishMessage(String channel, int message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(this.channel + "_" + channel, String.valueOf(message));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}