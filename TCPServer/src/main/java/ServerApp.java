package server;

import javafx.application.Application;
import javafx.stage.Stage;
import server.controller.ServerController;
import server.view.ServerView;

/**
 * Entry point for the TCP Chat Server application.
 * Usage: java TCPServer
 */
public class ServerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ServerController controller = new ServerController();
        ServerView view = new ServerView(controller);
        controller.setView(view);
        view.buildUI(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
