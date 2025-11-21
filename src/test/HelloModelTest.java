package com.example;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;


    static class NtfyConnectionSpy implements NtfyConnection {

        String sentMessage;

        @Override
        public CompletableFuture<Boolean> sendWithId(String message, String localId) {
            sentMessage = message;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public void receive(java.util.function.Consumer<NtfyMessageDto> handler) {

        }
    }

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    void sendMessageAddsMessageToList() {
        model.sendMessage("Hello World");

        ObservableList<NtfyMessageDto> messages = model.getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).message()).isEqualTo("Hello World");
        assertThat(messages.get(0).topic()).isEqualTo("me");
        assertThat(messages.get(0).id()).startsWith("local-");
    }

    @Test
    void sendMessageCallsSpySend() {
        model.sendMessage("Test Spy");


        Thread.sleep(100);

        assertThat(spy.sentMessage).isEqualTo("Test Spy");
    }

}
