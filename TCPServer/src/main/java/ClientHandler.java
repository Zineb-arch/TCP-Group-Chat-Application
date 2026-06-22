package server.model;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handles communication with a single connected client in its own thread.
 * Separation of Concerns: pure socket/IO logic, no JavaFX dependencies.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerModel serverModel;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean readOnly = false;

    // Callbacks to notify the UI layer
    private Consumer<String> onLog;
    private BiConsumer<String, Boolean> onClientUpdate; // username, connected?

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientHandler(Socket socket, ServerModel serverModel,
                         Consumer<String> onLog,
                         BiConsumer<String, Boolean> onClientUpdate) {
        this.socket = socket;
        this.serverModel = serverModel;
        this.onLog = onLog;
        this.onClientUpdate = onClientUpdate;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // First line sent by client is the username (or empty for read-only)
            username = in.readLine();
            if (username == null) { disconnect(); return; }

            if (username.trim().isEmpty()) {
                readOnly = true;
                username = "Guest_" + socket.getPort();
                out.println("[SERVER] You are in READ-ONLY MODE. You cannot send messages.");
            } else {
                out.println("[SERVER] Welcome, " + username + "! You are now connected.");
                serverModel.broadcast("[" + now() + "] " + username + " has joined the chat.", this);
            }

            serverModel.addClient(this);
            log("Welcome " + username + (readOnly ? " (READ-ONLY)" : ""));
            notifyClientUpdate(username, true);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("end") || message.equalsIgnoreCase("bye")) {
                    out.println("[SERVER] Goodbye, " + username + "!");
                    break;
                } else if (message.equalsIgnoreCase("allUsers")) {
                    out.println("[SERVER] Active users: " + serverModel.getActiveUsernames());
                } else if (!readOnly) {
                    String formatted = "[" + now() + "] " + username + ": " + message;
                    serverModel.broadcast(formatted, this);
                    // Echo to sender too
                    out.println(formatted);
                } else {
                    out.println("[SERVER] You are in READ-ONLY MODE. You cannot send messages.");
                }
            }
        } catch (IOException e) {
            log("Connection lost with " + (username != null ? username : "unknown"));
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public String getUsername() { return username; }

    private void disconnect() {
        try {
            serverModel.removeClient(this);
            if (username != null) {
                serverModel.broadcast("[" + now() + "] " + username + " has left the chat.", this);
                log(username + " disconnected.");
                notifyClientUpdate(username, false);
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private String now() {
        return LocalTime.now().format(TIME_FMT);
    }

    private void log(String msg) {
        if (onLog != null) onLog.accept(msg);
    }

    private void notifyClientUpdate(String name, boolean connected) {
        if (onClientUpdate != null) onClientUpdate.accept(name, connected);
    }
}
