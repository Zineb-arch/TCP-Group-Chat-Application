package server.controller;

import server.model.ServerModel;
import server.view.ServerView;

/**
 * Bridges the ServerModel and ServerView.
 * Controller receives callbacks from the model and updates the view on the JavaFX thread.
 */
public class ServerController {

    private final ServerModel model;
    private ServerView view;

    public ServerController() {
        this.model = new ServerModel();

        // Wire model callbacks → view updates
        model.setOnLog(msg -> { if (view != null) view.appendLog(msg); });
        model.setOnClientUpdate((username, connected) -> {
            if (view != null) {
                if (connected) view.addClient(username);
                else           view.removeClient(username);
            }
        });
        model.setOnStarted(() -> { if (view != null) view.setServerOnline(true); });
    }

    public void setView(ServerView view) {
        this.view = view;
    }

    public void startServer() {
        model.startServer();
    }

    public int getPort() {
        return model.getPort();
    }
}
