package client.view;

import client.controller.ClientController;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX view for the TCP Chat Client.
 * Shows a login screen first, then the main chat interface.
 */
public class ClientView {

    private final ClientController controller;
    private Stage stage;

    // Chat scene components
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Label statusLabel;
    private Circle statusCircle;
    private Label readOnlyBanner;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public ClientView(ClientController controller) {
        this.controller = controller;
    }

    public void buildUI(Stage stage) {
        this.stage = stage;
        stage.setTitle("TCP Group Chat — Client");
        stage.setOnCloseRequest(e -> controller.disconnect());
        showLoginScreen();
        stage.show();
    }

    // ── Login Screen ────────────────────────────────────────────

    private void showLoginScreen() {
        Label title = new Label("Group Chat");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Connect to " + controller.getServerHost() + ":" + controller.getServerPort());
        subtitle.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username...");
        usernameField.setMaxWidth(300);
        usernameField.setStyle(
            "-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-prompt-text-fill: #555577; " +
            "-fx-border-color: #44446a; -fx-border-radius: 6; -fx-background-radius: 6; " +
            "-fx-font-size: 14px; -fx-padding: 10 14;"
        );

        Label hint = new Label("Leave blank to join in Read-Only Mode");
        hint.setStyle("-fx-text-fill: #666688; -fx-font-size: 11px;");

        Button connectBtn = new Button("Connect");
        connectBtn.setMaxWidth(300);
        connectBtn.setStyle(
            "-fx-background-color: #6c63ff; -fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-font-weight: bold; -fx-padding: 10 0; -fx-border-radius: 6; " +
            "-fx-background-radius: 6; -fx-cursor: hand;"
        );

        Button readOnlyBtn = new Button("Join as Read-Only");
        readOnlyBtn.setMaxWidth(300);
        readOnlyBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 13px; " +
            "-fx-border-color: #44446a; -fx-border-radius: 6; -fx-background-radius: 6; " +
            "-fx-padding: 8 0; -fx-cursor: hand;"
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        connectBtn.setOnAction(e -> {
            String name = usernameField.getText().trim();
            connectAndSwitch(name, errorLabel);
        });
        readOnlyBtn.setOnAction(e -> connectAndSwitch("", errorLabel));
        usernameField.setOnAction(e -> connectBtn.fire());

        VBox card = new VBox(14, title, subtitle, new Separator(), usernameField, hint, connectBtn, readOnlyBtn, errorLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(380);
        card.setStyle(
            "-fx-background-color: #1e1e2e; -fx-border-color: #44446a; " +
            "-fx-border-radius: 12; -fx-background-radius: 12;"
        );

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #13131f;");

        stage.setScene(new Scene(root, 520, 480));
    }

    private void connectAndSwitch(String username, Label errorLabel) {
        errorLabel.setText("");
        controller.connect(username);
        // The view switches on onConnected callback
    }

    // ── Chat Screen ─────────────────────────────────────────────

    private void buildChatScreen(boolean readOnly) {
        // Header
        Label title = new Label("Group Chat");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        String userDisplay = controller.getUsername().isEmpty()
            ? "Guest (Read-Only)" : controller.getUsername();
        Label userLabel = new Label("Logged in as: " + userDisplay);
        userLabel.setStyle("-fx-text-fill: #aaaacc; -fx-font-size: 12px;");

        statusCircle = new Circle(7, Color.LIMEGREEN);
        statusLabel = new Label("Online");
        statusLabel.setStyle("-fx-text-fill: #aaffaa; -fx-font-size: 12px;");
        HBox statusBox = new HBox(5, statusCircle, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.setStyle(
            "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 12px; " +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 12;"
        );
        disconnectBtn.setOnAction(e -> controller.disconnect());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox headerLeft = new VBox(4, title, userLabel);
        HBox header = new HBox(12, headerLeft, spacer, statusBox, disconnectBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle("-fx-background-color: #1e1e2e;");

        // Read-only banner
        readOnlyBanner = new Label("👁  READ-ONLY MODE — You can view messages but cannot send.");
        readOnlyBanner.setStyle(
            "-fx-background-color: #3a2a0a; -fx-text-fill: #ffcc44; -fx-font-size: 12px; " +
            "-fx-padding: 8 16; -fx-font-weight: bold;"
        );
        readOnlyBanner.setMaxWidth(Double.MAX_VALUE);
        readOnlyBanner.setVisible(readOnly);
        readOnlyBanner.setManaged(readOnly);

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setStyle(
            "-fx-control-inner-background: #1a1a2e; -fx-background-color: #1a1a2e; " +
            "-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
            "-fx-border-color: #33334a; -fx-border-radius: 6; -fx-background-radius: 6;"
        );
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Input row
        messageField = new TextField();
        messageField.setPromptText(readOnly ? "You are in read-only mode..." : "Type a message... (\"allUsers\" to list users, \"bye\" to quit)");
        messageField.setDisable(readOnly);
        messageField.setStyle(
            "-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-prompt-text-fill: #555577; " +
            "-fx-border-color: #44446a; -fx-border-radius: 6; -fx-background-radius: 6; " +
            "-fx-font-size: 13px; -fx-padding: 9 12;"
        );
        HBox.setHgrow(messageField, Priority.ALWAYS);

        sendButton = new Button("SEND");
        sendButton.setDisable(readOnly);
        sendButton.setStyle(
            "-fx-background-color: #6c63ff; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-border-radius: 6; -fx-background-radius: 6; " +
            "-fx-padding: 9 18; -fx-cursor: hand;"
        );

        sendButton.setOnAction(e -> sendCurrentMessage());
        messageField.setOnAction(e -> sendCurrentMessage());

        HBox inputRow = new HBox(10, messageField, sendButton);
        inputRow.setAlignment(Pos.CENTER);

        VBox chatPanel = new VBox(12, chatArea, inputRow);
        chatPanel.setPadding(new Insets(14, 18, 14, 18));
        VBox.setVgrow(chatPanel, Priority.ALWAYS);

        VBox root = new VBox(header, readOnlyBanner, chatPanel);
        VBox.setVgrow(chatPanel, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #13131f;");

        Platform.runLater(() -> {
            stage.setScene(new Scene(root, 700, 520));
            stage.setTitle("TCP Group Chat — " + userDisplay);
        });
    }

    private void sendCurrentMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;
        controller.sendMessage(msg);
        messageField.clear();
    }

    // ── Callbacks from Controller ────────────────────────────────

    public void appendMessage(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
            chatArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void setStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) statusLabel.setText(status);
        });
    }

    public void onConnected(boolean readOnly) {
        Platform.runLater(() -> buildChatScreen(readOnly));
    }

    public void onDisconnected() {
        Platform.runLater(() -> {
            if (statusCircle != null) {
                statusCircle.setFill(Color.RED);
                statusLabel.setText("Disconnected");
                statusLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 12px;");
            }
            if (sendButton != null) sendButton.setDisable(true);
            if (messageField != null) messageField.setDisable(true);
            appendMessage("[SYSTEM] You have been disconnected from the server.");
        });
    }
}
