package server.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import server.controller.ServerController;

import java.util.*;

/**
 * JavaFX view for the server. Only handles UI — delegates all logic to ServerController.
 */
public class ServerView {

    private final ServerController controller;
    private ListView<String> clientListView;
    private TextArea logArea;
    private Label statusLabel;
    private Circle statusCircle;

    // Random colors per user
    private final Map<String, String> userColors = new HashMap<>();
    private final List<String> colorPalette = Arrays.asList(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#82E0AA"
    );
    private int colorIndex = 0;

    public ServerView(ServerController controller) {
        this.controller = controller;
    }

    public void buildUI(Stage stage) {
        stage.setTitle("TCP Group Chat — Server");

        // ── Header ──────────────────────────────────────────────
        Label title = new Label("TCP Chat Server");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        statusCircle = new Circle(8, Color.LIMEGREEN);
        statusLabel = new Label("Offline");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        HBox statusBox = new HBox(6, statusCircle, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        VBox headerContent = new VBox(4, title, statusBox);
        HBox header = new HBox(headerContent);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #1e1e2e;");

        // ── Connected Clients panel ──────────────────────────────
        Label clientsLabel = new Label("Connected Clients");
        clientsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        clientsLabel.setStyle("-fx-text-fill: #cccccc;");

        clientListView = new ListView<>();
        clientListView.setPrefHeight(300);
        clientListView.setStyle(
            "-fx-background-color: #2a2a3e; -fx-control-inner-background: #2a2a3e; " +
            "-fx-border-color: #44446a; -fx-border-radius: 6; -fx-background-radius: 6;"
        );

        // Custom cell factory for colored backgrounds
        clientListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String color = userColors.getOrDefault(item, "#555577");
                    Circle dot = new Circle(7, Color.web(color));
                    Label name = new Label(item);
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                    HBox box = new HBox(10, dot, name);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(6, 10, 6, 10));
                    box.setStyle("-fx-background-color: " + color + "33; " +
                                 "-fx-border-color: " + color + "66; " +
                                 "-fx-border-radius: 4; -fx-background-radius: 4;");
                    setGraphic(box);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        VBox clientsPanel = new VBox(10, clientsLabel, clientListView);
        clientsPanel.setPadding(new Insets(16));
        clientsPanel.setStyle("-fx-background-color: #252538; -fx-border-color: #44446a; " +
                              "-fx-border-width: 0 1 0 0;");
        clientsPanel.setPrefWidth(220);

        // ── Activity Log panel ───────────────────────────────────
        Label logLabel = new Label("Server Activity Log");
        logLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        logLabel.setStyle("-fx-text-fill: #cccccc;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-control-inner-background: #1a1a2e; -fx-background-color: #1a1a2e; " +
            "-fx-text-fill: #e0e0e0; -fx-font-family: 'Consolas'; -fx-font-size: 12px; " +
            "-fx-border-color: #44446a; -fx-border-radius: 6;"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Button clearBtn = new Button("Clear Log");
        clearBtn.setStyle("-fx-background-color: #44446a; -fx-text-fill: white; " +
                          "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> logArea.clear());

        VBox logPanel = new VBox(10, logLabel, logArea, clearBtn);
        logPanel.setPadding(new Insets(16));
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        // ── Main layout ──────────────────────────────────────────
        HBox mainContent = new HBox(clientsPanel, logPanel);
        HBox.setHgrow(logPanel, Priority.ALWAYS);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        VBox root = new VBox(header, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #1e1e2e;");

        Scene scene = new Scene(root, 780, 560);
        stage.setScene(scene);
        stage.show();

        // Start the server after UI is ready
        controller.startServer();
    }

    public void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[LOG] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void addClient(String username) {
        Platform.runLater(() -> {
            if (!userColors.containsKey(username)) {
                userColors.put(username, colorPalette.get(colorIndex % colorPalette.size()));
                colorIndex++;
            }
            if (!clientListView.getItems().contains(username)) {
                clientListView.getItems().add(username);
            }
            updateStatus();
        });
    }

    public void removeClient(String username) {
        Platform.runLater(() -> {
            clientListView.getItems().remove(username);
            updateStatus();
        });
    }

    public void setServerOnline(boolean online) {
        Platform.runLater(() -> {
            if (online) {
                statusCircle.setFill(Color.LIMEGREEN);
                statusLabel.setText("Online — listening on port " + controller.getPort());
                statusLabel.setStyle("-fx-text-fill: #aaffaa; -fx-font-size: 13px;");
            } else {
                statusCircle.setFill(Color.RED);
                statusLabel.setText("Offline");
                statusLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 13px;");
            }
        });
    }

    private void updateStatus() {
        int count = clientListView.getItems().size();
        statusLabel.setText("Online — " + count + " client(s) connected | port " + controller.getPort());
    }
}
