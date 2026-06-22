package client.model;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Core client model: manages TCP connection and message I/O.
 * No JavaFX — pure networking logic (Separation of Concerns).
 */
public class ClientModel {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverHost;
    private int serverPort;
    private String username;
    private boolean connected = false;
    private boolean readOnly = false;

    // Callbacks for the UI layer
    private Consumer<String> onMessageReceived;
    private Consumer<String> onStatusChange;
    private Runnable onConnected;
    private Runnable onDisconnected;

    public ClientModel() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("client.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load client.properties");
        }
        serverHost = props.getProperty("server.host", "localhost");
        serverPort = Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    // Allow overriding host/port from command-line args
    public void setServerAddress(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }

    public void setOnMessageReceived(Consumer<String> cb) { this.onMessageReceived = cb; }
    public void setOnStatusChange(Consumer<String> cb)    { this.onStatusChange = cb; }
    public void setOnConnected(Runnable cb)               { this.onConnected = cb; }
    public void setOnDisconnected(Runnable cb)            { this.onDisconnected = cb; }

    /**
     * Connect to the server in a background thread.
     * @param username empty string → read-only mode
     */
    public void connect(String username) {
        this.username = username;
        this.readOnly = username.trim().isEmpty();

        new Thread(() -> {
            try {
                socket = new Socket(serverHost, serverPort);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send username as first line
                out.println(username);
                connected = true;
                notifyStatus("Connected to " + serverHost + ":" + serverPort);
                if (onConnected != null) onConnected.run();

                // Listen for incoming messages
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    if (onMessageReceived != null) onMessageReceived.accept(msg);
                }
            } catch (IOException e) {
                notifyStatus("Connection error: " + e.getMessage());
            } finally {
                connected = false;
                if (onDisconnected != null) onDisconnected.run();
            }
        }, "ClientReceiveThread").start();
    }

    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        sendMessage("bye");
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { /* ignore */ }
    }

    public boolean isConnected()  { return connected; }
    public boolean isReadOnly()   { return readOnly; }
    public String  getUsername()  { return username; }
    public String  getServerHost(){ return serverHost; }
    public int     getServerPort(){ return serverPort; }

    private void notifyStatus(String msg) {
        if (onStatusChange != null) onStatusChange.accept(msg);
    }
}
