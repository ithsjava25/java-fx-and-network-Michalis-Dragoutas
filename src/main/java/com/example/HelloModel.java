package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();


    private final List<String> pendingEchoes = Collections.synchronizedList(new ArrayList<>());

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

    public CompletableFuture<Void> sendMessage(String text) {
        long now = System.currentTimeMillis() / 1000;
        String localId = "local-" + UUID.randomUUID();


        pendingEchoes.add(text);


        return connection.sendWithId(text, localId).thenAccept(success -> {
            if (!success) {
                System.err.println("Failed to send message to server");

                pendingEchoes.remove(text);
            }
        });
    }

    private void receiveMessages() {
        connection.receive(msg -> {
            NtfyMessageDto finalMsg = msg;


            synchronized (pendingEchoes) {
                if (pendingEchoes.contains(msg.message())) {
                    pendingEchoes.remove(msg.message());


                    finalMsg = new NtfyMessageDto(
                            msg.id(),
                            msg.time(),
                            msg.event(),
                            "me",
                            msg.message()
                    );
                }
            }

            NtfyMessageDto toAdd = finalMsg;
            Platform.runLater(() -> messages.add(toAdd));
        });
    }
}