package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebSocket extends AbstractCommunication {
    private WebSocketClient webSocketClient;
    private final String host;
    private final int port;
    private final String password;
    private boolean authenticated = false;

    public WebSocket(String host, int port, String password, String channel) {
        super(channel);
        this.host = host;
        this.port = port;
        this.password = password;
        connect();
    }

    @Override
    public void connect() {
        try {
            URI serverUri = new URI("ws://" + host + ":" + port);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    authenticate();
                }

                @Override
                public void onMessage(String message) {
                    handleIncomingMessage(message);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    String message = StandardCharsets.UTF_8.decode(bytes).toString();
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    authenticated = false;
                }

                @Override
                public void onError(Exception ex) {
                    throw new RuntimeException(ex);
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate() {
        if (password != null && !password.isEmpty()) {
            String authMessage = "AUTH|" + channel + "|" + password;
            webSocketClient.send(authMessage);
        }
    }

    private void handleIncomingMessage(String message) {
        try {
            String[] parts = message.split(" ", 3);

            if (parts.length < 2) {
                System.err.println("Invalid message format: " + message);
                return;
            }

            String command = parts[0];
            String channel = parts[1];
            String content = parts.length > 2 ? parts[2] : "";

            if ("AUTH".equals(command)) {
                if ("SUCCESS".equals(content)) {
                    authenticated = true;
                } else {
                    authenticated = false;
                }
            } else if ("MESSAGE".equals(command)) {
                WebSocket.super.onMessage(channel, content);
            } else if ("ERROR".equals(command)) {
                throw new RuntimeException("Ошибка в WebSocket: " + content);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishMessage(String channelType, String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String fullMessage = "MESSAGE|" + channel + "_" + channelType + "|" + message;
            webSocketClient.send(fullMessage);
        } else {
            throw new RuntimeException("DISCONNECTED");
        }
    }

    @Override
    public void close() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}