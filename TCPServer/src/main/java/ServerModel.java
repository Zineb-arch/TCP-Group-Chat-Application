package server.model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Core server model: manages connections, broadcasting, and config loading.
 * No JavaFX imports — pure business logic (Separation of Concerns).
 */
public class ServerModel {

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private int port;

    // UI callbacks
    private Consumer<String> onLog;
    private BiConsumer<String, Boolean> onClientUpdate;
    private Runnable onStarted;

    public ServerModel() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load server.properties, using default port 3000");
        }
        port = Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    public void setOnLog(Consumer<String> onLog) { this.onLog = onLog; }
    public void setOnClientUpdate(BiConsumer<String, Boolean> onClientUpdate) { this.onClientUpdate = onClientUpdate; }
    public void setOnStarted(Runnable onStarted) { this.onStarted = onStarted; }

    /** Start listening for connections in a background thread. */
    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("Server Started on port " + port);
                if (onStarted != null) onStarted.run();

                while (!serverSocket.isClosed()) {
                    log("Waiting for Client...");
                    Socket clientSocket = serverSocket.accept();
                    log("New connection from " + clientSocket.getInetAddress().getHostAddress());
                    ClientHandler handler = new ClientHandler(clientSocket, this, onLog, onClientUpdate);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) log("Server error: " + e.getMessage());
            }
        }, "ServerAcceptThread").start();
    }

    public void addClient(ClientHandler handler) {
        clients.add(handler);
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
        log("[BROADCAST] " + message);
    }

    public String getActiveUsernames() {
        return clients.stream()
                .map(ClientHandler::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    public int getPort() { return port; }

    private void log(String msg) {
        if (onLog != null) onLog.accept(msg);
        else System.out.println("[SERVER] " + msg);
    }
}
