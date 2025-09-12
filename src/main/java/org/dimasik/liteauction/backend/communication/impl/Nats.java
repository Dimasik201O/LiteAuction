package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import io.nats.client.*;
import io.nats.client.api.PublishAck;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Nats extends AbstractCommunication {
    private Connection connection;
    private final String[] servers;
    private final String username;
    private final String password;
    private Dispatcher dispatcher;

    public Nats(String[] servers, String username, String password, String channel) {
        super(channel);
        this.servers = servers;
        this.username = username;
        this.password = password;
        connect();
        subscribeToChannel();
    }

    @Override
    public void connect() {
        try {
            Options.Builder optionsBuilder = new Options.Builder()
                    .servers(servers)
                    .connectionTimeout(Duration.ofSeconds(2))
                    .pingInterval(Duration.ofSeconds(30))
                    .reconnectWait(Duration.ofSeconds(1))
                    .maxReconnects(10);

            if (username != null && password != null) {
                optionsBuilder.userInfo(username, password);
            }
            connection = io.nats.client.Nats.connect(optionsBuilder.build());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribeToChannel() {
        dispatcher = connection.createDispatcher((msg) -> {
            String subject = msg.getSubject();
            String channelType = extractChannelType(subject);
            String message = new String(msg.getData());

            Nats.super.onMessage(channelType, message);
        });

        dispatcher.subscribe(channel + "_msg.*");
        dispatcher.subscribe(channel + "_hover.*");
        dispatcher.subscribe(channel + "_sound.*");
        dispatcher.subscribe(channel + "_update.*");
    }

    private String extractChannelType(String subject) {
        int underscoreIndex = subject.lastIndexOf('_');
        if (underscoreIndex != -1 && underscoreIndex < subject.length() - 1) {
            return subject.substring(underscoreIndex + 1);
        }
        return subject;
    }

    @Override
    public void publishMessage(String channelType, String message) {
        try {
            String subject = channel + "_" + channelType;
            connection.publish(subject, message.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (dispatcher != null) {
                dispatcher.unsubscribe(channel + "_msg.*");
                dispatcher.unsubscribe(channel + "_hover.*");
                dispatcher.unsubscribe(channel + "_sound.*");
                dispatcher.unsubscribe(channel + "_update.*");
            }
            if (connection != null) {
                connection.close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}