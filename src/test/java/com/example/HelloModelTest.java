package com.example;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;


    static class NtfyConnectionSpy implements NtfyConnection {
        String lastSentMessage;
        final String fakeClientId = "test-client-id";
        Consumer<NtfyMessageDto> capturedHandler;

        @Override
        public String getClientId() {
            return fakeClientId;
        }

        @Override
        public CompletableFuture<Boolean> send(String message) {
            this.lastSentMessage = message;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public void receive(Consumer<NtfyMessageDto> handler) {
            this.capturedHandler = handler;
        }
    }

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized, ignore
        }
    }
    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    void sendMessage_ShouldCallConnectionSend_AndNotAddLocally() {
        model.sendMessage("Hello World").join();


        assertThat(spy.lastSentMessage).isEqualTo("Hello World");


        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    void receiveOwnMessage_ShouldSetTopicToMe() throws InterruptedException {
        NtfyMessageDto incoming = new NtfyMessageDto(
                "id1", 12345L, "message", "mytopic", "Green Bubble", spy.fakeClientId
        );

        spy.capturedHandler.accept(incoming);
        waitForFxEvents();


        ObservableList<NtfyMessageDto> messages = model.getMessages();
        assertThat(messages).hasSize(1);


        assertThat(messages.get(0).topic()).isEqualTo("me");
    }

    @Test
    void receiveStrangerMessage_ShouldKeepTopic() throws InterruptedException {

        NtfyMessageDto incoming = new NtfyMessageDto(
                "id2", 12345L, "message", "mytopic", "Stranger Message", "other-id"
        );

        spy.capturedHandler.accept(incoming);
        waitForFxEvents();

        ObservableList<NtfyMessageDto> messages = model.getMessages();

        assertThat(messages).hasSize(1);

        assertThat(messages.get(0).topic()).isEqualTo("mytopic");
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(2, TimeUnit.SECONDS);
    }
}