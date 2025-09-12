package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ extends AbstractCommunication {
    private Connection connection;
    private Channel channel;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String virtualHost;
    private DefaultConsumer consumer;

    public RabbitMQ(String host, int port, String username, String password,
                    String virtualHost, String channel) {
        super(channel);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
        connect();
        subscribeToChannel();
    }

    @Override
    public void connect() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            factory.setConnectionTimeout(2000);
            factory.setRequestedHeartbeat(30);

            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(channel + "_msg", "fanout", true);
            channel.exchangeDeclare(channel + "_hover", "fanout", true);
            channel.exchangeDeclare(channel + "_sound", "fanout", true);
            channel.exchangeDeclare(channel + "_update", "fanout", true);

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to connect to RabbitMQ", e);
        }
    }

    public void subscribeToChannel() {
        try {
            String queueNameMsg = channel.queueDeclare().getQueue();
            String queueNameHover = channel.queueDeclare().getQueue();
            String queueNameSound = channel.queueDeclare().getQueue();
            String queueNameUpdate = channel.queueDeclare().getQueue();

            channel.queueBind(queueNameMsg, this.channel + "_msg", "");
            channel.queueBind(queueNameHover, this.channel + "_hover", "");
            channel.queueBind(queueNameSound, this.channel + "_sound", "");
            channel.queueBind(queueNameUpdate, this.channel + "_update", "");

            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body);
                    String routingKey = envelope.getExchange();
                    String channelName = routingKey.replace(RabbitMQ.super.channel + "_", "");

                    RabbitMQ.super.onMessage(channelName, message);
                }
            };

            channel.basicConsume(queueNameMsg, true, consumer);
            channel.basicConsume(queueNameHover, true, consumer);
            channel.basicConsume(queueNameSound, true, consumer);
            channel.basicConsume(queueNameUpdate, true, consumer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishMessage(String channelType, String message) {
        try {
            channel.basicPublish(this.channel + "_" + channelType, "", null, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException | TimeoutException ignore) { }
    }
}