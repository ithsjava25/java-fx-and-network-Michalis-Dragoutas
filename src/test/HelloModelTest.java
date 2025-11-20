package com.example;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;

    static class NtfyConnectionSpy implements NtfyConnection {
        String lastMessage;
        String lastId;

        @Override
        public boolean sendWithId(String message, String id) {
            this.lastMessage = message;
            this.lastId = id;
            return true;
        }

        @Override
        public void receive(java.util.function.Consumer<NtfyMessageDto> handler) {}
    }

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    void sendMessageAddsMessageToList() {
        model.sendMessage("Hello Test");

        ObservableList<NtfyMessageDto> messages = model.getMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).message()).isEqualTo("Hello Test");
        assertThat(messages.get(0).topic()).isEqualTo("me");
        assertThat(messages.get(0).id()).isNotEmpty();
    }

    @Test
    void sendMessageCallsSpy() {
        model.sendMessage("Hello");

        assertThat(spy.lastMessage).isEqualTo("Hello");
        assertThat(spy.lastId).isNotNull();
    }
}