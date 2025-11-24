package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.CompletableFuture;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    public HelloModel() {
        this(new NtfyConnectionImpl());
    }

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public CompletableFuture<Void> sendMessage(String text) {

        return connection.send(text).thenAccept(success -> {
            if (!success) {
                System.err.println("Failed to send message to server");
            }
        });
    }

    private void receiveMessages() {
        connection.receive(msg -> {
            String topic = msg.topic();


            if (msg.title() != null && msg.title().equals(connection.getClientId())) {
                topic = "me";
            }

            NtfyMessageDto toAdd = new NtfyMessageDto(
                    msg.id(), msg.time(), msg.event(), topic, msg.message(), msg.title()
            );

            Platform.runLater(() -> messages.add(toAdd));
        });
    }
}