package org.dimasik.socket;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Main {
    private static final String CONFIG_FILE = "server.properties";
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_PASSWORD = "admin123";

    private static int serverPort;
    private static String password;
    private static WebSocketServer server;
    private static final Set<WebSocket> authenticatedConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) {
        loadConfig();
        System.out.println("   [   зᴀпуᴄᴋ ᴄᴇᴘʙᴇᴘᴀ   ]   ");
        startServer();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        try {
            if (!configFile.exists()) {
                createDefaultConfig(configFile);
                System.out.println("   |   ᴄᴏздᴀʜ ȹᴀйл ᴋᴏʜфигуᴘᴀции");
            }

            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            }

            serverPort = Integer.parseInt(props.getProperty("server-port", DEFAULT_PORT));
            password = props.getProperty("password", DEFAULT_PASSWORD);

            System.out.println("   [   ᴋᴏʜфигуᴘᴀция зᴀгᴘужᴇʜᴀ   ]   ");
            System.out.println("   |   пᴏᴘт: " + serverPort);
            System.out.println("   |   пᴀᴘᴏль: " + (password.isEmpty() ? "ʜᴇ зᴀдᴀʜ" : "зᴀдᴀʜ"));

        } catch (IOException e) {
            System.err.println("   |   ᴏшибᴋᴀ чтᴇʜия ᴋᴏʜȹигуᴘᴀции: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("   |   ʜᴇʙᴇᴘʜый пᴏᴘт ʙ ᴋᴏʜфигуᴘᴀции");
            System.exit(1);
        }
    }

    private static void createDefaultConfig(File configFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            Properties defaultProps = new Properties();
            defaultProps.setProperty("server-port", DEFAULT_PORT);
            defaultProps.setProperty("password", DEFAULT_PASSWORD);
            defaultProps.store(fos, "WebSocket server properties");
        }
    }

    private static void startServer() {
        server = new WebSocketServer(new InetSocketAddress(serverPort)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("   |   ʜᴏʙᴏᴇ пᴏдᴋлючᴇʜиᴇ: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("   |   ᴏтᴋлючᴇʜиᴇ: " + conn.getRemoteSocketAddress());
                authenticatedConnections.remove(conn);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                handleMessage(conn, message);
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                String textMessage = StandardCharsets.UTF_8.decode(message).toString();
                handleMessage(conn, textMessage);
            }

            private void handleMessage(WebSocket conn, String message) {
                try {
                    String[] parts = message.split("\\|", 3);

                    if (parts.length < 3) {
                        conn.send("ERROR Неверный формат сообщения");
                        return;
                    }

                    String command = parts[0];
                    String channel = parts[1];
                    String content = parts[2];

                    if ("AUTH".equals(command)) {
                        handleAuthentication(conn, content);
                    } else if ("MESSAGE".equals(command)) {
                        handleChannelMessage(conn, channel, content);
                    } else {
                        conn.send("ERROR Неизвестная команда: " + command);
                    }

                } catch (Exception e) {
                    conn.send("ERROR Ошибка обработки сообщения: " + e.getMessage());
                }
            }

            private void handleAuthentication(WebSocket conn, String receivedPassword) {
                if (password.equals(receivedPassword)) {
                    authenticatedConnections.add(conn);
                    conn.send("AUTH SUCCESS");
                    System.out.println("   |   ᴋлиᴇʜт ᴀутᴇʜтифициᴘᴏʙᴀʜ: " + conn.getRemoteSocketAddress());
                } else {
                    conn.send("AUTH FAILED");
                    System.out.println("   |   ʜᴇудᴀчʜᴀя пᴏпытᴋᴀ ᴀутᴇʜтифиᴋᴀции: " + conn.getRemoteSocketAddress());
                }
            }

            private void handleChannelMessage(WebSocket conn, String channel, String message) {
                if (!authenticatedConnections.contains(conn)) {
                    conn.send("ERROR Требуется аутентификация");
                    return;
                }

                String broadcastMessage = "MESSAGE " + channel + " " + message;

                for (WebSocket client : authenticatedConnections) {
                    if (client.isOpen()) {
                        client.send(broadcastMessage);
                    }
                }

                System.out.println("   |   ᴄᴏᴏбщᴇʜиᴇ ʙ ᴋᴀʜᴀлᴇ '" + channel + "': " + message);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                if (conn != null) {
                    System.err.println("   |   ᴏшибᴋᴀ для ᴋлиᴇʜтᴀ " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
                } else {
                    System.err.println("   |   ᴏшибᴋᴀ ᴄᴇᴘʙᴇᴘᴀ: " + ex.getMessage());
                }
            }

            @Override
            public void onStart() {
                System.out.println("   |   ᴄᴇᴘʙᴇᴘ зᴀпущᴇʜ ʜᴀ пᴏᴘту " + serverPort);
            }
        };

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                System.out.println("   |   ᴄᴇᴘʙᴇᴘ ᴏᴄтᴀʜᴏʙлᴇʜ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}