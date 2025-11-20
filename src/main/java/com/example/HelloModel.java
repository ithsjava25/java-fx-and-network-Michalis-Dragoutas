package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    // Track messages we sent (IDs)
    private final Set<String> pending = ConcurrentHashMap.newKeySet();

    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessage();
    }

    public HelloModel(NtfyConnection injected) {
        this.connection = injected;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /** Async send */
    public CompletableFuture<Boolean> sendMessage(String text) {

        String localId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis() / 1000;

        pending.add(localId);

        // Add local version immediately
        NtfyMessageDto local = new NtfyMessageDto(localId, now, "message", "me", text);

        Platform.runLater(() -> messages.add(local));

        // Send to server asynchronously
        return connection.sendWithId(text, localId)
                .thenApply(success -> {
                    if (!success) pending.remove(localId);
                    return success;
                });
    }

    private void receiveMessage() {

        connection.receive(msg -> {

            // Ignore echo of my own message
            if (pending.remove(msg.id())) {
                return;
            }

            Platform.runLater(() -> messages.add(msg));
        });
    }
}