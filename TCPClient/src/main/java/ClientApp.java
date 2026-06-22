package client;

import client.controller.ClientController;
import client.view.ClientView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the TCP Chat Client.
 * Usage: java TCPClient <ServerIPAddress> <PortNumber>
 *   e.g. java TCPClient localhost 3000
 */
public class ClientApp extends Application {

    private static String[] appArgs;

    @Override
    public void start(Stage primaryStage) {
        ClientController controller = new ClientController(appArgs != null ? appArgs : new String[0]);
        ClientView view = new ClientView(controller);
        controller.setView(view);
        view.buildUI(primaryStage);
    }

    public static void main(String[] args) {
        appArgs = args;
        launch(args);
    }
}
