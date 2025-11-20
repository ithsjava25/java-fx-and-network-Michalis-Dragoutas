package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    // Constructor for real connection
    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessages();
    }

    // Constructor for dependency injection (e.g., for tests)
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /** Send message asynchronously */
    public void sendMessage(String text) {
        long now = System.currentTimeMillis() / 1000;
        String localId = "local-" + UUID.randomUUID();
        NtfyMessageDto myMsg = new NtfyMessageDto(localId, now, "message", "me", text);

        // Add message locally immediately
        Platform.runLater(() -> messages.add(myMsg));

        // Send asynchronously
        connection.sendWithId(text, localId).thenAccept(success -> {
            if (!success) {
                System.err.println("Failed to send message to server");
                // Optionally update UI to indicate failure
            }
        });
    }

    /** Start receiving messages from server */
    private void receiveMessages() {
        connection.receive(msg -> Platform.runLater(() -> messages.add(msg)));
    }
}
