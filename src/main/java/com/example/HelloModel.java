package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    // Track only my messages by unique ID
    private final ConcurrentHashMap<String, Boolean> pendingMyMessages =
            new ConcurrentHashMap<>();

    // Constructor for real connection
    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessage();
    }

    // Constructor for tests
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void sendMessage(String text) {
        String id = UUID.randomUUID().toString();
        pendingMyMessages.put(id, Boolean.TRUE);

        long now = System.currentTimeMillis() / 1000;

        NtfyMessageDto myMsg =
                new NtfyMessageDto(id, now, "message", "me", text);

        Platform.runLater(() -> messages.add(myMsg));

        connection.sendWithId(text, id);
    }

    private void receiveMessage() {
        connection.receive(msg -> {
            // Check if it's my message
            if (pendingMyMessages.remove(msg.id()) != null) {
                return; //
            }

            Platform.runLater(() -> messages.add(msg));
        });
    }
}