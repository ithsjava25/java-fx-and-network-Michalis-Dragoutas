package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();


    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessages();
    }


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


        Platform.runLater(() -> messages.add(myMsg));


        connection.sendWithId(text, localId).thenAccept(success -> {
            if (!success) {
                System.err.println("Failed to send message to server");

            }
        });
    }

    /** Start receiving messages from server */
    private void receiveMessages() {
        connection.receive(msg -> Platform.runLater(() -> messages.add(msg)));
    }
}
