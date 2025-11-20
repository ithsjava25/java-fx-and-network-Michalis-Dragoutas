package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicBoolean;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final AtomicBoolean senderMe = new AtomicBoolean(false);

    // Constructor for real connection
    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessage();
    }

    // Constructor for test (inject spy)
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void sendMessage(String text) {
        senderMe.set(true);

        // Add message locally
        long now = System.currentTimeMillis() / 1000;
        NtfyMessageDto myMsg = new NtfyMessageDto("local-" + now, now, "message", "me", text);

        Platform.runLater(() -> messages.add(myMsg));

        // Send via connection
        connection.send(text);
    }

    private void receiveMessage() {
        connection.receive(msg -> {
            if (senderMe.getAndSet(false)) return;  // Ignore own message
            Platform.runLater(() -> messages.add(msg));
        });
    }
}