package org.dimasik.liteauction.backend.redis;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
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
                        int id = Integer.parseInt(message);
                        for(Map.Entry<UpdateData, Integer> entry : LiteAuction.getItems().entrySet()){
                            if(entry.getValue() == id) {
                                UpdateData updateData = entry.getKey();
                                updateData.getInventory().setItem(updateData.getSlot(), LiteAuction.getBoughtItem().clone());
                                LiteAuction.getItems().remove(updateData, id);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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