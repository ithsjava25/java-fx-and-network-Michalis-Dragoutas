package com.example;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;

    // Simple spy to capture sent messages
    static class NtfyConnectionSpy implements NtfyConnection {
        String sentMessage;

        @Override
        public boolean send(String message) {
            sentMessage = message;
            return true;
        }

        @Override
        public void receive(java.util.function.Consumer<NtfyMessageDto> messageHandler) {
            // No-op for testing sending
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
        assertThat(messages.get(0).id()).startsWith("local");
    }

    @Test
    void sendMessageCallsSpySend() {
        model.sendMessage("Test Spy");
        assertThat(spy.sentMessage).isEqualTo("Test Spy");
    }
}