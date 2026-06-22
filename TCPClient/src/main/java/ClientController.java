package client.controller;

import client.model.ClientModel;
import client.view.ClientView;

/**
 * Bridges ClientModel ↔ ClientView.
 */
public class ClientController {

    private final ClientModel model;
    private ClientView view;

    public ClientController(String[] args) {
        this.model = new ClientModel();

        // Override host/port from command-line args if provided
        if (args.length >= 2) {
            try {
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                model.setServerAddress(host, port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument, using config file defaults.");
            }
        }

        // Wire model → view callbacks
        model.setOnMessageReceived(msg -> { if (view != null) view.appendMessage(msg); });
        model.setOnStatusChange(status -> { if (view != null) view.setStatus(status); });
        model.setOnConnected(() -> { if (view != null) view.onConnected(model.isReadOnly()); });
        model.setOnDisconnected(() -> { if (view != null) view.onDisconnected(); });
    }

    public void setView(ClientView view) { this.view = view; }

    public void connect(String username) {
        model.connect(username);
    }

    public void sendMessage(String message) {
        if (message == null || message.isBlank()) return;
        String msg = message.trim();
        // Handle disconnect commands in controller
        if (msg.equalsIgnoreCase("end") || msg.equalsIgnoreCase("bye")) {
            model.sendMessage(msg);
            model.disconnect();
            return;
        }
        model.sendMessage(msg);
    }

    public void disconnect() {
        model.disconnect();
    }

    public boolean isConnected()  { return model.isConnected(); }
    public boolean isReadOnly()   { return model.isReadOnly(); }
    public String  getUsername()  { return model.getUsername(); }
    public String  getServerHost(){ return model.getServerHost(); }
    public int     getServerPort(){ return model.getServerPort(); }
}
