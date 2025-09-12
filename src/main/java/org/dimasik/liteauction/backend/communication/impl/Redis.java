package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis extends AbstractCommunication {
    private JedisPool jedisPool;
    private JedisPubSub pubSub;
    private final String host;
    private final int port;
    private final String password;

    public Redis(String host, int port, String password, String channel) {
        super(channel);
        this.host = host;
        this.port = port;
        this.password = password;
        connect();
        subscribeToChannel();
    }

    @Override
    public void connect() {
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

    public void subscribeToChannel() {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                Redis.super.onMessage(channel, message);
            }
        };

        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(pubSub, channel + "_msg", channel + "_hover", channel + "_sound", channel + "_update");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "Auction redis thread").start();
    }

    @Override
    public void publishMessage(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(this.channel + "_" + channel, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}